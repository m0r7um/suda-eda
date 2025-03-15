package org.food.sudaeda.dto.response;

import org.food.sudaeda.core.enums.OrderStatus;

public record MarkAsPickedUpResponse(
        Long id,
        OrderStatus status
) {
}
