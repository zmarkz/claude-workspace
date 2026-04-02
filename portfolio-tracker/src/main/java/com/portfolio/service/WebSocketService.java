package com.portfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastPriceUpdates(Map<String, BigDecimal> prices) {
        try {
            messagingTemplate.convertAndSend("/topic/prices", prices);
        } catch (Exception e) {
            log.error("Error broadcasting price updates via WebSocket", e);
        }
    }
}
