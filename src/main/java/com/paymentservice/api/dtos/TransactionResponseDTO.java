package com.paymentservice.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
        Long id,
        BigDecimal value,
        Long payerId,
        String payerName,
        Long payeeId,
        String payeeName,
        LocalDateTime timestamp) {
}
