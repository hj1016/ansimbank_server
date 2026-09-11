package com.grandma.ansimbank.account;

import com.grandma.ansimbank.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "bank_code", nullable = false)
    private String bankCode;
    
    @Column(name = "bank_name", nullable = false)
    private String bankName;
    
    @Column(name = "account_number", nullable = false)
    private String accountNumber;
    
    @Column(name = "account_holder", nullable = false)
    private String accountHolder;
    
    @Builder.Default
    @Column(name = "is_primary")
    private Boolean isPrimary = false;
    
    @Column(name = "connected_id")
    private String connectedId; // CODEF ConnectedId (CODEF 방식에서만 사용)
    
    @Column(name = "link_type")
    private String linkType; // "MANUAL" 또는 "CODEF"
    
    @Builder.Default
    @Column(name = "balance")
    private Long balance = 0L; // 계좌 잔액 (원 단위)

    // [동시성] 낙관적 락 버전 컬럼.
    // 차감 경로(TransferService)는 비관적 락(SELECT ... FOR UPDATE)으로 직렬화되므로 거기선 이 버전이 무의미하다.
    // 이 컬럼이 실제로 막는 건 '락을 타지 않고 행 전체를 save하는 경로'다:
    // AccountService.setPrimaryAccount/unlinkAccount 는 findById(락 없음)로 읽고 save()하는데,
    // 이 엔티티에 @DynamicUpdate가 없어 save가 balance까지 전 컬럼을 덮어쓴다. 주계좌 토글 도중 동시 차감이
    // 일어나면 stale balance가 차감을 덮어쓰는 lost update가 가능하며, @Version이 이를 OptimisticLockException으로 막는다.
    // (근본 해결: AccountService가 balance를 건드리지 않게 분리하거나 @DynamicUpdate 적용)
    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}