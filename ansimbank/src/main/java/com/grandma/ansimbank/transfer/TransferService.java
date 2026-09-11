package com.grandma.ansimbank.transfer;

import com.grandma.ansimbank.transfer.dto.TransferRequestDTO;
import com.grandma.ansimbank.transfer.dto.TransferResponseDTO;
import com.grandma.ansimbank.transaction.Transaction;
import com.grandma.ansimbank.transaction.TransactionRepository;
import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.user.UserRepository;
import com.grandma.ansimbank.account.Account;
import com.grandma.ansimbank.account.AccountRepository;
import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransferService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionAuditService transactionAuditService;

    public TransferResponseDTO transfer(TransferRequestDTO request) {
        // 송금인 조회
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 락을 잡기 전에 끝낼 수 있는 검증은 먼저 수행해 락 점유 시간을 최소화한다.
        validateTransferAmount(request.getAmount());
        validateSelfTransfer(request.getSenderAccount(), request.getReceiverAccount());
        validateReceiverAccount(request.getReceiverAccount(), request.getReceiverName(), request.getReceiverBank());

        // [동시성] 비관적 쓰기 락으로 송금 계좌 행을 잠근 채 조회한다.
        // 같은 계좌에 대한 동시 송금이 "잔액 확인 → 차감" 사이에 끼어들어 이중 차감/마이너스 잔액을
        // 만드는 race condition을, 차감 경로 자체에서 DB 레벨로 직렬화해 차단한다.
        Account senderAccount = accountRepository
                .findByAccountNumberAndUser_UserIdForUpdate(request.getSenderAccount(), sender.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        long amount;
        try {
            amount = request.getAmount().longValueExact();
        } catch (ArithmeticException e) {
            throw new CustomException(ErrorCode.INVALID_TRANSFER_AMOUNT);
        }

        // 잔액 확인 (락을 쥔 상태이므로 확인~차감이 원자적으로 보장됨)
        if (senderAccount.getBalance() < amount) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        Transaction.TransactionType type = determineTransactionType(request);
        String externalTransactionId = generateExternalTransactionId();

        // [원자성] 거래를 PENDING 으로 먼저 적재해 '시도'를 추적 가능한 상태로 만든다.
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .senderAccount(request.getSenderAccount())
                .receiverAccount(request.getReceiverAccount())
                .receiverName(request.getReceiverName())
                .receiverBank(request.getReceiverBank())
                .amount(request.getAmount())
                .memo(request.getMemo())
                .transactionType(type)
                .transactionStatus(Transaction.TransactionStatus.PENDING)
                .externalTransactionId(externalTransactionId)
                .requestedAt(LocalDateTime.now())
                .build();
        transaction = transactionRepository.save(transaction);

        // 계좌 잔액 차감
        long originalBalance = senderAccount.getBalance();
        senderAccount.setBalance(originalBalance - amount);
        accountRepository.save(senderAccount);

        log.info("계좌 잔액 차감 완료: 계좌={}, 송금금액={}",
                maskAccountNumber(senderAccount.getAccountNumber()), amount);

        // [보상 트랜잭션] 외부 송금 실패 시:
        //   1) 본 트랜잭션을 롤백시켜 잔액 차감을 자동 원복(= 보상)하고,
        //   2) 별도(REQUIRES_NEW) 감사 트랜잭션에 FAILED 이력을 남겨 실패 사실은 영속화한다.
        try {
            callExternalBankTransfer(transaction);
        } catch (RuntimeException e) {
            log.error("외부 송금 실패 → 롤백으로 잔액 원복, FAILED 이력 적재: 외부거래ID={}, 사유={}",
                    externalTransactionId, e.getMessage());
            transactionAuditService.recordFailure(sender.getUserId(), request, type, externalTransactionId, e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }

        // 외부 송금 성공 → 완료 처리
        transaction.setTransactionStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        log.info("송금 처리 완료: 거래ID={}, 금액={}",
                transaction.getTransactionId(), request.getAmount());

        TransferResponseDTO response = TransferResponseDTO.from(transaction);
        response.setMessage("송금이 완료되었습니다.");

        return response;
    }

    private String generateExternalTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private Transaction.TransactionType determineTransactionType(TransferRequestDTO request) {
        if (request.getPresetId() != null) {
            return Transaction.TransactionType.ONE_CLICK;
        } else if (request.getDelegationId() != null) {
            return Transaction.TransactionType.DELEGATION;
        }
        return Transaction.TransactionType.DIRECT;
    }
    
    private void validateReceiverAccount(String accountNumber, String receiverName, String bankName) {
        // 실제로는 외부 은행 API를 통해 계좌 유효성 검증
        if (accountNumber.length() < 10) {
            throw new CustomException(ErrorCode.INVALID_ACCOUNT_NUMBER);
        }
        
        log.info("수취 계좌 형식 검증: 계좌={}, 은행={}", maskAccountNumber(accountNumber), bankName);
    }
    
    private void validateTransferAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_TRANSFER_AMOUNT);
        }
        
        // 송금 한도 체크 (예: 1회 최대 500만원)
        BigDecimal maxAmount = new BigDecimal("5000000");
        if (amount.compareTo(maxAmount) > 0) {
            throw new CustomException(ErrorCode.TRANSFER_LIMIT_EXCEEDED);
        }
    }
    
    private void validateSelfTransfer(String senderAccount, String receiverAccount) {
        if (senderAccount.equals(receiverAccount)) {
            throw new CustomException(ErrorCode.SAME_ACCOUNT_TRANSFER);
        }
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        return "*".repeat(accountNumber.length() - 4) + accountNumber.substring(accountNumber.length() - 4);
    }
    
    /**
     * 외부 은행 송금 호출 (현재는 모킹).
     *
     * <p>한계(정직한 미완성):
     * <ul>
     *   <li>실제 망 연동이 아니라 {@link Thread#sleep}으로 지연만 흉내 내는 단방향 차감이다.
     *       수취 계좌의 잔액 증가(복식부기)는 외부 은행 소관이라 여기서는 표현되지 않는다.</li>
     *   <li>실패/타임아웃을 던지면 위의 보상 로직이 동작하도록 구조는 잡아두었으나,
     *       "외부는 성공했는데 우리 쪽 커밋이 실패"하는 분산 트랜잭션 불일치는 단일 DB 트랜잭션으로는
     *       완결할 수 없다. 운영에서는 Saga + Outbox + 정산(reconciliation) 배치가 정답이며, 이는 다음 과제.</li>
     * </ul>
     */
    private void callExternalBankTransfer(Transaction transaction) {
        try {
            Thread.sleep(1000); // 외부 API 호출 시뮬레이션
            log.info("외부 은행 송금 처리 완료: {}", transaction.getExternalTransactionId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
    
    public java.util.List<TransferResponseDTO> getTransferHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        java.util.List<Transaction> transactions = transactionRepository.findBySender_UserIdOrderByCreatedAtDesc(userId);
        
        return transactions.stream()
                .map(TransferResponseDTO::from)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public java.util.List<TransferResponseDTO> getDelegatedTransferHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 위임 송금 내역 조회 (delegationId가 있는 거래)
        java.util.List<Transaction> transactions = transactionRepository.findBySender_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(t -> t.getDelegation() != null)
                .collect(java.util.stream.Collectors.toList());
        
        return transactions.stream()
                .map(TransferResponseDTO::from)
                .collect(java.util.stream.Collectors.toList());
    }
    
}
