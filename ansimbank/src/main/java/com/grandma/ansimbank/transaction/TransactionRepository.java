package com.grandma.ansimbank.transaction;

import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.delegation.Delegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findBySenderOrderByCreatedAtDesc(User sender);
    
    List<Transaction> findBySender_UserIdOrderByCreatedAtDesc(Long senderId);
    
    List<Transaction> findByDelegationOrderByCreatedAtDesc(Delegation delegation);
    
    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);
    
    List<Transaction> findByTransactionStatus(Transaction.TransactionStatus status);
    
    List<Transaction> findByTransactionType(Transaction.TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.sender = :sender AND t.transactionStatus = :status ORDER BY t.createdAt DESC")
    List<Transaction> findBySenderAndTransactionStatusOrderByCreatedAtDesc(@Param("sender") User sender, @Param("status") Transaction.TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.sender = :sender AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findBySenderAndCreatedAtBetweenOrderByCreatedAtDesc(@Param("sender") User sender, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.sender = :sender AND t.transactionStatus = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalAmountBySenderAndDateRange(@Param("sender") User sender, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.sender = :sender AND t.transactionStatus = 'COMPLETED' AND DATE(t.createdAt) = CURRENT_DATE")
    long countTodayTransactionsBySender(@Param("sender") User sender);
    
    @Query("SELECT t FROM Transaction t WHERE t.receiverAccount = :accountNumber ORDER BY t.createdAt DESC")
    List<Transaction> findByReceiverAccountOrderByCreatedAtDesc(@Param("accountNumber") String accountNumber);
    
    @Query("SELECT t FROM Transaction t WHERE (t.senderAccount = :accountNumber OR t.receiverAccount = :accountNumber) ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountNumberOrderByCreatedAtDesc(@Param("accountNumber") String accountNumber);
}