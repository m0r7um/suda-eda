package org.food.sudaeda.core.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.mappers.OrderMapper;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.dto.response.GetOrderResponse;
import org.food.sudaeda.utils.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public List<GetOrderResponse> getSellerOrders() {
        return orderRepository.findBySellerId(SecurityUtils.getUserId()).stream().map(orderMapper::orderToResponse).toList();
    }

    public List<GetOrderResponse> getSellerOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusAndSellerId(SecurityUtils.getUserId(), status).stream().map(orderMapper::orderToResponse).toList();
    }
}
