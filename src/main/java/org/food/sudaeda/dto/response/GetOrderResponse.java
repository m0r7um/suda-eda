package org.food.sudaeda.dto.response;

import org.food.sudaeda.core.enums.OrderStatus;

import java.time.LocalDateTime;

public record GetOrderResponse(
        Long id,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime deliveryTime
) {
}
