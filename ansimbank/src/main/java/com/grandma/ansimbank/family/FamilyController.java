package com.grandma.ansimbank.family;

import com.grandma.ansimbank.common.response.ApiCommonResponse;
import com.grandma.ansimbank.family.dto.FamilyConnectionRequestDTO;
import com.grandma.ansimbank.family.dto.FamilyConnectionResponseDTO;
import com.grandma.ansimbank.family.dto.FamilyConnectionStatusUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
@Slf4j
// JWT 통합 후에는 @PreAuthorize 또는 Spring Security 설정으로 인증/권한 체크 필요
// 현재: userId 파라미터로 사용자 식별
// 변경 후: JWT 토큰에서 자동으로 사용자 정보 추출하여 보안성 강화
public class FamilyController {
    
    private final FamilyService familyService;
    
    @GetMapping("/connections")
    // TODO: JWT 토큰 도입 시 userId 파라미터를 Authentication으로 교체
    // public ResponseEntity<ApiCommonResponse<List<FamilyConnectionResponseDTO>>> getFamilyConnections(
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<List<FamilyConnectionResponseDTO>>> getFamilyConnections(
            @RequestParam Long userId) {
        
        log.info("가족 연동 현황 조회: 사용자ID={}", userId);
        
        List<FamilyConnectionResponseDTO> connections = familyService.getFamilyConnections(userId);
        
        return ResponseEntity.ok(ApiCommonResponse.success(connections));
    }
    

    @PostMapping("/connection/request")
    // TODO: JWT 토큰 도입 시 Authentication 파라미터 추가 및 request의 requesterId 제거
    // public ResponseEntity<ApiCommonResponse<FamilyConnectionResponseDTO>> requestFamilyConnection(
    //         @Valid @RequestBody FamilyConnectionRequestDTO request,
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long requesterId = userPrincipal.getUserId();
    //     // request에서 requesterId 제거하고 authenticatedUserId 사용
    //     // 이렇게 하면 토큰의 사용자만 요청을 보낼 수 있어 보안성 향상
    public ResponseEntity<ApiCommonResponse<FamilyConnectionResponseDTO>> requestFamilyConnection(
            @Valid @RequestBody FamilyConnectionRequestDTO request) {
        
        log.info("가족 연동 요청: 요청자ID={}, 대상전화번호={}", request.getRequesterId(), request.getTargetPhoneNumber());
        
        FamilyConnectionResponseDTO connection = familyService.requestFamilyConnection(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiCommonResponse.success(connection));
    }
    
    @PutMapping("/connection/{connectionId}/status")
    // TODO: JWT 토큰 도입 시 Authentication 파라미터 추가 및 request의 userId 제거
    // public ResponseEntity<ApiCommonResponse<FamilyConnectionResponseDTO>> updateConnectionStatus(
    //         @PathVariable Long connectionId,
    //         @Valid @RequestBody FamilyConnectionStatusUpdateDTO request,
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    //     // request에서 userId 제거하고 authenticatedUserId 사용
    //     // 승인/거부 권한 검증이 JWT 기반으로 자동화됨
    public ResponseEntity<ApiCommonResponse<FamilyConnectionResponseDTO>> updateConnectionStatus(
            @PathVariable Long connectionId,
            @Valid @RequestBody FamilyConnectionStatusUpdateDTO request) {
        
        log.info("가족 연동 상태 변경: 연결ID={}, 상태={}", connectionId, request.getStatus());
        
        FamilyConnectionResponseDTO connection = familyService.updateConnectionStatus(connectionId, request);
        
        return ResponseEntity.ok(ApiCommonResponse.success(connection));
    }
    
    @DeleteMapping("/connection/{connectionId}")
    // TODO: JWT 토큰 도입 시 userId 파라미터를 Authentication으로 교체
    // public ResponseEntity<ApiCommonResponse<String>> deleteFamilyConnection(
    //         @PathVariable Long connectionId,
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<String>> deleteFamilyConnection(
            @PathVariable Long connectionId,
            @RequestParam Long userId) {
        
        log.info("가족 연동 해제: 연결ID={}, 요청자ID={}", connectionId, userId);
        
        familyService.deleteFamilyConnection(connectionId, userId);
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @GetMapping("/connection/stats")
    // TODO: JWT 토큰 도입 시 userId 파라미터를 Authentication으로 교체
    // public ResponseEntity<ApiCommonResponse<Map<String, Object>>> getFamilyConnectionStats(
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<java.util.Map<String, Object>>> getFamilyConnectionStats(
            @RequestParam Long userId) {
        
        log.info("가족 연동 통계 조회: 사용자ID={}", userId);
        
        long connectedCount = familyService.getConnectedFamilyCount(userId);
        long pendingCount = familyService.getPendingConnectionCount(userId);
        
        java.util.Map<String, Object> stats = java.util.Map.of(
                "connectedCount", connectedCount,
                "pendingCount", pendingCount
        );
        
        return ResponseEntity.ok(ApiCommonResponse.success(stats));
    }
}