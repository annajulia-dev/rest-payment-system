package com.paymentservice.api.exception;

public class UnauthorizedTransactionException extends RuntimeException {
    public UnauthorizedTransactionException() {
        super("Mercantes não podem fazer transações.");
    }
    public UnauthorizedTransactionException(String message){
        super(message);
    }
}
