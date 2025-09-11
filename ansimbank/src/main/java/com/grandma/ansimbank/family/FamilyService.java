package com.grandma.ansimbank.family;

import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.constants.ConnectionStatus;
import com.grandma.ansimbank.common.exception.CustomException;
import com.grandma.ansimbank.family.dto.FamilyConnectionRequestDTO;
import com.grandma.ansimbank.family.dto.FamilyConnectionResponseDTO;
import com.grandma.ansimbank.family.dto.FamilyConnectionStatusUpdateDTO;
import com.grandma.ansimbank.fcm.entity.FamilyConnection;
import com.grandma.ansimbank.user.repository.FamilyConnectionRepository;
import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.user.UserRepository;
import com.grandma.ansimbank.invitation.FamilyInvitation;
import com.grandma.ansimbank.invitation.FamilyInvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FamilyService {
    
    private final FamilyConnectionRepository familyConnectionRepository;
    private final UserRepository userRepository;
    private final FamilyInvitationRepository familyInvitationRepository;
    
    @Transactional(readOnly = true)
    public List<FamilyConnectionResponseDTO> getFamilyConnections(Long userId) {
        log.info("가족 연동 현황 조회: 사용자ID={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        List<FamilyConnectionResponseDTO> results = new java.util.ArrayList<>();
        
        // 1. 모든 상태의 가족 연결 조회 (APPROVED, PENDING)
        List<FamilyConnection> allConnections = familyConnectionRepository
                .findAllConnectionsByUserIdAllStatuses(userId);
        
        results.addAll(allConnections.stream()
                .map(FamilyConnectionResponseDTO::from)
                .toList());
        
        // 2. 내가 보낸 대기 중인 초대들 조회
        List<FamilyInvitation> sentInvitations = familyInvitationRepository
                .findByInviter_UserIdOrderByCreatedAtDesc(userId);
        
        results.addAll(sentInvitations.stream()
                .filter(invitation -> invitation.getStatus() == FamilyInvitation.InvitationStatus.PENDING)
                .map(this::convertInvitationToResponseDTO)
                .toList());
        
        log.info("가족 연동 현황 조회 완료: 사용자ID={}, 총 {}개 항목", userId, results.size());
        
        return results;
    }
    
    public FamilyConnectionResponseDTO requestFamilyConnection(FamilyConnectionRequestDTO request) {
        log.info("가족 연동 요청: 요청자ID={}, 대상전화번호={}", request.getRequesterId(), request.getTargetPhoneNumber());
        
        User requester = userRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 대상자가 가입된 사용자인지 확인
        Optional<User> targetUser = userRepository.findByPhoneAndIsActiveTrue(request.getTargetPhoneNumber());
        
        if (targetUser.isPresent()) {
            // 가입된 사용자인 경우 기존 로직 유지
            return createDirectFamilyConnection(requester, targetUser.get(), request);
        } else {
            // 미가입자인 경우 초대 생성
            return createFamilyInvitation(requester, request);
        }
    }
    
    private FamilyConnectionResponseDTO createDirectFamilyConnection(User requester, User target, FamilyConnectionRequestDTO request) {
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
            ConnectionStatus status = existingConnection.get().getConnectionStatus();
            if (status == ConnectionStatus.APPROVED) {
                throw new CustomException(ErrorCode.FAMILY_CONNECTION_ALREADY_EXISTS);
            } else if (status == ConnectionStatus.PENDING) {
                throw new CustomException(ErrorCode.FAMILY_CONNECTION_PENDING);
            }
        }
        
        User parent, child;
        if (requester.getUserType() == User.UserType.PARENT) {
            parent = requester;
            child = target;
        } else if (target.getUserType() == User.UserType.PARENT) {
            parent = target;
            child = requester;
        } else {
            parent = requester;
            child = target;
        }
        
        FamilyConnection connection = FamilyConnection.builder()
                .parent(parent)
                .child(child)
                .connectionStatus(ConnectionStatus.PENDING)
                .build();
        
        connection = familyConnectionRepository.save(connection);
        
        log.info("가족 연동 요청 생성: 연결ID={}, 부모={}, 자녀={}", 
                connection.getConnectionId(), parent.getName(), child.getName());
        
        return FamilyConnectionResponseDTO.from(connection);
    }
    
    private FamilyConnectionResponseDTO createFamilyInvitation(User requester, FamilyConnectionRequestDTO request) {
        // 이미 같은 전화번호로 유효한 초대가 있는지 확인
        Optional<FamilyInvitation> existingInvitation = familyInvitationRepository
                .findExistingValidInvitation(requester.getUserId(), request.getTargetPhoneNumber(), LocalDateTime.now());
        
        if (existingInvitation.isPresent()) {
            throw new CustomException(ErrorCode.FAMILY_INVITATION_ALREADY_EXISTS);
        }
        
        // 새 초대 생성
        String invitationCode = "FAM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7); // 7일 후 만료
        
        FamilyInvitation invitation = FamilyInvitation.builder()
                .inviter(requester)
                .targetPhoneNumber(request.getTargetPhoneNumber())
                .relationshipType(request.getRelationshipType())
                .invitationMessage(request.getMessage())
                .status(FamilyInvitation.InvitationStatus.PENDING)
                .invitationCode(invitationCode)
                .expiresAt(expiresAt)
                .build();
        
        invitation = familyInvitationRepository.save(invitation);
        
        log.info("가족 초대 생성: 초대ID={}, 초대자={}, 대상전화번호={}, 초대코드={}", 
                invitation.getInvitationId(), requester.getName(), request.getTargetPhoneNumber(), invitationCode);

        // 초대는 즉시 완료로 처리 (실제 연결은 초대받은 사람이 가입할 때 생성)
        return FamilyConnectionResponseDTO.builder()
                .connectionId(invitation.getInvitationId())
                .parentName(requester.getName())
                .parentPhone(requester.getPhone())
                .childName("초대 대기중")
                .childPhone(request.getTargetPhoneNumber())
                .relationshipType(request.getRelationshipType())
                .connectionStatus("INVITATION_SENT")
                .createdAt(invitation.getCreatedAt())
                .build();
    }
    
    public FamilyConnectionResponseDTO updateConnectionStatus(Long connectionId, FamilyConnectionStatusUpdateDTO request, Long userId) {
        log.info("가족 연동 상태 변경: 연결ID={}, 상태={}, 사용자ID={}", connectionId, request.getStatus(), userId);
        
        FamilyConnection connection = familyConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_CONNECTION_NOT_FOUND));
        
        if (!connection.getParent().getUserId().equals(userId) && 
            !connection.getChild().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_FAMILY_CONNECTION);
        }
        
        if (connection.getConnectionStatus() != ConnectionStatus.PENDING) {
            throw new CustomException(ErrorCode.FAMILY_CONNECTION_ALREADY_PROCESSED);
        }
        
        try {
            ConnectionStatus newStatus = ConnectionStatus.valueOf(request.getStatus());
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
        // 1. PENDING 상태의 family connection 개수
        long pendingConnections = familyConnectionRepository
                .findAllConnectionsByUserId(userId, ConnectionStatus.PENDING)
                .size();
        
        // 2. 내가 보낸 PENDING 상태의 invitation 개수
        long pendingInvitations = familyInvitationRepository
                .findByInviter_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .mapToLong(invitation -> invitation.getStatus() == FamilyInvitation.InvitationStatus.PENDING ? 1 : 0)
                .sum();
        
        return pendingConnections + pendingInvitations;
    }
    
    @Transactional(readOnly = true)
    public long getConnectedFamilyCount(Long userId) {
        return familyConnectionRepository
                .findAllConnectionsByUserId(userId, ConnectionStatus.APPROVED)
                .size();
    }

    private FamilyConnectionResponseDTO convertInvitationToResponseDTO(FamilyInvitation invitation) {
        return FamilyConnectionResponseDTO.builder()
                .connectionId(invitation.getInvitationId())
                .parentName(invitation.getInviter().getName())
                .parentPhone(invitation.getInviter().getPhone())
                .childName("초대 대기중")
                .childPhone(invitation.getTargetPhoneNumber())
                .relationshipType(invitation.getRelationshipType())
                .connectionStatus("PENDING")
                .createdAt(invitation.getCreatedAt())
                .updatedAt(invitation.getUpdatedAt())
                .build();
    }
    
    /**
     * 사용자 가입 시 해당 전화번호로 온 초대들을 처리
     */
    public void processInvitationsOnSignUp(User newUser) {
        log.info("신규 가입자 초대 처리: 사용자ID={}, 전화번호={}", newUser.getUserId(), newUser.getPhone());
        
        List<FamilyInvitation> validInvitations = familyInvitationRepository
                .findValidInvitationsByPhoneNumber(newUser.getPhone(), LocalDateTime.now());
        
        for (FamilyInvitation invitation : validInvitations) {
            try {
                User inviter = invitation.getInviter();
                User parent, child;
                
                if (inviter.getUserType() == User.UserType.PARENT) {
                    parent = inviter;
                    child = newUser;
                } else if (newUser.getUserType() == User.UserType.PARENT) {
                    parent = newUser;
                    child = inviter;
                } else {
                    // If both are same type, use inviter as parent for consistency
                    parent = inviter;
                    child = newUser;
                }
                
                FamilyConnection connection = FamilyConnection.builder()
                        .parent(parent)
                        .child(child)
                        .connectionStatus(ConnectionStatus.PENDING)
                        .build();
                
                connection = familyConnectionRepository.save(connection);
                
                // 초대 상태를 수락됨으로 변경
                invitation.setStatus(FamilyInvitation.InvitationStatus.ACCEPTED);
                familyInvitationRepository.save(invitation);
                
                log.info("초대 기반 가족 연결 생성: 연결ID={}, 초대ID={}, 부모={}, 자녀={}", 
                        connection.getConnectionId(), invitation.getInvitationId(), 
                        parent.getName(), child.getName());
                        
            } catch (Exception e) {
                log.error("초대 처리 실패: 초대ID={}, 오류={}", invitation.getInvitationId(), e.getMessage());
            }
        }
    }
}