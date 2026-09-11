package com.grandma.ansimbank.account;

import com.grandma.ansimbank.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    /**
     * [동시성] 잔액 차감 경로 전용 비관적 쓰기 락 조회.
     * SELECT ... FOR UPDATE 로 해당 계좌 행을 잠근 채 가져와, 같은 계좌에 대한 동시 송금을
     * DB 레벨에서 직렬화한다. 승인 처리(ApprovalRepository.findByIdForUpdate)와 동일한 방어 전략을
     * 정작 돈이 빠져나가는 송금 경로에도 대칭으로 적용한 것.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber AND a.user.userId = :userId AND a.isActive = true")
    Optional<Account> findByAccountNumberAndUser_UserIdForUpdate(
            @Param("accountNumber") String accountNumber, @Param("userId") Long userId);
}