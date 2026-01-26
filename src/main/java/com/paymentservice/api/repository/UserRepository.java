package com.paymentservice.api.repository;

import com.paymentservice.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserRepository
    extends JpaRepository<User, Long> {

    Optional<User> findUserByDocument(String document);

    boolean existsByDocument(String document);
    boolean existsByEmail(String email);

}
