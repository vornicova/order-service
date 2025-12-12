package com.example.order_service.service.impl;

import com.example.order_service.dto.OrderCreateRequestDto;
import com.example.order_service.dto.OrderResponseDto;
import com.example.order_service.entity.Order;
import com.example.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.order_service.client.NotificationClient;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_shouldCalculateTotalSaveOrderAndSendNotification() {
        // given
        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setUserId(10L);

        var item1 = new OrderCreateRequestDto.OrderItemRequestDto();
        item1.setProductId(1L);
        item1.setProductName("Cake 1");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("100.00"));

        var item2 = new OrderCreateRequestDto.OrderItemRequestDto();
        item2.setProductId(2L);
        item2.setProductName("Cake 2");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("50.00"));

        request.setItems(List.of(item1, item2));

        // настроим, что репозиторий вернёт уже сохранённый заказ
        Order saved = new Order();
        saved.setId(1L);
        saved.setUserId(10L);
        saved.setStatus(OrderStatus.NEW);
        saved.setTotalPrice(new BigDecimal("250.00"));
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(saved.getCreatedAt());
        saved.setItems(Collections.emptyList());

        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        // when
        OrderResponseDto result = orderService.createOrder(request);

        // then: проверяем, что сохранился корректный total
        verify(orderRepository).save(orderCaptor.capture());
        Order orderToSave = orderCaptor.getValue();

        assertEquals(10L, orderToSave.getUserId());
        assertEquals(OrderStatus.NEW, orderToSave.getStatus());
        assertEquals(new BigDecimal("250.00"), orderToSave.getTotalPrice());

        // проверяем вызов нотификаций
        verify(notificationClient).sendOrderCreated(
                10L,
                1L,
                "Спасибо за заказ! Мы приступаем к приготовлению 🎂"
        );

        // проверяем маппинг в DTO
        assertEquals(1L, result.getId());
        assertEquals(10L, result.getUserId());
        assertEquals("NEW", result.getStatus());
        assertEquals(new BigDecimal("250.00"), result.getTotalPrice());
    }


    @Test
    void getOrderById_shouldReturnDto_whenOrderExists() {
        // given
        Order order = new Order();
        order.setId(1L);
        order.setUserId(11L);
        order.setStatus(OrderStatus.NEW);
        order.setTotalPrice(new BigDecimal("100.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(order.getCreatedAt());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        OrderResponseDto dto = orderService.getOrderById(1L);

        // then
        assertEquals(1L, dto.getId());
        assertEquals(11L, dto.getUserId());
        assertEquals("NEW", dto.getStatus());
        assertEquals(new BigDecimal("100.00"), dto.getTotalPrice());
    }

    @Test
    void getOrderById_shouldThrow_whenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.getOrderById(999L)
        );

        assertTrue(ex.getMessage().contains("Order not found: 999"));
    }

    @Test
    void getOrdersByUserId_shouldMapAllOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setUserId(7L);
        order1.setStatus(OrderStatus.NEW);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(7L);
        order2.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findByUserId(7L)).thenReturn(List.of(order1, order2));

        // when
        List<OrderResponseDto> dtos = orderService.getOrdersByUserId(7L);

        // then
        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(2L, dtos.get(1).getId());
    }

    @Test
    void updateStatus_shouldChangeStatusAndReturnDto() {
        // given
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(order.getCreatedAt());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        OrderResponseDto dto = orderService.updateStatus(1L, "DELIVERED");

        // then
        assertEquals("DELIVERED", dto.getStatus());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(order.getUpdatedAt());
    }

    @Test
    void getOrdersByStatus_shouldFilterByStatus() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.NEW);

        when(orderRepository.findByStatus(OrderStatus.NEW))
                .thenReturn(List.of(order1));

        List<OrderResponseDto> dtos = orderService.getOrdersByStatus("NEW");

        assertEquals(1, dtos.size());
        assertEquals("NEW", dtos.get(0).getStatus());
    }

    @Test
    void getOrdersByUserIdAndStatus_shouldFilterByUserAndStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(3L);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findByUserIdAndStatus(3L, OrderStatus.DELIVERED))
                .thenReturn(List.of(order));

        List<OrderResponseDto> dtos =
                orderService.getOrdersByUserIdAndStatus(3L, "DELIVERED");

        assertEquals(1, dtos.size());
        assertEquals(3L, dtos.get(0).getUserId());
        assertEquals("DELIVERED", dtos.get(0).getStatus());
    }
}
