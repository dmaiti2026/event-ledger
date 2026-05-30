package com.dmaiti.eventledger.account.repository;

import com.dmaiti.eventledger.account.entity.Transaction;
import com.dmaiti.eventledger.account.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByEventTimestampAsc(String accountId);

    long countByAccountId(String accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountId = :accountId AND t.type = :type")
    BigDecimal sumByAccountIdAndType(@Param("accountId") String accountId, @Param("type") TransactionType type);
}
