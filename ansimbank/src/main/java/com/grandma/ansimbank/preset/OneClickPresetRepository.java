package com.grandma.ansimbank.preset;

import com.grandma.ansimbank.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OneClickPresetRepository extends JpaRepository<OneClickPreset, Long> {
    
    List<OneClickPreset> findByUserAndIsActiveTrueOrderByDisplayOrderAsc(User user);
    
    List<OneClickPreset> findByUser_UserIdAndIsActiveTrueOrderByDisplayOrderAsc(Long userId);
    
    @Query("SELECT COUNT(p) FROM OneClickPreset p WHERE p.user = :user AND p.isActive = true")
    long countActivePresetsByUser(@Param("user") User user);
    
    @Query("SELECT MAX(p.displayOrder) FROM OneClickPreset p WHERE p.user = :user AND p.isActive = true")
    Integer findMaxDisplayOrderByUser(@Param("user") User user);
    
    List<OneClickPreset> findByReceiverAccountAndIsActiveTrue(String receiverAccount);
}