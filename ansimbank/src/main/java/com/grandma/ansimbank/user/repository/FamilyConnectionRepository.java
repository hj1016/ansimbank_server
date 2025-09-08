package com.grandma.ansimbank.user.repository;

import com.grandma.ansimbank.common.constants.ConnectionStatus;
import com.grandma.ansimbank.user.entity.FamilyConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyConnectionRepository extends JpaRepository<FamilyConnection, Long> {
    List<FamilyConnection> findByParentIdAndStatus(Long parentId, ConnectionStatus status);
    List<FamilyConnection> findByChildIdAndStatus(Long childId, ConnectionStatus status);
    Optional<FamilyConnection> findByParentIdAndChildId(Long parentId, Long childId);
}