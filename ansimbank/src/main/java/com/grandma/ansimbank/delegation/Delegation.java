package com.grandma.ansimbank.delegation;

import com.grandma.ansimbank.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "delegations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delegation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delegation_id")
    private Long delegationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegator_id", nullable = false)
    private User delegator;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegate_id", nullable = false)
    private User delegate;
    
    @Column(name = "delegation_type")
    private String delegationType;
    
    @Column(name = "delegation_scope", columnDefinition = "TEXT")
    private String delegationScope;
    
    @Column(name = "valid_from")
    private LocalDate validFrom;
    
    @Column(name = "valid_until")
    private LocalDate validUntil;
    
    @Column(name = "daily_limit", precision = 15, scale = 2)
    private BigDecimal dailyLimit;
    
    @Column(name = "monthly_limit", precision = 15, scale = 2)
    private BigDecimal monthlyLimit;
    
    @Column(name = "allowed_accounts", columnDefinition = "TEXT")
    private String allowedAccounts;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "delegation_status", nullable = false)
    private DelegationStatus delegationStatus;
    
    @Column(name = "document_path")
    private String documentPath;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum DelegationStatus {
        ACTIVE, SUSPENDED, TERMINATED
    }
}