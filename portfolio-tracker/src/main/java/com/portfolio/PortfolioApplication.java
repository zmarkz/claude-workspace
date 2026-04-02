package com.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

// Main Application Class
@SpringBootApplication
@EnableScheduling
@EnableWebSocket
public class PortfolioApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }
}