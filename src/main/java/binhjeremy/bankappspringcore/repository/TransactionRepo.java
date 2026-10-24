package binhjeremy.bankappspringcore.repository;

import binhjeremy.bankappspringcore.models.Account;
import binhjeremy.bankappspringcore.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
}
