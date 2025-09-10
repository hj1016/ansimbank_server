package com.grandma.ansimbank.family;

import com.grandma.ansimbank.common.response.ApiCommonResponse;
import com.grandma.ansimbank.common.security.services.UserPrincipal;
import com.grandma.ansimbank.family.dto.FamilyConnectionRequestDTO;
import com.grandma.ansimbank.family.dto.FamilyConnectionResponseDTO;
import com.grandma.ansimbank.family.dto.FamilyConnectionStatusUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<ApiCommonResponse<List<FamilyConnectionResponseDTO>>> getFamilyConnections(
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
        log.info("가족 연동 현황 조회: 사용자ID={}", userId);
        
        List<FamilyConnectionResponseDTO> connections = familyService.getFamilyConnections(userId);
        
        return ResponseEntity.ok(ApiCommonResponse.success(connections));
    }
    

    @PostMapping("/connection/request")
    public ResponseEntity<ApiCommonResponse<FamilyConnectionResponseDTO>> requestFamilyConnection(
            @Valid @RequestBody FamilyConnectionRequestDTO request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long requesterId = userPrincipal.getId();
        
        // request에서 requesterId를 인증된 사용자 ID로 설정
        request.setRequesterId(requesterId);
        
        log.info("가족 연동 요청: 요청자ID={}, 대상전화번호={}", requesterId, request.getTargetPhoneNumber());
        
        FamilyConnectionResponseDTO connection = familyService.requestFamilyConnection(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiCommonResponse.success(connection));
    }
    
    @PutMapping("/connection/{connectionId}/status")
    public ResponseEntity<ApiCommonResponse<FamilyConnectionResponseDTO>> updateConnectionStatus(
            @PathVariable Long connectionId,
            @Valid @RequestBody FamilyConnectionStatusUpdateDTO request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
        // request에서 userId를 인증된 사용자 ID로 설정
        request.setUserId(userId);
        
        log.info("가족 연동 상태 변경: 연결ID={}, 상태={}, 사용자ID={}", connectionId, request.getStatus(), userId);
        
        FamilyConnectionResponseDTO connection = familyService.updateConnectionStatus(connectionId, request);
        
        return ResponseEntity.ok(ApiCommonResponse.success(connection));
    }
    
    @DeleteMapping("/connection/{connectionId}")
    public ResponseEntity<ApiCommonResponse<String>> deleteFamilyConnection(
            @PathVariable Long connectionId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
        log.info("가족 연동 해제: 연결ID={}, 요청자ID={}", connectionId, userId);
        
        familyService.deleteFamilyConnection(connectionId, userId);
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @GetMapping("/connection/stats")
    public ResponseEntity<ApiCommonResponse<java.util.Map<String, Object>>> getFamilyConnectionStats(
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
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