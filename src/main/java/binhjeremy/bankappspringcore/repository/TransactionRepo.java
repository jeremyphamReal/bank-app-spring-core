package binhjeremy.bankappspringcore.repository;

import binhjeremy.bankappspringcore.models.Account;
import binhjeremy.bankappspringcore.models.DTO.response.TransactionResponse;
import binhjeremy.bankappspringcore.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
    Page<Transaction> findByAccountUsernameOrderByDateTimeDesc(String username, Pageable pageable);
}
