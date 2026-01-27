package com.paymentservice.api.proxy;

import com.paymentservice.api.dtos.AuthorizationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "TransferAuthorization", url = "https://util.devi.tools/api/v2")
public interface TransferAuthorizationProxy {

    @GetMapping("/authorize")
    public AuthorizationResponse authorizeTransfer();
}
