package com.paymentservice.api.repository;

import com.paymentservice.api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface TransactionRepository
    extends JpaRepository<Transaction, Long> {
}
