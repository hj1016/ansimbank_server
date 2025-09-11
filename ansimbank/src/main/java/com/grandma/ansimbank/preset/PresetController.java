package com.grandma.ansimbank.preset;

import com.grandma.ansimbank.common.response.ApiCommonResponse;
import com.grandma.ansimbank.preset.dto.PresetRequestDTO;
import com.grandma.ansimbank.preset.dto.PresetResponseDTO;
import com.grandma.ansimbank.common.security.services.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/preset")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class PresetController {

    private final PresetService presetService;

    @GetMapping
    public ResponseEntity<ApiCommonResponse<List<PresetResponseDTO>>> getPresets(
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("원클릭 프리셋 조회: 사용자ID={}", userId);

        List<PresetResponseDTO> presets = presetService.getPresetsByUser(userId);

        return ResponseEntity.ok(ApiCommonResponse.success(presets));
    }

    @PostMapping
    public ResponseEntity<ApiCommonResponse<PresetResponseDTO>> createPreset(
            @Valid @RequestBody PresetRequestDTO request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("원클릭 프리셋 생성: 사용자ID={}, 이름={}", userId, request.getPresetName());

        PresetResponseDTO preset = presetService.createPreset(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiCommonResponse.success(preset));
    }

    @PutMapping("/{presetId}")
    public ResponseEntity<ApiCommonResponse<PresetResponseDTO>> updatePreset(
            @PathVariable Long presetId,
            @Valid @RequestBody PresetRequestDTO request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("원클릭 프리셋 수정: 프리셋ID={}, 사용자ID={}", presetId, userId);

        PresetResponseDTO preset = presetService.updatePreset(presetId, request, userId);

        return ResponseEntity.ok(ApiCommonResponse.success(preset));
    }

    @DeleteMapping("/{presetId}")
    public ResponseEntity<ApiCommonResponse<String>> deletePreset(
            @PathVariable Long presetId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("원클릭 프리셋 삭제: 프리셋ID={}, 사용자ID={}", presetId, userId);

        presetService.deletePreset(presetId, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}