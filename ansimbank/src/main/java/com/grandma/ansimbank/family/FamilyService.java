package com.grandma.ansimbank.family;

import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.exception.CustomException;
import com.grandma.ansimbank.family.dto.FamilyConnectionRequestDTO;
import com.grandma.ansimbank.family.dto.FamilyConnectionResponseDTO;
import com.grandma.ansimbank.family.dto.FamilyConnectionStatusUpdateDTO;
import com.grandma.ansimbank.fcm.entity.FamilyConnection;
import com.grandma.ansimbank.user.repository.FamilyConnectionRepository;
import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FamilyService {
    
    private final FamilyConnectionRepository familyConnectionRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<FamilyConnectionResponseDTO> getFamilyConnections(Long userId) {
        log.info("가족 연동 현황 조회: 사용자ID={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        List<FamilyConnection> connections = familyConnectionRepository
                .findAllConnectionsByUserId(userId, FamilyConnection.ConnectionStatus.APPROVED);
        
        return connections.stream()
                .map(FamilyConnectionResponseDTO::from)
                .toList();
    }
    
    public FamilyConnectionResponseDTO requestFamilyConnection(FamilyConnectionRequestDTO request) {
        log.info("가족 연동 요청: 요청자ID={}, 대상전화번호={}", request.getRequesterId(), request.getTargetPhoneNumber());
        
        User requester = userRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        User target = userRepository.findByPhoneAndIsActiveTrue(request.getTargetPhoneNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        if (requester.getUserId().equals(target.getUserId())) {
            throw new CustomException(ErrorCode.INVALID_FAMILY_CONNECTION_REQUEST);
        }
        
        Optional<FamilyConnection> existingConnection = familyConnectionRepository
                .findByParentUserIdAndChildUserId(requester.getUserId(), target.getUserId());
        
        if (existingConnection.isEmpty()) {
            existingConnection = familyConnectionRepository
                    .findByParentUserIdAndChildUserId(target.getUserId(), requester.getUserId());
        }
        
        if (existingConnection.isPresent()) {
            FamilyConnection.ConnectionStatus status = existingConnection.get().getConnectionStatus();
            if (status == FamilyConnection.ConnectionStatus.APPROVED) {
                throw new CustomException(ErrorCode.FAMILY_CONNECTION_ALREADY_EXISTS);
            } else if (status == FamilyConnection.ConnectionStatus.PENDING) {
                throw new CustomException(ErrorCode.FAMILY_CONNECTION_PENDING);
            }
        }
        
        User parent, child;
        if (requester.getBirthDate().isBefore(target.getBirthDate())) {
            parent = requester;
            child = target;
        } else {
            parent = target;
            child = requester;
        }
        
        FamilyConnection connection = FamilyConnection.builder()
                .parent(parent)
                .child(child)
                .connectionStatus(FamilyConnection.ConnectionStatus.PENDING)
                .build();
        
        connection = familyConnectionRepository.save(connection);
        
        log.info("가족 연동 요청 생성: 연결ID={}, 부모={}, 자녀={}", 
                connection.getConnectionId(), parent.getName(), child.getName());
        
        return FamilyConnectionResponseDTO.from(connection);
    }
    
    public FamilyConnectionResponseDTO updateConnectionStatus(Long connectionId, FamilyConnectionStatusUpdateDTO request) {
        log.info("가족 연동 상태 변경: 연결ID={}, 상태={}", connectionId, request.getStatus());
        
        FamilyConnection connection = familyConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_CONNECTION_NOT_FOUND));
        
        if (!connection.getParent().getUserId().equals(request.getUserId()) && 
            !connection.getChild().getUserId().equals(request.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_FAMILY_CONNECTION);
        }
        
        if (connection.getConnectionStatus() != FamilyConnection.ConnectionStatus.PENDING) {
            throw new CustomException(ErrorCode.FAMILY_CONNECTION_ALREADY_PROCESSED);
        }
        
        try {
            FamilyConnection.ConnectionStatus newStatus = FamilyConnection.ConnectionStatus.valueOf(request.getStatus());
            connection.setConnectionStatus(newStatus);
            
            connection = familyConnectionRepository.save(connection);
            
            log.info("가족 연동 상태 변경 완료: 연결ID={}, 새상태={}", connectionId, newStatus);
            
            return FamilyConnectionResponseDTO.from(connection);
            
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_CONNECTION_STATUS);
        }
    }
    
    public void deleteFamilyConnection(Long connectionId, Long userId) {
        log.info("가족 연동 해제: 연결ID={}, 요청자ID={}", connectionId, userId);
        
        FamilyConnection connection = familyConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_CONNECTION_NOT_FOUND));
        
        if (!connection.getParent().getUserId().equals(userId) && 
            !connection.getChild().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_FAMILY_CONNECTION);
        }
        
        familyConnectionRepository.delete(connection);
        
        log.info("가족 연동 해제 완료: 연결ID={}", connectionId);
    }
    
    @Transactional(readOnly = true)
    public long getPendingConnectionCount(Long userId) {
        return familyConnectionRepository
                .findAllConnectionsByUserId(userId, FamilyConnection.ConnectionStatus.PENDING)
                .size();
    }
    
    @Transactional(readOnly = true)
    public long getConnectedFamilyCount(Long userId) {
        return familyConnectionRepository
                .findAllConnectionsByUserId(userId, FamilyConnection.ConnectionStatus.APPROVED)
                .size();
    }
}