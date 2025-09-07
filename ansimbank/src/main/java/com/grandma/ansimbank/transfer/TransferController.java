package com.grandma.ansimbank.transfer;

import com.grandma.ansimbank.transfer.dto.TransferRequestDTO;
import com.grandma.ansimbank.transfer.dto.TransferResponseDTO;
import com.grandma.ansimbank.common.response.ApiCommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/remit")
@RequiredArgsConstructor
@Slf4j
public class TransferController {
    
    private final TransferService transferService;
    
    @PostMapping("/transfer")
    // TODO: JWT 토큰 도입 시 Authentication 파라미터 추가 필요
    // public ResponseEntity<ApiCommonResponse<TransferResponseDTO>> transfer(
    //         @Valid @RequestBody TransferRequestDTO request, 
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long authenticatedUserId = userPrincipal.getUserId();
    //     // request에서 senderId 제거하고 authenticatedUserId 사용
    public ResponseEntity<ApiCommonResponse<TransferResponseDTO>> transfer(
            @Valid @RequestBody TransferRequestDTO request) {
        
        log.info("송금 요청: 송금인ID={}, 송금액={}, 수취계좌={}", 
                request.getSenderId(), request.getAmount(), request.getReceiverAccount());
        
        TransferResponseDTO response = transferService.transfer(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiCommonResponse.success(response));
    }
    
    @GetMapping("/validate-account")
    public ResponseEntity<ApiCommonResponse<String>> validateAccount(
            @RequestParam String accountNumber,
            @RequestParam String bankCode) {
        
        // 실제로는 외부 은행 API를 통해 계좌 유효성 검증
        log.info("계좌 유효성 검증 요청: 계좌번호={}, 은행코드={}", accountNumber, bankCode);
        
        String accountHolderName = "홍길동"; // 가상 예금주명
        
        return ResponseEntity.ok(ApiCommonResponse.success(accountHolderName));
    }
    
    @GetMapping("/fee")
    public ResponseEntity<ApiCommonResponse<Integer>> getTransferFee(
            @RequestParam String senderBankCode,
            @RequestParam String receiverBankCode,
            @RequestParam int amount) {
        
        // 가상 수수료 계산
        int fee = calculateTransferFee(senderBankCode, receiverBankCode, amount);
        
        log.info("송금 수수료 조회: 송금은행={}, 수취은행={}, 금액={}, 수수료={}", 
                senderBankCode, receiverBankCode, amount, fee);
        
        return ResponseEntity.ok(ApiCommonResponse.success(fee));
    }
    
    @GetMapping("/history")
    // TODO: JWT 토큰 도입 시 userId 파라미터를 Authentication으로 교체
    // public ResponseEntity<ApiCommonResponse<java.util.List<TransferResponseDTO>>> getTransferHistory(
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<java.util.List<TransferResponseDTO>>> getTransferHistory(
            @RequestParam Long userId) {
        
        log.info("송금 내역 조회 요청: 사용자ID={}", userId);
        
        java.util.List<TransferResponseDTO> history = transferService.getTransferHistory(userId);
        
        return ResponseEntity.ok(ApiCommonResponse.success(history));
    }
    
    @PostMapping("/quick")
    // TODO: JWT 토큰 도입 시 Authentication 파라미터 추가 및 request의 senderId 제거
    // CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    // Long authenticatedUserId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<TransferResponseDTO>> quickTransfer(
            @Valid @RequestBody TransferRequestDTO request) {
        
        log.info("원클릭 송금 요청: 송금인ID={}, 프리셋ID={}", request.getSenderId(), request.getPresetId());
        
        TransferResponseDTO response = transferService.transfer(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiCommonResponse.success(response));
    }
    
    @PostMapping("/delegated/transfer")
    // TODO: JWT 토큰 도입 시 Authentication 파라미터 추가 및 request의 senderId 제거
    // CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    // Long authenticatedUserId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<TransferResponseDTO>> delegatedTransfer(
            @Valid @RequestBody TransferRequestDTO request) {
        
        log.info("위임 송금 요청: 송금인ID={}, 위임장ID={}", request.getSenderId(), request.getDelegationId());
        
        TransferResponseDTO response = transferService.transfer(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiCommonResponse.success(response));
    }
    
    @GetMapping("/delegated")
    // TODO: JWT 토큰 도입 시 userId 파라미터를 Authentication으로 교체
    // CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    // Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<java.util.List<TransferResponseDTO>>> getDelegatedTransfers(
            @RequestParam Long userId) {
        
        log.info("대리 송금 내역 조회: 사용자ID={}", userId);
        
        java.util.List<TransferResponseDTO> transfers = transferService.getDelegatedTransferHistory(userId);
        
        return ResponseEntity.ok(ApiCommonResponse.success(transfers));
    }

    private int calculateTransferFee(String senderBankCode, String receiverBankCode, int amount) {
        // 같은 은행이면 무료, 다른 은행이면 500원
        if (senderBankCode.equals(receiverBankCode)) {
            return 0;
        }
        return 500;
    }
}