package com.paymentservice.api.dtos;

import jakarta.validation.constraints.Email;

public record UserUpdateDTO(
        String firstName,
        String lastName,
        @Email(message = "Email inválido.")
        String email,
        String password
) {
}
