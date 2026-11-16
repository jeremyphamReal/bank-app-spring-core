package binhjeremy.bankappspringcore.service;

import binhjeremy.bankappspringcore.models.Account;
import binhjeremy.bankappspringcore.models.DTO.TransactionType;
import binhjeremy.bankappspringcore.models.Transaction;
import binhjeremy.bankappspringcore.repository.AccountRepo;
import binhjeremy.bankappspringcore.repository.TransactionRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepo accountRepo;
    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private AccountService accountService;

    // 1. Test Deposit thành công
    @Test
    void deposit_shouldIncreaseBalance() {
        String username = "binhjeremy";
        BigDecimal initialBalance = new BigDecimal("10000000");
        BigDecimal depositAmount = new BigDecimal("200000");
        BigDecimal expectedBalance = new BigDecimal("10200000");

        Account account = new Account();
        account.setUsername(username);
        account.setBalance(initialBalance);

        when(accountRepo.findByUsernameWithLock(username)).thenReturn(Optional.of(account));
        when(accountRepo.save(account)).thenReturn(account);
        when(transactionRepo.save(ArgumentMatchers.any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Account updatedAccount = accountService.deposit(username, depositAmount);
        assertEquals(expectedBalance, updatedAccount.getBalance());
        assertEquals(expectedBalance, account.getBalance());

        verify(accountRepo, times(1)).findByUsernameWithLock(username);
        verify(accountRepo, times(1)).save(account);
        verify(transactionRepo, times(1)).save(ArgumentMatchers.any(Transaction.class));
    }

    // 2. Test Withdraw thành công
    @Test
    void withdraw_shouldDecreaseBalance() {
        String username = "binhjeremy";
        BigDecimal initialBalance = new BigDecimal("10000000");
        BigDecimal withdrawAmount = new BigDecimal("200000");
        BigDecimal expectedBalance = new BigDecimal("9800000");

        Account account = new Account();
        account.setUsername(username);
        account.setBalance(initialBalance);

        when(accountRepo.findByUsernameWithLock(username)).thenReturn(Optional.of(account));
        when(accountRepo.save(account)).thenReturn(account);

        Account updatedAccount = accountService.withdraw(username, withdrawAmount);

        assertEquals(expectedBalance, updatedAccount.getBalance());
        verify(accountRepo).findByUsernameWithLock(username);
        verify(accountRepo).save(account);
        verify(transactionRepo).save(argThat(transaction ->
                transaction.getAmount().equals(withdrawAmount)
                        && transaction.getType() == TransactionType.WITHDRAW
                        && transaction.getAccount() == account));
    }

    // 3. Test Withdraw thất bại do thiếu tiền (Ném IllegalStateException)
    @Test
    void withdraw_shouldThrowException_whenInsufficientFunds() {
        String username = "binhjeremy";
        BigDecimal balance = new BigDecimal("100000");
        BigDecimal withdrawAmount = new BigDecimal("200000");

        Account account = new Account();
        account.setUsername(username);
        account.setBalance(balance);
        when(accountRepo.findByUsernameWithLock(username)).thenReturn(Optional.of(account));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> accountService.withdraw(username, withdrawAmount)
        );

        assertEquals("Insufficient funds", exception.getMessage());
        assertEquals(balance, account.getBalance());
        verify(accountRepo).findByUsernameWithLock(username);
        verify(accountRepo, never()).save(any(Account.class));
        verify(transactionRepo, never()).save(any(Transaction.class));
    }

    // 4. Test Transfer thành công (Trừ người gửi, cộng người nhận)
    @Test
    void transfer_shouldUpdateBothAccounts() {
        String fromUsername = "sender";
        String toUsername = "receiver";
        BigDecimal transferAmount = new BigDecimal("300000");

        Account fromAccount = new Account();
        fromAccount.setUsername(fromUsername);
        fromAccount.setBalance(new BigDecimal("1000000"));

        Account toAccount = new Account();
        toAccount.setUsername(toUsername);
        toAccount.setBalance(new BigDecimal("500000"));

        when(accountRepo.findByUsernameWithLock(fromUsername)).thenReturn(Optional.of(fromAccount));
        when(accountRepo.findByUsernameWithLock(toUsername)).thenReturn(Optional.of(toAccount));

        Account result = accountService.transfer(fromUsername, toUsername, transferAmount);

        assertEquals(fromAccount, result);
        assertEquals(new BigDecimal("700000"), fromAccount.getBalance());
        assertEquals(new BigDecimal("800000"), toAccount.getBalance());
        verify(accountRepo).findByUsernameWithLock(fromUsername);
        verify(accountRepo).findByUsernameWithLock(toUsername);
        verify(accountRepo).save(fromAccount);
        verify(accountRepo).save(toAccount);
        verify(transactionRepo).save(argThat(transaction ->
                transaction.getAmount().equals(transferAmount)
                        && transaction.getType() == TransactionType.TRANSFER_OUT
                        && transaction.getAccount() == fromAccount));
        verify(transactionRepo).save(argThat(transaction ->
                transaction.getAmount().equals(transferAmount)
                        && transaction.getType() == TransactionType.TRANSFER_IN
                        && transaction.getAccount() == toAccount));
    }

    // 5. Test Transfer thất bại do thiếu tiền
    @Test
    void transfer_shouldThrowException_whenInsufficientFunds() {
        String fromUsername = "sender";
        String toUsername = "receiver";
        BigDecimal transferAmount = new BigDecimal("300000");

        Account fromAccount = new Account();
        fromAccount.setUsername(fromUsername);
        fromAccount.setBalance(new BigDecimal("100000"));

        Account toAccount = new Account();
        toAccount.setUsername(toUsername);
        toAccount.setBalance(new BigDecimal("500000"));

        when(accountRepo.findByUsernameWithLock(fromUsername)).thenReturn(Optional.of(fromAccount));
        when(accountRepo.findByUsernameWithLock(toUsername)).thenReturn(Optional.of(toAccount));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> accountService.transfer(fromUsername, toUsername, transferAmount)
        );

        assertEquals("Insufficient funds", exception.getMessage());
        assertEquals(new BigDecimal("100000"), fromAccount.getBalance());
        assertEquals(new BigDecimal("500000"), toAccount.getBalance());
        verify(accountRepo, never()).save(any(Account.class));
        verify(transactionRepo, never()).save(any(Transaction.class));
    }

    // 6. Test Transfer cho chính mình (Nếu bạn có chặn logic này)
    @Test
    void transfer_toSelf_shouldKeepBalanceUnchanged() {
        String username = "binhjeremy";
        BigDecimal initialBalance = new BigDecimal("1000000");
        BigDecimal transferAmount = new BigDecimal("200000");

        Account account = new Account();
        account.setUsername(username);
        account.setBalance(initialBalance);
        when(accountRepo.findByUsernameWithLock(username)).thenReturn(Optional.of(account));

        assertDoesNotThrow(() -> accountService.transfer(username, username, transferAmount));

        assertEquals(initialBalance, account.getBalance());
        verify(accountRepo, times(2)).findByUsernameWithLock(username);
        verify(accountRepo, times(2)).save(account);
        verify(transactionRepo, times(2)).save(any(Transaction.class));
    }
}
