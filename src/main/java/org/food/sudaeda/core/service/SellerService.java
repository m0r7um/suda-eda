package org.food.sudaeda.core.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.dto.response.GetOrderResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final OrderRepository orderRepository;

    public List<GetOrderResponse> getSellerOrders(Long sellerId) {
        return orderRepository.findBySellerId(sellerId).stream().map((order) -> new GetOrderResponse(
                order.getId(),
                order.getStatus()
        )).toList();
    }
}
