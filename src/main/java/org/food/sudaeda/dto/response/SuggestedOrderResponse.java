package org.food.sudaeda.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.food.sudaeda.core.enums.SuggestedOrderStatus;

public record SuggestedOrderResponse(
        Long id,
        SuggestedOrderStatus status,
        @JsonProperty("order_id")
        Long orderId
) {
}
