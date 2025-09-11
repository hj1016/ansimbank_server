package com.grandma.ansimbank.account;

import com.grandma.ansimbank.account.dto.AccountLinkRequestDTO;
import com.grandma.ansimbank.account.dto.AccountResponseDTO;
import com.grandma.ansimbank.common.response.ApiCommonResponse;
import com.grandma.ansimbank.common.security.services.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    
    private final AccountService accountService;
    
    @GetMapping
    public ResponseEntity<ApiCommonResponse<List<AccountResponseDTO>>> getAccounts(
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
        log.info("계좌 목록 조회: 사용자ID={}", userId);
        
        List<AccountResponseDTO> accounts = accountService.getAccountsByUser(userId);
        
        return ResponseEntity.ok(ApiCommonResponse.success(accounts));
    }
    
    @PostMapping("/link")
    public ResponseEntity<ApiCommonResponse<AccountResponseDTO>> linkAccount(
            @Valid @RequestBody AccountLinkRequestDTO request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
        request.setUserId(userId);
        
        log.info("계좌 연동: 사용자ID={}, 계좌번호={}", userId, request.getAccountNumber());
        
        AccountResponseDTO account = accountService.linkAccount(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiCommonResponse.success(account));
    }
    
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiCommonResponse<String>> unlinkAccount(
            @PathVariable Long accountId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
        log.info("계좌 연동 해제: 계좌ID={}, 사용자ID={}", accountId, userId);
        
        accountService.unlinkAccount(accountId, userId);
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @PutMapping("/{accountId}/primary")
    public ResponseEntity<ApiCommonResponse<String>> setPrimaryAccount(
            @PathVariable Long accountId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
        log.info("주계좌 설정: 계좌ID={}, 사용자ID={}", accountId, userId);
        
        accountService.setPrimaryAccount(accountId, userId);
        
        return ResponseEntity.ok(ApiCommonResponse.success("주계좌로 설정되었습니다."));
    }
}