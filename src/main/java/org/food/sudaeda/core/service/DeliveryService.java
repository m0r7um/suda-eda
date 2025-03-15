package org.food.sudaeda.core.service;

import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.enums.SuggestedOrderStatus;
import org.food.sudaeda.core.model.Order;
import org.food.sudaeda.core.model.User;
import org.food.sudaeda.core.repository.SuggestedOrdersRepository;
import org.food.sudaeda.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final SuggestedOrdersRepository suggestedOrdersRepository;

    public Duration getDeliveryTime(Order order) {
        User seller = order.getSeller();
        User courier = suggestedOrdersRepository
                .findByOrderAndStatus(order, SuggestedOrderStatus.ACCEPTED)
                .orElseThrow(() -> new NotFoundException("No accepted courier found"))
                .getCourier();

        // TODO real calculation of time between seller and courier
        return Duration.ofMinutes(10);
    }
}
