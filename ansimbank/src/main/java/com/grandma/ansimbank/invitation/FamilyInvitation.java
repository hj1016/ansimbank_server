package com.grandma.ansimbank.invitation;

import com.grandma.ansimbank.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FamilyInvitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long invitationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_user_id", nullable = false)
    private User inviter; // 초대를 보낸 사용자
    
    @Column(name = "target_phone_number", nullable = false)
    private String targetPhoneNumber; // 초대받을 사람의 전화번호
    
    @Column(name = "relationship_type")
    private String relationshipType; // 관계 (선택사항)
    
    @Column(name = "invitation_message")
    private String invitationMessage; // 초대 메시지
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status; // 초대 상태
    
    @Column(name = "invitation_code", unique = true)
    private String invitationCode; // 초대 코드 (UUID)
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 초대 만료 시간
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum InvitationStatus {
        PENDING,    // 대기 중
        ACCEPTED,   // 수락됨 (가입 완료)
        EXPIRED,    // 만료됨
        CANCELLED   // 취소됨
    }
    
    // 초대가 유효한지 확인
    public boolean isValid() {
        return status == InvitationStatus.PENDING && 
               expiresAt.isAfter(LocalDateTime.now());
    }
}