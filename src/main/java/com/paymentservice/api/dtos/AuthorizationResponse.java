package com.paymentservice.api.dtos;

public record AuthorizationResponse(
        String status,
        AuthorizationData data
) {
    public record AuthorizationData(boolean authorization){}
}
