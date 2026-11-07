package binhjeremy.bankappspringcore.config;

import binhjeremy.bankappspringcore.models.DTO.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // =================================================================
    // NHÓM 1: LỖI VALIDATION (Dữ liệu đầu vào sai - HTTP 400)
    // =================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(
                new ApiResponse<>(400, "Dữ liệu không hợp lệ", errors, LocalDateTime.now())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error(400, ex.getMessage())
        );
    }

    // =================================================================
    // NHÓM 2: LỖI NGHIỆP VỤ (Logic ngân hàng - HTTP 409 Conflict)
    // =================================================================

    // Bắt các lỗi như: Số dư không đủ, không thể chuyển tiền cho chính mình, v.v.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.error(409, ex.getMessage())
        );
    }

    // =================================================================
    // NHÓM 3: LỖI TÀI NGUYÊN KHÔNG TÌM THẤY (HTTP 404)
    // =================================================================

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(404, ex.getMessage())
        );
    }

    // =================================================================
    // NHÓM 4: LỖI BẢO MẬT & XÁC THỰC (HTTP 401 & 403)
    // =================================================================

    // Lỗi đăng nhập sai tài khoản/mật khẩu
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error(401, "Tài khoản hoặc mật khẩu không chính xác")
        );
    }

    // Lỗi từ chối truy cập (dành cho các API dùng @PreAuthorize)
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error(403, "Bạn không có quyền truy cập chức năng này")
        );
    }

    // =================================================================
    // NHÓM 5: LỖI HỆ THỐNG KHÔNG LƯỜNG TRƯỚC (HTTP 500)
    // =================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
        // Ghi log lỗi ra màn hình console để Dev dễ debug
        System.err.println("🔥 LỖI HỆ THỐNG TẠI: " + request.getDescription(false));
        ex.printStackTrace();

        // Trả về thông báo chung chung cho Client (không lộ chi tiết lỗi DB)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(500, "Đã có lỗi xảy ra từ phía Server. Vui lòng thử lại sau!")
        );
    }
}
