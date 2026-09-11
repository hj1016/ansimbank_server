package com.grandma.ansimbank.transfer;

import com.grandma.ansimbank.transaction.Transaction;
import com.grandma.ansimbank.transaction.TransactionRepository;
import com.grandma.ansimbank.transfer.dto.TransferRequestDTO;
import com.grandma.ansimbank.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 송금 실패 이력을 영속화하는 감사(audit) 서비스.
 *
 * <p>핵심은 {@link Propagation#REQUIRES_NEW} 이다. 외부 송금이 실패하면 송금 트랜잭션 전체가
 * 롤백되어 잔액 차감은 안전하게 원복되지만, 그 롤백과 함께 PENDING 거래 기록도 사라진다.
 * 그러면 "실패가 있었다"는 사실 자체가 DB에서 증발한다. 별도 트랜잭션으로 FAILED 행을
 * 독립 INSERT 해두면, 본 트랜잭션이 롤백되더라도 실패 이력은 커밋되어 남는다.
 *
 * <p>금융에서 "되돌렸다"는 것과 "되돌린 사실을 남긴다"는 것은 다른 요구사항이다.
 * 보상(잔액 원복)은 트랜잭션 롤백이 처리하고, 감사 추적성은 이 서비스가 책임진다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAuditService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long senderId,
                              TransferRequestDTO request,
                              Transaction.TransactionType type,
                              String externalTransactionId,
                              String reason) {
        Transaction failed = Transaction.builder()
                .sender(userRepository.getReferenceById(senderId))
                .senderAccount(request.getSenderAccount())
                .receiverAccount(request.getReceiverAccount())
                .receiverName(request.getReceiverName())
                .receiverBank(request.getReceiverBank())
                .amount(request.getAmount())
                .memo("[송금 실패] " + reason)
                .transactionType(type)
                .transactionStatus(Transaction.TransactionStatus.FAILED)
                .externalTransactionId(externalTransactionId)
                .requestedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(failed);
        log.warn("송금 실패 이력 적재: 송금인ID={}, 외부거래ID={}, 사유={}", senderId, externalTransactionId, reason);
    }
}
