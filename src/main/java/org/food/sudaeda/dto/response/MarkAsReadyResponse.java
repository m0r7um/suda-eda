package org.food.sudaeda.dto.response;

import org.food.sudaeda.core.enums.OrderStatus;

import java.time.LocalDateTime;

public record MarkAsReadyResponse(
        Long id,
        OrderStatus status,
        LocalDateTime deliveryTime
) {
}
