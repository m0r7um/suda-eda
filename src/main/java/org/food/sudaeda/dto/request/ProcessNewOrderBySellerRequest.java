package org.food.sudaeda.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProcessNewOrderBySellerRequest {
    @JsonProperty("seller_id")
    private Long sellerId;
}
