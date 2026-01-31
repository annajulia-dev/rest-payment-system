package com.paymentservice.api.exception;

import com.paymentservice.api.enums.TransactionMessage;

public class UnauthorizedTransactionException extends RuntimeException {
    public UnauthorizedTransactionException() {
        super(TransactionMessage.INSUFFICIENT_FUNDS.getMessage());
    }
    public UnauthorizedTransactionException(String message){
        super(message);
    }
}
