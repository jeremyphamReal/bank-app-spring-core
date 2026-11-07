package binhjeremy.bankappspringcore.controller;

import binhjeremy.bankappspringcore.models.Account;
import binhjeremy.bankappspringcore.models.DTO.request.AmountRequest;
import binhjeremy.bankappspringcore.models.DTO.request.TransferRequest;
import binhjeremy.bankappspringcore.models.DTO.response.AccountResponse;
import binhjeremy.bankappspringcore.models.DTO.response.ApiResponse;
import binhjeremy.bankappspringcore.models.DTO.response.TransferResponse;
import binhjeremy.bankappspringcore.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final AccountService accountService;
    public TransactionController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse> transfer(
            @RequestBody @Valid TransferRequest transferRequest,
            Authentication authencation) {
        String username = authencation.getName();

        Account updateAccount = accountService.transfer(username, transferRequest.toUsername(), transferRequest.amount());

        TransferResponse response = new TransferResponse(
                "TXN" + System.currentTimeMillis(),
                transferRequest.toUsername(),
                transferRequest.amount(),
                updateAccount.getBalance(),
                LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(response, "Chuyển khoản thành công"));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<AccountResponse>> deposit(@RequestBody @Valid AmountRequest amountRequest, Authentication authencation) {
        String username = authencation.getName();
        Account updateAccount = accountService.deposit(username, amountRequest.amount());

        AccountResponse response = new AccountResponse(
                updateAccount.getId(),
                updateAccount.getUsername(),
                updateAccount.getBalance()
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Nạp tiền thành công"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<AccountResponse>> withdraw(@RequestBody @Valid AmountRequest amountRequest, Authentication authencation) {
        String username = authencation.getName();
        Account updateAccount = accountService.withdraw(username, amountRequest.amount());

        AccountResponse response = new AccountResponse(
                updateAccount.getId(),
                updateAccount.getUsername(),
                updateAccount.getBalance()
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Rút tiền thành công"));
    }
}
