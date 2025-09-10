package com.grandma.ansimbank.user.repository;

// ConnectionStatus는 FamilyConnection.ConnectionStatus 사용
import com.grandma.ansimbank.fcm.entity.FamilyConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyConnectionRepository extends JpaRepository<FamilyConnection, Long> {
    List<FamilyConnection> findByParentIdAndConnectionStatus(Long parentId, FamilyConnection.ConnectionStatus status);
    List<FamilyConnection> findByChildIdAndConnectionStatus(Long childId, FamilyConnection.ConnectionStatus status);
    Optional<FamilyConnection> findByParentIdAndChildId(Long parentId, Long childId);
    
    // FCM FamilyConnectionRepository에서 가져온 메소드들 (User 기반)
    List<FamilyConnection> findByParentUserIdAndConnectionStatus(Long parentId, FamilyConnection.ConnectionStatus status);
    Optional<FamilyConnection> findByChildUserIdAndConnectionStatus(Long childId, FamilyConnection.ConnectionStatus status);
    Optional<FamilyConnection> findByParentUserIdAndChildUserId(Long parentId, Long childId);
    
    // 사용자가 연결된 모든 관계 찾기
    @Query("SELECT fc FROM FamilyConnection fc WHERE (fc.parent.userId = :userId OR fc.child.userId = :userId) AND fc.connectionStatus = :status")
    List<FamilyConnection> findAllConnectionsByUserId(@Param("userId") Long userId, @Param("status") FamilyConnection.ConnectionStatus status);
}