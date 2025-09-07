package com.grandma.ansimbank.transfer.dto;

import com.grandma.ansimbank.transaction.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDTO {
    
    private Long transactionId;
    private String senderAccount;
    private String receiverAccount;
    private String receiverName;
    private String receiverBank;
    private BigDecimal amount;
    private String memo;
    private Transaction.TransactionType transactionType;
    private Transaction.TransactionStatus transactionStatus;
    private String externalTransactionId;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private String message;
    
    public static TransferResponseDTO from(com.grandma.ansimbank.transaction.Transaction transaction) {
        return TransferResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .senderAccount(transaction.getSenderAccount())
                .receiverAccount(transaction.getReceiverAccount())
                .receiverName(transaction.getReceiverName())
                .receiverBank(transaction.getReceiverBank())
                .amount(transaction.getAmount())
                .memo(transaction.getMemo())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getTransactionStatus())
                .externalTransactionId(transaction.getExternalTransactionId())
                .requestedAt(transaction.getRequestedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}