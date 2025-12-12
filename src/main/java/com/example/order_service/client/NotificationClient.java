package com.example.order_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import  com.example.order_service.client.NotificationRequest;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://notification-service:8086/api/notifications")
            .build();

    public void sendOrderCreated(Long userId, Long orderId, String body) {

      NotificationRequest req = new NotificationRequest(
                userId,
                "ORDER_CREATED",
                "SYSTEM",
                "Ваш заказ #" + orderId + " создан",
                body,
                orderId
        );

        webClient.post()
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
