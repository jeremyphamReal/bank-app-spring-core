package binhjeremy.bankappspringcore.controller;

import binhjeremy.bankappspringcore.config.JwtUtil;
import binhjeremy.bankappspringcore.models.DTO.Login.LoginRequest;
import binhjeremy.bankappspringcore.models.DTO.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Thực hiện xác thực
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(), request.password())
            );

            // Nếu thành công -> tạo token
            String token = jwtUtil.generateToken(request.username());
            return ResponseEntity.ok(ApiResponse.success(Map.of("token", token),"Đăng nhập thành công"));

        } catch (BadCredentialsException e) {
            // Bắt lỗi sai tài khoản/mật khẩu
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Tài khoản hoặc mật khẩu không chính xác"));
        } catch (Exception e) {
            // Các lỗi khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}
