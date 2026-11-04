package binhjeremy.bankappspringcore.models.DTO.request;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {
}
