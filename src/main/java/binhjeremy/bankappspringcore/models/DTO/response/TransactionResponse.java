package binhjeremy.bankappspringcore.models.DTO.response;

import binhjeremy.bankappspringcore.models.DTO.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        LocalDateTime time
) {
}
