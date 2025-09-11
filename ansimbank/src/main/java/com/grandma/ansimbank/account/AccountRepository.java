package com.grandma.ansimbank.account;

import com.grandma.ansimbank.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    List<Account> findByUserAndIsActiveTrue(User user);
    
    List<Account> findByUser_UserIdAndIsActiveTrue(Long userId);
    
    Optional<Account> findByUserAndIsPrimaryTrueAndIsActiveTrue(User user);
    
    Optional<Account> findByUser_UserIdAndIsPrimaryTrueAndIsActiveTrue(Long userId);
    
    Optional<Account> findByAccountNumberAndBankCode(String accountNumber, String bankCode);
    
    boolean existsByAccountNumberAndBankCode(String accountNumber, String bankCode);
    
    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.bankCode = :bankCode AND a.isActive = true")
    List<Account> findByUserAndBankCodeAndIsActiveTrue(@Param("user") User user, @Param("bankCode") String bankCode);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.user = :user AND a.isActive = true")
    long countByUserAndIsActiveTrue(@Param("user") User user);
    
    @Query("SELECT a FROM Account a WHERE a.accountHolder = :accountHolder AND a.isActive = true")
    List<Account> findByAccountHolderAndIsActiveTrue(@Param("accountHolder") String accountHolder);
    
    Optional<Account> findByAccountNumberAndUser_UserId(String accountNumber, Long userId);
}