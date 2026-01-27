package com.paymentservice.api.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "NotifyTransferation", url = "https://util.devi.tools/api/v1")
public interface NotificationProxy {

    @PostMapping("/notify")
    public void sendNotification();
}
