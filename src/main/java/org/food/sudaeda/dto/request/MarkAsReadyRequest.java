package org.food.sudaeda.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MarkAsReadyRequest {
    @JsonProperty("seller_id")
    private Long sellerId;
}
