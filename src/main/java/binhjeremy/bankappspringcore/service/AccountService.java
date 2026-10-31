package binhjeremy.bankappspringcore.service;

import binhjeremy.bankappspringcore.models.Account;
import binhjeremy.bankappspringcore.models.Transaction;
import binhjeremy.bankappspringcore.repository.AccountRepo;
import binhjeremy.bankappspringcore.repository.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    public Account findAccountByName(String username){
        return accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }

    public Account registerAccount(String username, String password) {
        if(accountRepo.findByUsername(username).isPresent()){
            throw new UsernameNotFoundException("Username already exists");
        }
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);
        return accountRepo.save(account);
    }

    public void deposit(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepo.save(account);

        Transaction transaction = new Transaction(
                amount, "deposit", LocalDateTime.now(), account
        );

        transactionRepo.save(transaction);
    }

    public void withdraw(Account account, BigDecimal amount) {
        if(account.getBalance().compareTo(amount) <= 0){
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepo.save(account);

        Transaction transaction = new Transaction(
                amount, "withdraw", LocalDateTime.now(), account
        );
        transactionRepo.save(transaction);
    }

    public List<Transaction> getTransactions(Account account) {
        return transactionRepo.findByAccountId(account.getId());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = findAccountByName(username);
        if(account == null){
            throw new UsernameNotFoundException("Username not found");
        }
        return new Account(account.getId(),
                account.getUsername(),
                account.getPassword(),
                account.getBalance(),
                account.getTransactions(),
                authorities());
    }
    public Collection<? extends GrantedAuthority> authorities() {
        return Arrays.asList(new SimpleGrantedAuthority("User"));
    }

    @Transactional
    public Account transfer(String fromUsername, String toUsername, BigDecimal amount) {
        Account fromAccount = accountRepo.findByUsername(fromUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Deposit account does not exist"));

        Account toAccount = accountRepo.findByUsername(toUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Receiving account does not exist"));

        if(fromAccount.getBalance().compareTo(amount) <= 0){
            throw new RuntimeException("Insufficient funds");
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepo.save(fromAccount);
        accountRepo.save(toAccount);

        Transaction debitTransaction = new Transaction(
                amount, "Chuyển tiền đến " + toAccount.getUsername(), LocalDateTime.now(), fromAccount
        );
        transactionRepo.save(debitTransaction);

        Transaction creditTransaction = new Transaction(
                amount, "Nhận tiền từ " + fromAccount.getUsername(), LocalDateTime.now(), toAccount
        );
        transactionRepo.save(creditTransaction);

        return fromAccount;
    }
}
