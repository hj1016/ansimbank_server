package com.grandma.ansimbank.user.entity;

import com.grandma.ansimbank.common.constants.ConnectionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FamilyConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;

    // 권한 설정
    private Long transferLimit; // 송금 한도
    private boolean monitoringEnabled; // 모니터링 권한
    private Long alertThreshold; // 알림 임계값

    @CreatedDate
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;
}