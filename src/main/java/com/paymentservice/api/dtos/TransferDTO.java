package com.paymentservice.api.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferDTO(
        @NotNull(message = "O valor da transferência é obrigatório.")
        @Positive(message = "O valor da transferência deve ser positivo.")
        BigDecimal value,
        @NotNull(message = "O pagador é obrigatório.")
        Long payer,
        @NotNull(message = "O beneficiário é obrigatório.")
        Long receiver) {
}
