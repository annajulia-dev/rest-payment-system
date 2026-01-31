package com.paymentservice.api.enums;

public enum TransactionMessage {
    SAME_ACCOUNT("Você não pode transferir para si mesmo."),
    AUTHORIZATION_SERVICE_UNAVAILABLE("Serviço de autorização indisponível."),
    UNAUTHORIZED("Transferência não autorizada."),
    USER_NOT_FOUND("Usuário não encontrado: ID "),
    INSUFFICIENT_FUNDS("Saldo insuficiente para realizar a transação."),
    MERCHANT_TRANSFER("Mercantes não podem realizar transferências.");

    private final String message;

    TransactionMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
