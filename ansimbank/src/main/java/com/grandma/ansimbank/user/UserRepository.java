package com.grandma.ansimbank.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}