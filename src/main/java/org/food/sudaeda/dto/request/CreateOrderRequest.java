package org.food.sudaeda.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @JsonProperty("seller_id")
    private Long sellerId;
}
