package com.grandma.ansimbank.user.repository;

import com.grandma.ansimbank.common.constants.ConnectionStatus;
import com.grandma.ansimbank.common.constants.UserType;
import com.grandma.ansimbank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndUserType(String username, UserType userType);
    boolean existsByUsername(String username);
    List<User> findByRequestedFamilyIdAndConnectionStatus(String familyId, ConnectionStatus status);
}