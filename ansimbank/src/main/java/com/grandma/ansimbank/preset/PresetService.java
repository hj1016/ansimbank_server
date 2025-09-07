package com.grandma.ansimbank.preset;

import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.exception.CustomException;
import com.grandma.ansimbank.preset.dto.PresetRequestDTO;
import com.grandma.ansimbank.preset.dto.PresetResponseDTO;
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
public class PresetService {
    
    private final OneClickPresetRepository presetRepository;
    private final UserRepository userRepository;
    
    public List<PresetResponseDTO> getPresetsByUser(Long userId) {
        List<OneClickPreset> presets = presetRepository.findByUser_UserIdAndIsActiveTrueOrderByDisplayOrderAsc(userId);
        
        return presets.stream()
                .map(PresetResponseDTO::from)
                .collect(Collectors.toList());
    }
    
    public PresetResponseDTO createPreset(PresetRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 프리셋 개수 한도 체크 (예: 사용자당 최대 20개)
        long presetCount = presetRepository.countActivePresetsByUser(user);
        if (presetCount >= 20) {
            throw new CustomException(ErrorCode.PRESET_LIMIT_EXCEEDED);
        }
        
        // 중복 이름 체크
        List<OneClickPreset> existingPresets = presetRepository.findByUserAndIsActiveTrueOrderByDisplayOrderAsc(user);
        boolean duplicateName = existingPresets.stream()
                .anyMatch(p -> p.getPresetName().equals(request.getPresetName()));
        if (duplicateName) {
            throw new CustomException(ErrorCode.DUPLICATE_PRESET_NAME);
        }
        
        // 표시 순서 자동 설정
        Integer maxDisplayOrder = presetRepository.findMaxDisplayOrderByUser(user);
        int nextDisplayOrder = maxDisplayOrder != null ? maxDisplayOrder + 1 : 1;
        
        OneClickPreset preset = OneClickPreset.builder()
                .user(user)
                .presetName(request.getPresetName())
                .receiverAccount(request.getReceiverAccount())
                .receiverName(request.getReceiverName())
                .receiverBank(request.getReceiverBank())
                .defaultAmount(request.getDefaultAmount())
                .buttonColor(request.getButtonColor() != null ? request.getButtonColor() : "#007AFF")
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : nextDisplayOrder)
                .build();
        
        preset = presetRepository.save(preset);
        
        log.info("원클릭 프리셋 생성 완료: 프리셋ID={}, 사용자={}, 이름={}", 
                preset.getPresetId(), user.getName(), preset.getPresetName());
        
        return PresetResponseDTO.from(preset);
    }
    
    public PresetResponseDTO updatePreset(Long presetId, PresetRequestDTO request) {
        OneClickPreset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRESET_NOT_FOUND));
        
        if (!preset.getUser().getUserId().equals(request.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        
        preset.setPresetName(request.getPresetName());
        preset.setReceiverAccount(request.getReceiverAccount());
        preset.setReceiverName(request.getReceiverName());
        preset.setReceiverBank(request.getReceiverBank());
        preset.setDefaultAmount(request.getDefaultAmount());
        if (request.getButtonColor() != null) {
            preset.setButtonColor(request.getButtonColor());
        }
        if (request.getDisplayOrder() != null) {
            preset.setDisplayOrder(request.getDisplayOrder());
        }
        
        preset = presetRepository.save(preset);
        
        log.info("원클릭 프리셋 수정 완료: 프리셋ID={}", presetId);
        
        return PresetResponseDTO.from(preset);
    }
    
    public void deletePreset(Long presetId) {
        OneClickPreset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRESET_NOT_FOUND));
        
        preset.setIsActive(false);
        presetRepository.save(preset);
        
        log.info("원클릭 프리셋 삭제 완료: 프리셋ID={}", presetId);
    }
}