package com.grandma.ansimbank.fcm.repository;

import com.grandma.ansimbank.fcm.entity.FamilyConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyConnectionRepository extends JpaRepository<FamilyConnection, Long> {

    // 부모 ID로 연결된 자녀들 찾기 (승인된 것만)
    List<FamilyConnection> findByParentUserIdAndConnectionStatus(Long parentId, FamilyConnection.ConnectionStatus status);

    // 자녀 ID로 연결된 부모 찾기 (승인된 것만)
    Optional<FamilyConnection> findByChildUserIdAndConnectionStatus(Long childId, FamilyConnection.ConnectionStatus status);

    // 특정 부모-자녀 연결 찾기
    Optional<FamilyConnection> findByParentUserIdAndChildUserId(Long parentId, Long childId);

    // 사용자가 연결된 모든 관계 찾기 (부모든 자녀든 상관없이)
    @Query("SELECT fc FROM FamilyConnection fc WHERE (fc.parent.userId = :userId OR fc.child.userId = :userId) AND fc.connectionStatus = :status")
    List<FamilyConnection> findAllConnectionsByUserId(@Param("userId") Long userId, @Param("status") FamilyConnection.ConnectionStatus status);
}