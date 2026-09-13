package com.wealthops.transaction.repository;

import com.wealthops.transaction.entity.Transaction;
import com.wealthops.transaction.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByClientId(Long clientId);

    List<Transaction> findByClient_AssignedRm_Id(Long rmId);

    List<Transaction> findByClient_AssignedRm_IdAndStatus(Long rmId, TransactionStatus status);

    List<Transaction> findByClient_Branch_Id(Long branchId);

    List<Transaction> findByClient_Branch_IdAndStatus(Long branchId, TransactionStatus status);

    List<Transaction> findByStatus(TransactionStatus status);
}