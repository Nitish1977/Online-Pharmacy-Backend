package com.pharmacy.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
public class IdentityServiceApplication {
    public static void main(String[] args) {
        // Force IPv4 to fix DNS resolution issues on Windows
        System.setProperty("java.net.preferIPv4Stack", "true");
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
