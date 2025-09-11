package com.grandma.ansimbank.account.dto;

import com.grandma.ansimbank.account.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {
    
    private Long accountId;
    private Long userId;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private Boolean isPrimary;
    private Long balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static AccountResponseDTO from(Account account) {
        return AccountResponseDTO.builder()
                .accountId(account.getAccountId())
                .userId(account.getUser().getUserId())
                .bankCode(account.getBankCode())
                .bankName(account.getBankName())
                .accountNumber(account.getAccountNumber())
                .accountHolder(account.getAccountHolder())
                .isPrimary(account.getIsPrimary())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}