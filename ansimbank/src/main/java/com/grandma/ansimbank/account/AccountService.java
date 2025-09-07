package com.grandma.ansimbank.account;

import com.grandma.ansimbank.account.dto.AccountLinkRequestDTO;
import com.grandma.ansimbank.account.dto.AccountResponseDTO;
import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.exception.CustomException;
import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    
    public List<AccountResponseDTO> getAccountsByUser(Long userId) {
        List<Account> accounts = accountRepository.findByUser_UserIdAndIsActiveTrue(userId);
        
        return accounts.stream()
                .map(AccountResponseDTO::from)
                .collect(Collectors.toList());
    }
    
    public AccountResponseDTO linkAccount(AccountLinkRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 중복 계좌 체크
        if (accountRepository.existsByAccountNumberAndBankCode(request.getAccountNumber(), request.getBankCode())) {
            throw new CustomException(ErrorCode.ACCOUNT_ALREADY_LINKED);
        }
        
        // 계좌 연동 한도 체크 (예: 사용자당 최대 10개)
        long accountCount = accountRepository.countByUserAndIsActiveTrue(user);
        if (accountCount >= 10) {
            throw new CustomException(ErrorCode.ACCOUNT_LINK_LIMIT_EXCEEDED);
        }
        
        // 가상 계좌 검증 (실제로는 외부 은행 API 호출)
        validateAccountWithBank(request.getBankCode(), request.getAccountNumber(), request.getAccountHolder());
        
        // 주계좌 설정 시 기존 주계좌 해제
        if (request.getIsPrimary()) {
            accountRepository.findByUser_UserIdAndIsPrimaryTrueAndIsActiveTrue(user.getUserId())
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        accountRepository.save(existingPrimary);
                    });
        }
        
        Account account = Account.builder()
                .user(user)
                .bankCode(request.getBankCode())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountHolder(request.getAccountHolder())
                .isPrimary(request.getIsPrimary())
                .build();
        
        account = accountRepository.save(account);
        
        log.info("계좌 연동 완료: 사용자={}, 은행={}, 계좌번호={}", 
                user.getName(), request.getBankName(), request.getAccountNumber());
        
        return AccountResponseDTO.from(account);
    }
    
    public void unlinkAccount(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        if (!account.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCOUNT);
        }
        
        account.setIsActive(false);
        accountRepository.save(account);
        
        log.info("계좌 연동 해제 완료: 계좌ID={}", accountId);
    }
    
    public void setPrimaryAccount(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        if (!account.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCOUNT);
        }
        
        // 기존 주계좌 해제
        accountRepository.findByUser_UserIdAndIsPrimaryTrueAndIsActiveTrue(userId)
                .ifPresent(existingPrimary -> {
                    existingPrimary.setIsPrimary(false);
                    accountRepository.save(existingPrimary);
                });
        
        // 새 주계좌 설정
        account.setIsPrimary(true);
        accountRepository.save(account);
        
        log.info("주계좌 설정 완료: 계좌ID={}", accountId);
    }
    
    private void validateAccountWithBank(String bankCode, String accountNumber, String accountHolder) {
        // 가상 은행 API 호출 (실제로는 외부 은행 시스템과 연동)
        log.info("은행 계좌 검증: 은행코드={}, 계좌번호={}, 예금주={}", bankCode, accountNumber, accountHolder);
        
        // 간단한 검증 로직
        if (accountNumber.length() < 10) {
            throw new CustomException(ErrorCode.INVALID_ACCOUNT_NUMBER);
        }
        
        // 가상 검증 성공
        log.info("계좌 검증 성공");
    }
}