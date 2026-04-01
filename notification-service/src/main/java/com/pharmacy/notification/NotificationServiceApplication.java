package com.pharmacy.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NotificationServiceApplication {
    public static void main(String[] args) {
        // Force IPv4 for Windows DNS resolution consistency
        System.setProperty("java.net.preferIPv4Stack", "true");
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
