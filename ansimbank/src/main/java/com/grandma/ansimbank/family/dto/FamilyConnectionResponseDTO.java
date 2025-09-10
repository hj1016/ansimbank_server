package com.grandma.ansimbank.family.dto;

import com.grandma.ansimbank.fcm.entity.FamilyConnection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyConnectionResponseDTO {
    
    private Long connectionId;
    private Long parentId;
    private String parentName;
    private String parentPhone;
    private Long childId;
    private String childName;
    private String childPhone;
    private String connectionStatus; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static FamilyConnectionResponseDTO from(FamilyConnection connection) {
        return FamilyConnectionResponseDTO.builder()
                .connectionId(connection.getConnectionId())
                .parentId(connection.getParent().getUserId())
                .parentName(connection.getParent().getName())
                .parentPhone(connection.getParent().getPhone())
                .childId(connection.getChild().getUserId())
                .childName(connection.getChild().getName())
                .childPhone(connection.getChild().getPhone())
                .connectionStatus(connection.getConnectionStatus().name())
                .createdAt(connection.getCreatedAt())
                .updatedAt(connection.getUpdatedAt())
                .build();
    }
}