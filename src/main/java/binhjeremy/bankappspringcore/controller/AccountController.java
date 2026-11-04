package binhjeremy.bankappspringcore.controller;

import binhjeremy.bankappspringcore.models.Account;
import binhjeremy.bankappspringcore.models.DTO.request.ChangePasswordRequest;
import binhjeremy.bankappspringcore.models.DTO.response.AccountResponse;
import binhjeremy.bankappspringcore.models.DTO.response.ApiResponse;
import binhjeremy.bankappspringcore.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponse>> getMyProfile(Authentication auth) {
        String username = auth.getName();
        Account account =  accountService.findAccountByName(username);
        AccountResponse data = new AccountResponse(account.getId(), account.getUsername(), account.getBalance());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> change_password(Authentication auth, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        String username =  auth.getName();
        accountService.changePassword(username, changePasswordRequest.oldPassword(), changePasswordRequest.newPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công"));
    }
}
