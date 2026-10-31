package binhjeremy.bankappspringcore.models.DTO.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T>  success (T data) {
        return new ApiResponse<T>(200, "Success", data, LocalDateTime.now());
    }
    public static <T> ApiResponse<T>  success (T data, String message) {
        return new ApiResponse<T>(200, "Success", data, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null, LocalDateTime.now());
    }
}
