package com.grandma.ansimbank.preset;

import com.grandma.ansimbank.common.response.ApiCommonResponse;
import com.grandma.ansimbank.preset.dto.PresetRequestDTO;
import com.grandma.ansimbank.preset.dto.PresetResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preset")
@RequiredArgsConstructor
@Slf4j
public class PresetController {
    
    private final PresetService presetService;
    
    @GetMapping
    // TODO: JWT 토큰 도입 시 userId 파라미터를 Authentication으로 교체
    // public ResponseEntity<ApiCommonResponse<List<PresetResponseDTO>>> getPresets(
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<List<PresetResponseDTO>>> getPresets(
            @RequestParam Long userId) {
        
        log.info("원클릭 프리셋 조회: 사용자ID={}", userId);
        
        List<PresetResponseDTO> presets = presetService.getPresetsByUser(userId);
        
        return ResponseEntity.ok(ApiCommonResponse.success(presets));
    }
    
    @PostMapping
    // TODO: JWT 토큰 도입 시 Authentication 파라미터 추가 및 request의 userId 제거
    // public ResponseEntity<ApiCommonResponse<PresetResponseDTO>> createPreset(
    //         @Valid @RequestBody PresetRequestDTO request,
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    //     // request에서 userId 제거하고 authenticatedUserId 사용
    public ResponseEntity<ApiCommonResponse<PresetResponseDTO>> createPreset(
            @Valid @RequestBody PresetRequestDTO request) {
        
        log.info("원클릭 프리셋 생성: 사용자ID={}, 이름={}", request.getUserId(), request.getPresetName());
        
        PresetResponseDTO preset = presetService.createPreset(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiCommonResponse.success(preset));
    }
    
    @PutMapping("/{presetId}")
    // TODO: JWT 토큰 도입 시 Authentication 파라미터 추가 및 request의 userId 제거
    // 수정 권한 검증을 위해 JWT에서 사용자 ID를 추출하여 프리셋 소유자와 비교
    // public ResponseEntity<ApiCommonResponse<PresetResponseDTO>> updatePreset(
    //         @PathVariable Long presetId,
    //         @Valid @RequestBody PresetRequestDTO request,
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<PresetResponseDTO>> updatePreset(
            @PathVariable Long presetId,
            @Valid @RequestBody PresetRequestDTO request) {
        
        log.info("원클릭 프리셋 수정: 프리셋ID={}", presetId);
        
        PresetResponseDTO preset = presetService.updatePreset(presetId, request);
        
        return ResponseEntity.ok(ApiCommonResponse.success(preset));
    }
    
    @DeleteMapping("/{presetId}")
    // TODO: JWT 토큰 도입 시 Authentication 파라미터 추가
    // 삭제 권한 검증을 위해 JWT에서 사용자 ID를 추출하여 프리셋 소유자와 비교
    // public ResponseEntity<ApiCommonResponse<String>> deletePreset(
    //         @PathVariable Long presetId,
    //         Authentication authentication) {
    //     CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
    //     Long userId = userPrincipal.getUserId();
    public ResponseEntity<ApiCommonResponse<String>> deletePreset(
            @PathVariable Long presetId) {
        
        log.info("원클릭 프리셋 삭제: 프리셋ID={}", presetId);
        
        presetService.deletePreset(presetId);
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}