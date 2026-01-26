package com.paymentservice.api.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Saldo insuficiente para realizar a transação.");
    }

    public InsufficientFundsException(String message){
        super(message);
    }
}
