package com.example.order_service.controller;

import com.example.order_service.dto.OrderCreateRequestDto;
import com.example.order_service.dto.OrderResponseDto;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponseDto createOrder(@RequestBody OrderCreateRequestDto request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponseDto getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponseDto> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @PatchMapping("/{id}/status")
    public OrderResponseDto updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return orderService.updateStatus(id, status);
    }

    /**
     * Универсальный GET:
     * - без параметров        → ВСЕ заказы (для админки)
     * - ?status=READY         → все со статусом READY (для фильтров в админке)
     * - ?customerId=123       → заказы конкретного покупателя (личный кабинет)
     * - ?customerId=123&status=READY → если захочешь добавить комбинированный фильтр
     */
    @GetMapping
    public List<OrderResponseDto> getOrders(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "status", required = false) String status
    ) {
        if (customerId != null && status != null) {
            return orderService.getOrdersByUserIdAndStatus(customerId, status);
        }
        if (customerId != null) {
            return orderService.getOrdersByUserId(customerId);
        }
        if (status != null) {
            return orderService.getOrdersByStatus(status);
        }
        return orderService.getAllOrders();
    }
}
