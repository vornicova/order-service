package com.example.order_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderCreateRequestDto {

    private Long userId;
    private LocalDateTime pickupTime;
    private String comment;
    private List<OrderItemRequestDto> items;

    @Data
    public static class OrderItemRequestDto {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
