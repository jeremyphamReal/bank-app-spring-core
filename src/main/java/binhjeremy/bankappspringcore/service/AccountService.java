package binhjeremy.bankappspringcore.service;

import binhjeremy.bankappspringcore.models.Account;
import binhjeremy.bankappspringcore.models.DTO.TransactionType;
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

    @Transactional(readOnly = true)
    public Account findAccountByName(String username){
        return accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }

    @Transactional
    public Account findAccountWithLock(String username) {
        return accountRepo.findByUsernameWithLock(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
    public Account registerAccount(String username, String password) {
        if(accountRepo.findByUsernameWithLock(username).isPresent()){
            throw new UsernameNotFoundException("Username already exists");
        }
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);
        return accountRepo.save(account);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        if(!passwordEncoder.matches(oldPassword, account.getPassword())){
            throw new IllegalArgumentException("Old password does not match");
        }
        if(passwordEncoder.matches(newPassword, account.getPassword())){
            throw new IllegalArgumentException("New password does not match old password");
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepo.save(account);
    }

    @Transactional
    public Account deposit(String username, BigDecimal amount) {

        Account account = findAccountWithLock(username);
        account.setBalance(account.getBalance().add(amount));

        Transaction transaction = new Transaction(
                amount, TransactionType.DEPOSIT, LocalDateTime.now(), account
        );

        transactionRepo.save(transaction);

        return accountRepo.save(account);
    }

    @Transactional
    public Account withdraw(String username, BigDecimal amount) {
        Account account = findAccountWithLock(username);
        if(account.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));

        Transaction transaction = new Transaction(
                amount, TransactionType.WITHDRAW, LocalDateTime.now(), account
        );
        transactionRepo.save(transaction);

        return accountRepo.save(account);
    }

    public List<Transaction> getTransactions(Account account) {
        return transactionRepo.findByAccountId(account.getId());
    }

    @Override
    @Transactional
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
        Account fromAccount = accountRepo.findByUsernameWithLock(fromUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Deposit account does not exist"));

        Account toAccount = accountRepo.findByUsernameWithLock(toUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Receiving account does not exist"));

        if(fromAccount.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient funds");
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepo.save(fromAccount);
        accountRepo.save(toAccount);

        Transaction debitTransaction = new Transaction(
                amount, TransactionType.TRANSFER_OUT, LocalDateTime.now(), fromAccount
        );
        transactionRepo.save(debitTransaction);

        Transaction creditTransaction = new Transaction(
                amount, TransactionType.TRANSFER_IN, LocalDateTime.now(), toAccount
        );
        transactionRepo.save(creditTransaction);

        return fromAccount;
    }
}
