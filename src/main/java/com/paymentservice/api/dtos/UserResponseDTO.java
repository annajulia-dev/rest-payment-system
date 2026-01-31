package com.paymentservice.api.dtos;

import com.paymentservice.api.model.User;
import com.paymentservice.api.enums.UserType;

import java.math.BigDecimal;

public record UserResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        UserType userType,
        BigDecimal balance) {
    public UserResponseDTO(User user){
        this(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUserType(),
                user.getBalance());
    }
}
