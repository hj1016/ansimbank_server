package com.grandma.ansimbank.preset;

import com.grandma.ansimbank.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "one_click_presets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OneClickPreset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preset_id")
    private Long presetId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "preset_name", nullable = false)
    private String presetName;
    
    @Column(name = "receiver_account", nullable = false)
    private String receiverAccount;
    
    @Column(name = "receiver_name", nullable = false)
    private String receiverName;
    
    @Column(name = "receiver_bank", nullable = false)
    private String receiverBank;
    
    @Column(name = "default_amount", precision = 15, scale = 2)
    private BigDecimal defaultAmount;
    
    @Column(name = "button_color")
    private String buttonColor;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
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