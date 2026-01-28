package com.paymentservice.api.dtos;

import com.paymentservice.api.model.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UserDTO(
        @NotBlank(message = "Nome é obrigatório.")
    String firstName,
        @NotBlank(message = "Sobrenome é obrigatório.")
    String lastName,
        @NotBlank(message = "Senha é obrigatório.")
    String password,
        @Email(message = "Email inválido.")
    String email,
        @NotBlank(message = "Documento é obrigatório.")
    String document,
        @NotNull(message = "Tipo de usuário é obrigatório.")
    UserType userType,
    BigDecimal balance) {
}
