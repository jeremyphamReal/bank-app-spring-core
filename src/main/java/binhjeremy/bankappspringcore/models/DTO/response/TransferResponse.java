package binhjeremy.bankappspringcore.models.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        String transactionId,
        String toUsername,
        BigDecimal transferredAmount,
        BigDecimal remainingBalance,
        LocalDateTime transactionTime
) { }
