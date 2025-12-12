package com.example.order_service.service;

import com.example.order_service.dto.OrderCreateRequestDto;
import com.example.order_service.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderCreateRequestDto request);

    OrderResponseDto getOrderById(Long id);

    List<OrderResponseDto> getOrdersByUserId(Long userId);

    OrderResponseDto updateStatus(Long id, String newStatus);

    List<OrderResponseDto> getAllOrders();

    List<OrderResponseDto> getOrdersByStatus(String status);

    List<OrderResponseDto> getOrdersByUserIdAndStatus(Long userId, String status);
}

