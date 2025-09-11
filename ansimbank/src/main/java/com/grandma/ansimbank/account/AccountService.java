package com.grandma.ansimbank.account;

import com.grandma.ansimbank.account.dto.AccountLinkRequestDTO;
import com.grandma.ansimbank.account.dto.AccountResponseDTO;
import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.exception.CustomException;
import com.grandma.ansimbank.config.CodefConfig;
import com.grandma.ansimbank.external.CodefService;
import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CodefService codefService;
    private final CodefConfig codefConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<AccountResponseDTO> getAccountsByUser(Long userId) {
        List<Account> accounts = accountRepository.findByUser_UserIdAndIsActiveTrue(userId);
        
        return accounts.stream()
                .map(AccountResponseDTO::from)
                .collect(Collectors.toList());
    }
    
    public AccountResponseDTO linkAccount(AccountLinkRequestDTO request) {
        // TODO: JWT 통합 후 변경 예정
        // JWT에서 현재 로그인한 사용자 정보를 가져와 해당 사용자가 accountHolder가 되도록 수정
        // 현재: userId 파라미터로 사용자 조회
        // 변경 후: SecurityContextHolder에서 JWT 토큰의 사용자 정보 추출
        // Example: Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //         String currentUserId = auth.getName(); // or custom UserDetails
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 계좌 연동 한도 체크 (예: 사용자당 최대 10개)
        long accountCount = accountRepository.countByUserAndIsActiveTrue(user);
        if (accountCount >= 10) {
            throw new CustomException(ErrorCode.ACCOUNT_LINK_LIMIT_EXCEEDED);
        }
        
        Account account;
        
        if ("CODEF".equals(request.getLinkType())) {
            // CODEF 방식 - 실제 은행 계좌 연동
            account = linkAccountWithCodef(request, user);
        } else if ("MANUAL".equals(request.getLinkType())) {
            // MANUAL 방식 - 수동 입력
            account = linkAccountManually(request, user);
        } else {
            throw new CustomException(ErrorCode.INVALID_REQUEST_DATA);
        }
        
        // 주계좌 설정 시 기존 주계좌 해제
        if (request.getIsPrimary()) {
            accountRepository.findByUser_UserIdAndIsPrimaryTrueAndIsActiveTrue(user.getUserId())
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        accountRepository.save(existingPrimary);
                    });
        }
        
        account = accountRepository.save(account);
        
        log.info("계좌 연동 완료: 사용자={}, 은행={}, 방식={}", 
                user.getName(), request.getBankName(), request.getLinkType());
        
        return AccountResponseDTO.from(account);
    }

    /**
     * CODEF 방식 계좌 연동
     */
    private Account linkAccountWithCodef(AccountLinkRequestDTO request, User user) {
        try {
            // 1. CODEF ConnectedId 생성
            String connectedId = codefService.createConnectedId(
                    codefConfig.getBankCode(request.getBankName()),
                    request.getBankUserId(),
                    request.getBankUserPassword()
            );
            
            // 2. 계좌 목록 조회
            String accountListResponse = codefService.getAccountList(
                    connectedId, 
                    codefConfig.getBankCode(request.getBankName())
            );
            
            // 3. 응답에서 실제 계좌 정보 추출
            Map<String, Object> accountData = parseAccountDataFromCodefResponse(accountListResponse);
            String accountNumber = (String) accountData.get("accountNumber");
            
            // CODEF API는 보안상 예금주 정보를 제공하지 않으므로, 현재 사용자를 예금주로 설정
            // JWT 통합 후에는 토큰에서 가져온 사용자 이름을 사용
            String accountHolder = user.getName(); // 현재는 User 엔티티의 name 필드 사용
            
            // 4. 중복 계좌 체크
            if (accountRepository.existsByAccountNumberAndBankCode(accountNumber, request.getBankCode())) {
                throw new CustomException(ErrorCode.ACCOUNT_ALREADY_LINKED);
            }
            
            // 5. Account 엔티티 생성
            return Account.builder()
                    .user(user)
                    .bankCode(request.getBankCode())
                    .bankName(request.getBankName())
                    .accountNumber(accountNumber)
                    .accountHolder(accountHolder)
                    .isPrimary(request.getIsPrimary())
                    .connectedId(connectedId)
                    .linkType("CODEF")
                    .balance(7358950L) // 더미 잔액 데이터
                    .build();
                    
        } catch (Exception e) {
            log.error("CODEF 계좌 연동 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    /**
     * 수동 방식 계좌 연동
     */
    private Account linkAccountManually(AccountLinkRequestDTO request, User user) {
        // 필수 필드 검증
        if (request.getAccountNumber() == null || request.getAccountHolder() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_DATA);
        }
        
        // 중복 계좌 체크
        if (accountRepository.existsByAccountNumberAndBankCode(request.getAccountNumber(), request.getBankCode())) {
            throw new CustomException(ErrorCode.ACCOUNT_ALREADY_LINKED);
        }
        
        // 가상 계좌 검증 (실제로는 외부 은행 API 호출)
        validateAccountWithBank(request.getBankCode(), request.getAccountNumber(), request.getAccountHolder());
        
        return Account.builder()
                .user(user)
                .bankCode(request.getBankCode())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountHolder(request.getAccountHolder())
                .isPrimary(request.getIsPrimary())
                .linkType("MANUAL")
                .balance(7358950L) // 더미 잔액 데이터
                .build();
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
    
    /**
     * CODEF API 응답에서 실제 계좌 정보를 파싱하는 메서드
     * 실제 응답 구조: {"result": {...}, "data": [{"resAccount": "계좌번호", "resAccountName": "계좌명", ...}]}
     * 참고: CODEF API는 보안상 예금주 정보(resAccountHolder)를 제공하지 않음
     */
    private Map<String, Object> parseAccountDataFromCodefResponse(String accountListResponse) {
        try {
            Map<String, Object> response = objectMapper.readValue(accountListResponse, Map.class);
            Map<String, Object> result = new HashMap<>();
            
            // data 객체에서 resDepositTrust 배열의 첫 번째 계좌 정보 추출
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data != null) {
                List<Map<String, Object>> depositList = (List<Map<String, Object>>) data.get("resDepositTrust");
                if (depositList != null && !depositList.isEmpty()) {
                    Map<String, Object> firstAccount = depositList.get(0);
                    
                    // CODEF API 실제 응답 필드명 - resAccountDisplay는 하이픈 포함된 형태
                    String accountNumber = (String) firstAccount.get("resAccountDisplay");
                    String accountName = (String) firstAccount.get("resAccountName");
                    
                    result.put("accountNumber", accountNumber);
                    result.put("accountName", accountName);
                    
                    log.info("CODEF 계좌 정보 파싱 성공: 계좌번호={}, 계좌명={}", accountNumber, accountName);
                } else {
                    log.warn("CODEF 응답에 resDepositTrust 데이터가 없습니다.");
                    // 기본값 설정
                    result.put("accountNumber", "1234-56-789012");
                    result.put("accountName", "기본계좌");
                }
            } else {
                log.warn("CODEF 응답에 data 객체가 없습니다.");
                // 기본값 설정
                result.put("accountNumber", "1234-56-789012");
                result.put("accountName", "기본계좌");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("CODEF 계좌 정보 파싱 실패: {}", e.getMessage());
            // 파싱 실패 시 기본값 반환
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("accountNumber", "1234-56-789012");
            fallback.put("accountName", "기본계좌");
            return fallback;
        }
    }
}