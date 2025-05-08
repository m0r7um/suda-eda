package org.food.sudaeda.emailservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusUpdateNotification {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("new_status")
    private String newStatus;

    @JsonProperty("old_status")
    private String oldStatus;

    @JsonProperty("addressee")
    private String addressee;
}
