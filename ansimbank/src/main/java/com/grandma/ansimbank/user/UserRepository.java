package com.grandma.ansimbank.user;

import com.grandma.ansimbank.common.constants.ConnectionStatus;
// UserType은 User.UserType으로 사용
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    Optional<User> findByPhone(String phone);
    
    Optional<User> findByPhoneAndIsActiveTrue(String phone);
    
    Optional<User> findBySocialProviderAndSocialId(User.SocialProvider socialProvider, String socialId);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.isActive = true")
    java.util.List<User> findByUserTypeAndIsActiveTrue(@Param("userType") User.UserType userType);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.isActive = true")
    long countByUserTypeAndIsActiveTrue(@Param("userType") User.UserType userType);
    
    // AuthUserRepository에서 가져온 메소드들
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndUserType(String username, User.UserType userType);
    boolean existsByUsername(String username);
    List<User> findByRequestedFamilyIdAndConnectionStatus(String familyId, ConnectionStatus status);
}