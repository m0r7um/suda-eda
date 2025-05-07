package org.food.sudaeda.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateOrderResponse(@JsonProperty("order_id") Long orderId) { }
