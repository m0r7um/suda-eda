package org.food.sudaeda.core.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.mappers.OrderMapper;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.dto.response.GetOrderResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public List<GetOrderResponse> getSellerOrders(Long sellerId) {
        return orderRepository.findBySellerId(sellerId).stream().map(orderMapper::orderToResponse).toList();
    }

    public List<GetOrderResponse> getSellerOrdersByStatus(Long sellerId, OrderStatus status) {
        return orderRepository.findByStatusAndSellerId(sellerId, status).stream().map(orderMapper::orderToResponse).toList();
    }
}
