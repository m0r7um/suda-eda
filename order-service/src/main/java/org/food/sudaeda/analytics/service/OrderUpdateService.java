package org.food.sudaeda.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.sudaeda.analytics.model.OrderUpdate;
import org.food.sudaeda.analytics.repository.OrderUpdateRepository;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.exception.NotFoundException;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderUpdateService {
    private final OrderUpdateRepository orderUpdateRepository;

    public OrderUpdate add(Long orderId, OrderStatus fromStatus, OrderStatus toStatus) {
        OrderUpdate orderUpdate = OrderUpdate.builder()
                .orderId(orderId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .createdAt(LocalDateTime.now())
                .isSent(false)
                .build();
        return orderUpdateRepository.save(orderUpdate);
    }

    public OrderUpdate getOrderUpdateById(Long id) {
        return orderUpdateRepository.findById(id).orElseThrow(() -> new NotFoundException("Order update not found"));
    }
}
