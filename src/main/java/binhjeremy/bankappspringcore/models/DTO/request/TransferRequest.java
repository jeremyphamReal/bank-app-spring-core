package binhjeremy.bankappspringcore.models.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Money Transfer is not null")
        @Positive(message = "Money more than 0")
        BigDecimal amount,

        @NotBlank(message = "Name to transfer not be blank")
        String toUsername
) { }
