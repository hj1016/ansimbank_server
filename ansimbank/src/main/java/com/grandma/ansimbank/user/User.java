package com.grandma.ansimbank.user;

import com.grandma.ansimbank.common.constants.ConnectionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider")
    private SocialProvider socialProvider;
    
    @Column(name = "social_id")
    private String socialId;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // AuthUser에서 가져온 필드들 (JWT 인증용)
    @Column(unique = true)
    private String username;
    
    // 가족 연동 관련 필드
    private String requestedFamilyId; // 연동 요청한 부모/자녀 ID
    
    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus; // PENDING, APPROVED, REJECTED

    // FCM 토큰과의 관계 (FCM 기능과 통합)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<com.grandma.ansimbank.fcm.entity.FcmToken> fcmTokens;

    public enum UserType {
        PARENT, CHILD
    }

    public enum SocialProvider {
        KAKAO, NAVER, NONE
    }
    
    // AuthUser와 호환을 위한 getter 메소드
    public Long getId() {
        return this.userId;
    }
    
    public void setId(Long id) {
        this.userId = id;
    }
    
    public String getPhoneNumber() {
        return this.phone;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phone = phoneNumber;
    }
}