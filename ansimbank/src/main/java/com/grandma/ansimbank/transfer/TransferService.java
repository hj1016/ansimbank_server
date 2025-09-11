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
    
    public TransferResponseDTO transfer(TransferRequestDTO request) {
        // 송금인 조회
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 송금인 계좌 검증 - 사용자 ID와 계좌번호로 검색
        Account senderAccount = accountRepository.findByAccountNumberAndUser_UserId(
                request.getSenderAccount(), sender.getUserId())
                .orElse(null);
        
        // 등록된 계좌가 없는 경우 에러
        if (senderAccount == null) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        
        // 송금 금액 검증
        validateTransferAmount(request.getAmount());
        
        // 자기 자신에게 송금 방지
        validateSelfTransfer(request.getSenderAccount(), request.getReceiverAccount());
        
        // 수취 계좌 검증 (실제로는 외부 은행 API 호출)
        validateReceiverAccount(request.getReceiverAccount(), request.getReceiverName(), request.getReceiverBank());
        
        // 잔액 확인 및 차감 처리
        if (senderAccount.getBalance() < request.getAmount().longValue()) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        
        // 계좌 잔액 차감
        long originalBalance = senderAccount.getBalance();
        senderAccount.setBalance(originalBalance - request.getAmount().longValue());
        accountRepository.save(senderAccount);
        
        log.info("계좌 잔액 차감 완료: 계좌번호={}, 기존잔액={}, 송금금액={}, 잔여잔액={}", 
                senderAccount.getAccountNumber(), 
                originalBalance, 
                request.getAmount().longValue(), 
                senderAccount.getBalance());

        // 송금 처리
        Transaction transaction = processTransfer(sender, request);
        
        log.info("송금 처리 완료: 거래ID={}, 송금인={}, 수취인={}, 금액={}", 
                transaction.getTransactionId(), sender.getName(), request.getReceiverName(), request.getAmount());
        
        TransferResponseDTO response = TransferResponseDTO.from(transaction);
        response.setMessage("송금이 완료되었습니다.");
        
        return response;
    }
    
    private Transaction processTransfer(User sender, TransferRequestDTO request) {
        // 외부 거래 ID 생성
        String externalTransactionId = "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
        
        // 거래 생성
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .senderAccount(request.getSenderAccount())
                .receiverAccount(request.getReceiverAccount())
                .receiverName(request.getReceiverName())
                .receiverBank(request.getReceiverBank())
                .amount(request.getAmount())
                .memo(request.getMemo())
                .transactionType(determineTransactionType(request))
                .transactionStatus(Transaction.TransactionStatus.PENDING)
                .externalTransactionId(externalTransactionId)
                .requestedAt(LocalDateTime.now())
                .build();
        
        // 거래 저장
        transaction = transactionRepository.save(transaction);
        
        // 가상 처리 (실제로는 외부 은행 API 호출)
        simulateExternalBankTransfer(transaction);
        
        // 거래 완료 처리
        transaction.setTransactionStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        
        return transactionRepository.save(transaction);
    }
    
    private Transaction.TransactionType determineTransactionType(TransferRequestDTO request) {
        if (request.getPresetId() != null) {
            return Transaction.TransactionType.ONE_CLICK;
        } else if (request.getDelegationId() != null) {
            return Transaction.TransactionType.DELEGATION;
        }
        return Transaction.TransactionType.DIRECT;
    }
    
    private void validateSenderAccount(String accountNumber) {
        // 송금인 계좌 기본 유효성 검증
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_ACCOUNT_NUMBER);
        }
        
        if (accountNumber.length() < 10) {
            throw new CustomException(ErrorCode.INVALID_ACCOUNT_NUMBER);
        }
        
        log.info("송금인 계좌 검증 통과: 계좌번호={}", accountNumber);
    }
    
    private void validateReceiverAccount(String accountNumber, String receiverName, String bankName) {
        // 실제로는 외부 은행 API를 통해 계좌 유효성 검증
        if (accountNumber.length() < 10) {
            throw new CustomException(ErrorCode.INVALID_ACCOUNT_NUMBER);
        }
        
        log.info("수취 계좌 검증: 계좌번호={}, 예금주={}, 은행={}", accountNumber, receiverName, bankName);
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
    
    private void simulateExternalBankTransfer(Transaction transaction) {
        // 가상 외부 은행 송금 처리 (실제로는 외부 API 호출)
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
    
    private String extractBankCode(String accountNumber) {
        return "001"; // 임시 은행코드
    }
}