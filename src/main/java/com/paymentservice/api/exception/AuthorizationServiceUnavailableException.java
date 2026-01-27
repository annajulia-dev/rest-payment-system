package com.paymentservice.api.exception;

public class AuthorizationServiceUnavailableException extends RuntimeException {
    public AuthorizationServiceUnavailableException(String message) {
        super(message);
    }
}
