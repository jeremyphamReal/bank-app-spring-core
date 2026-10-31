package binhjeremy.bankappspringcore.controller;

import binhjeremy.bankappspringcore.models.Account;
import binhjeremy.bankappspringcore.models.DTO.request.TransferRequest;
import binhjeremy.bankappspringcore.models.DTO.response.ApiResponse;
import binhjeremy.bankappspringcore.models.DTO.response.TransferResponse;
import binhjeremy.bankappspringcore.service.AccountService;
import jakarta.validation.Valid;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final AccountService accountService;

    public TransferController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transferAccount(@Valid @RequestBody TransferRequest transferRequest,
                                             Authentication authentication) {
        String username = authentication.name();
        Account updateAccount = accountService.transfer(username, transferRequest.toUsername(), transferRequest.amount());
        TransferResponse response = new TransferResponse(
                "TXN" + System.currentTimeMillis(), //Mã giao dịch
                transferRequest.toUsername(), //Người nhận
                transferRequest.amount(),   //Số tiền chuyển
                updateAccount.getBalance(), //Số dư còn lại
                LocalDateTime.now()     //Thời gian
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Chuyển tiền thành công"));
    }
}
