package org.food.sudaeda.dto.response;

import org.food.sudaeda.core.enums.OrderStatus;

public record MarkAsStartedResponse(
        Long id,
        OrderStatus status
) {
}
