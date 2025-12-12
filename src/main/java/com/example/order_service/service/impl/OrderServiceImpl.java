package com.example.order_service.service.impl;

import com.example.order_service.client.NotificationClient;
import com.example.order_service.client.user.UserClient;
import com.example.order_service.dto.OrderCreateRequestDto;
import com.example.order_service.dto.OrderResponseDto;
import com.example.order_service.dto.UserResponseDto;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final NotificationClient notificationClient;

    @Override
    public OrderResponseDto createOrder(OrderCreateRequestDto request) {

        LocalDateTime now = LocalDateTime.now();

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        List<OrderItem> items = request.getItems().stream()
                .map(itemDto -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemDto.getProductId());

                    String productName =
                            itemDto.getProductName() != null ? itemDto.getProductName() : "";
                    BigDecimal unitPrice =
                            itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO;
                    int quantity =
                            itemDto.getQuantity() != null ? itemDto.getQuantity() : 0;

                    item.setProductName(productName);
                    item.setQuantity(quantity);
                    item.setUnitPrice(unitPrice);
                    item.setLinePrice(unitPrice.multiply(BigDecimal.valueOf(quantity))); // ⬅️ всегда не null

                    item.setOrder(order);
                    return item;
                })
                .collect(toList());


        BigDecimal total = items.stream()
                .map(OrderItem::getLinePrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);
        notificationClient.sendOrderCreated(
                saved.getUserId(),
                saved.getId(),
                "Спасибо за заказ! Мы приступаем к приготовлению 🎂"
        );

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    public OrderResponseDto updateStatus(Long id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        order.setStatus(OrderStatus.valueOf(newStatus));
        order.setUpdatedAt(LocalDateTime.now());

        return toDto(order);
    }

    private OrderResponseDto toDto(Order order) {
        List<OrderItem> orderItems =
                order.getItems() != null ? order.getItems() : Collections.emptyList();

        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(orderItems.stream()
                        .map(item -> OrderResponseDto.OrderItemResponseDto.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .linePrice(item.getLinePrice())
                                .build())
                        .collect(toList()))
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(String status) {
        OrderStatus st = OrderStatus.valueOf(status);
        return orderRepository.findByStatus(st)
                .stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUserIdAndStatus(Long userId, String status) {
        OrderStatus st = OrderStatus.valueOf(status);
        return orderRepository.findByUserIdAndStatus(userId, st)
                .stream()
                .map(this::toDto)
                .collect(toList());
    }


}
