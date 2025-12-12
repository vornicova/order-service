package com.example.order_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private Long customerId;
    private String type;
    private String channel;
    private String subject;
    private String body;
    private Long orderId;
}
