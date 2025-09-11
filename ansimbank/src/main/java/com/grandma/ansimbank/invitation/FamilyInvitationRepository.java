package com.grandma.ansimbank.invitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyInvitationRepository extends JpaRepository<FamilyInvitation, Long> {
    
    // 초대 코드로 찾기
    Optional<FamilyInvitation> findByInvitationCode(String invitationCode);
    
    // 전화번호로 유효한 초대 찾기
    @Query("SELECT fi FROM FamilyInvitation fi WHERE fi.targetPhoneNumber = :phoneNumber " +
           "AND fi.status = 'PENDING' AND fi.expiresAt > :now")
    List<FamilyInvitation> findValidInvitationsByPhoneNumber(
            @Param("phoneNumber") String phoneNumber, 
            @Param("now") LocalDateTime now);
    
    // 초대자별 초대 목록
    List<FamilyInvitation> findByInviter_UserIdOrderByCreatedAtDesc(Long inviterId);
    
    // 전화번호별 초대 목록
    List<FamilyInvitation> findByTargetPhoneNumberOrderByCreatedAtDesc(String targetPhoneNumber);
    
    // 만료된 초대 찾기
    @Query("SELECT fi FROM FamilyInvitation fi WHERE fi.status = 'PENDING' AND fi.expiresAt <= :now")
    List<FamilyInvitation> findExpiredInvitations(@Param("now") LocalDateTime now);
    
    // 특정 초대자가 같은 전화번호로 보낸 유효한 초대가 있는지 확인
    @Query("SELECT fi FROM FamilyInvitation fi WHERE fi.inviter.userId = :inviterId " +
           "AND fi.targetPhoneNumber = :phoneNumber AND fi.status = 'PENDING' " +
           "AND fi.expiresAt > :now")
    Optional<FamilyInvitation> findExistingValidInvitation(
            @Param("inviterId") Long inviterId,
            @Param("phoneNumber") String phoneNumber,
            @Param("now") LocalDateTime now);
}