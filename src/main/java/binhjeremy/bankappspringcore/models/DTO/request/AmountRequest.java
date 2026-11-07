package binhjeremy.bankappspringcore.models.DTO.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(
        @NotNull(message = "Số tiền không được để trống")
        BigDecimal amount
) {
}
