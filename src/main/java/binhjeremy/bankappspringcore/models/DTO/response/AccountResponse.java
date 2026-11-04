package binhjeremy.bankappspringcore.models.DTO.response;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String username,
        BigDecimal balance
) {
}
