package kh.edu.istad.moviebooking.features.bakong.dto;

import java.math.BigDecimal;

public record BakongTransactionData(
        String hash,

        String fromAccountId,

        String toAccountId,

        String currency,

        BigDecimal amount,

        String description
) {
}
