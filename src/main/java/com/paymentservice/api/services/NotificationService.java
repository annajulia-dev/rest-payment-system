package com.paymentservice.api.services;

import com.paymentservice.api.proxy.NotificationProxy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class NotificationService {

    private final NotificationProxy notificationProxy;
    private final Logger logger = Logger.getLogger(NotificationService.class.getName());

    public NotificationService(NotificationProxy notificationProxy){
        this.notificationProxy = notificationProxy;
    }

    @Async
    public void sendNotification(){
        try{
            notificationProxy.sendNotification();
        } catch (Exception e) {
            logger.info("Error sending the notification: " + e.getMessage());
        }
    }
}
