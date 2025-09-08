package com.grandma.ansimbank.account;

import com.grandma.ansimbank.account.dto.AccountLinkRequestDTO;
import com.grandma.ansimbank.account.dto.AccountResponseDTO;
import com.grandma.ansimbank.common.response.ApiCommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
// JWT 통합 후에는 @PreAuthorize 또는 Spring Security 설정으로 인증/권한 체크 필요
// 현재: userId 파라미터로 사용자 식별
// 변경 후: JWT 토큰에서 자동으로 사용자 정보 추출하여 보안성 강화
public class AccountController {
    
    private final AccountService accountService;
    
    @GetMapping
    // TODO: JWT 토큰 도입 시 userId 파라미터를 Authentication으로 교체
    // public ResponseEntity<ApiCommonResponse<List<AccountResponseDTO>>> getAccounts(
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<List<AccountResponseDTO>>> getAccounts(
            @RequestParam Long userId) {
        
        log.info("계좌 목록 조회: 사용자ID={}", userId);
        
        List<AccountResponseDTO> accounts = accountService.getAccountsByUser(userId);
        
        return ResponseEntity.ok(ApiCommonResponse.success(accounts));
    }
    
    @PostMapping("/link")
    // TODO: JWT 토큰 도입 시 Authentication 파라미터 추가 및 request의 userId 제거
    // CODEF 연동 시 JWT의 사용자 정보와 실제 은행 계좌 소유자가 일치하는지 추가 검증 필요
    // public ResponseEntity<ApiCommonResponse<AccountResponseDTO>> linkAccount(
    //         @Valid @RequestBody AccountLinkRequestDTO request,
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    //     // request에서 userId 제거하고 authenticatedUserId 사용
    //     // JWT 사용자 이름과 CODEF에서 조회한 계좌 소유자 이름 비교 검증 로직 추가
    public ResponseEntity<ApiCommonResponse<AccountResponseDTO>> linkAccount(
            @Valid @RequestBody AccountLinkRequestDTO request) {
        
        log.info("계좌 연동: 사용자ID={}, 계좌번호={}", request.getUserId(), request.getAccountNumber());
        
        AccountResponseDTO account = accountService.linkAccount(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiCommonResponse.success(account));
    }
    
    @DeleteMapping("/{accountId}")
    // TODO: JWT 토큰 도입 시 userId 파라미터를 Authentication으로 교체
    // public ResponseEntity<ApiCommonResponse<String>> unlinkAccount(
    //         @PathVariable Long accountId,
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<String>> unlinkAccount(
            @PathVariable Long accountId,
            @RequestParam Long userId) {
        
        log.info("계좌 연동 해제: 계좌ID={}, 사용자ID={}", accountId, userId);
        
        accountService.unlinkAccount(accountId, userId);
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @PutMapping("/{accountId}/primary")
    // TODO: JWT 토큰 도입 시 userId 파라미터를 Authentication으로 교체
    // public ResponseEntity<ApiCommonResponse<String>> setPrimaryAccount(
    //         @PathVariable Long accountId,
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<String>> setPrimaryAccount(
            @PathVariable Long accountId,
            @RequestParam Long userId) {
        
        log.info("주계좌 설정: 계좌ID={}, 사용자ID={}", accountId, userId);
        
        accountService.setPrimaryAccount(accountId, userId);
        
        return ResponseEntity.ok(ApiCommonResponse.success("주계좌로 설정되었습니다."));
    }
}