package org.food.sudaeda.core.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.enums.SuggestedOrderStatus;
import org.food.sudaeda.core.model.Order;
import org.food.sudaeda.core.model.SuggestedOrder;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.core.repository.SuggestedOrdersRepository;
import org.food.sudaeda.dto.response.SuggestedOrderResponse;
import org.food.sudaeda.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SuggestedOrderService {
    private final SuggestedOrdersRepository suggestedOrdersRepository;
    private final OrderRepository orderRepository;

    public List<SuggestedOrderResponse> findPendingByCourier(Long courierId) {
        List<SuggestedOrder> suggestedPendingOrders = suggestedOrdersRepository.findByCourier_IdAndStatus(courierId, SuggestedOrderStatus.PENDING);
        return suggestedPendingOrders.stream().map(
                (suggestedOrder) -> new SuggestedOrderResponse(
                        suggestedOrder.getId(),
                        suggestedOrder.getStatus(),
                        suggestedOrder.getOrder().getId()
                )
        ).toList();
    }

    @Transactional
    public SuggestedOrderResponse acceptSuggestion(Long suggestedOrderId) {
        SuggestedOrder suggestedOrder = suggestedOrdersRepository.findById(suggestedOrderId).orElseThrow(() -> new NotFoundException("Suggested order not found " + suggestedOrderId));
        suggestedOrder.setStatus(SuggestedOrderStatus.ACCEPTED);
        SuggestedOrder savedSuggestedOrder = suggestedOrdersRepository.save(suggestedOrder);
        Order foundOrder = orderRepository.findById(savedSuggestedOrder.getOrder().getId()).orElseThrow(() -> new NotFoundException("Order not found"));
        foundOrder.setStatus(OrderStatus.APPROVED_BY_COURIER);
        orderRepository.save(foundOrder);
        return new SuggestedOrderResponse(
                savedSuggestedOrder.getId(),
                savedSuggestedOrder.getStatus(),
                savedSuggestedOrder.getOrder().getId()
        );
    }
}
