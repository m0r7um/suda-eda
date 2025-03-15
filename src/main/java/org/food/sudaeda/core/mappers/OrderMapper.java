package org.food.sudaeda.core.mappers;

import org.food.sudaeda.core.model.Order;
import org.food.sudaeda.dto.response.GetOrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public GetOrderResponse orderToResponse(Order order) {
        return new GetOrderResponse(
                order.getId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getDeliveryTime()
        );
    }
}
