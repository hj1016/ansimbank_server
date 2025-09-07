package com.grandma.ansimbank.fcm.repository;

import com.grandma.ansimbank.fcm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 활성 사용자만 찾기
    Optional<User> findByUserIdAndIsActiveTrue(Long userId);

    // 이메일로 활성 사용자 찾기
    Optional<User> findByEmailAndIsActiveTrue(String email);
}