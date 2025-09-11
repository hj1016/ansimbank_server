package com.grandma.ansimbank.user.repository;

import com.grandma.ansimbank.fcm.entity.FamilyConnection;
import com.grandma.ansimbank.common.constants.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyConnectionRepository extends JpaRepository<FamilyConnection, Long> {
    List<FamilyConnection> findByParentIdAndConnectionStatus(Long parentId, ConnectionStatus status);
    List<FamilyConnection> findByChildIdAndConnectionStatus(Long childId, ConnectionStatus status);
    Optional<FamilyConnection> findByParentIdAndChildId(Long parentId, Long childId);
    
    // FCM FamilyConnectionRepository에서 가져온 메소드들 (User 기반)
    List<FamilyConnection> findByParentUserIdAndConnectionStatus(Long parentId, ConnectionStatus status);
    Optional<FamilyConnection> findByChildUserIdAndConnectionStatus(Long childId, ConnectionStatus status);
    Optional<FamilyConnection> findByParentUserIdAndChildUserId(Long parentId, Long childId);
    
    // 사용자가 연결된 모든 관계 찾기 (특정 상태)
    @Query("SELECT fc FROM FamilyConnection fc WHERE (fc.parent.userId = :userId OR fc.child.userId = :userId) AND fc.connectionStatus = :status")
    List<FamilyConnection> findAllConnectionsByUserId(@Param("userId") Long userId, @Param("status") ConnectionStatus status);
    
    // 사용자가 연결된 모든 관계 찾기 (모든 상태)
    @Query("SELECT fc FROM FamilyConnection fc WHERE (fc.parent.userId = :userId OR fc.child.userId = :userId)")
    List<FamilyConnection> findAllConnectionsByUserIdAllStatuses(@Param("userId") Long userId);
}