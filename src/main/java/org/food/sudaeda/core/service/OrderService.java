package org.food.sudaeda.core.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.enums.Role;
import org.food.sudaeda.core.model.User;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.core.repository.UserRepository;
import org.food.sudaeda.dto.request.CreateOrderRequest;
import org.food.sudaeda.dto.request.ProcessNewOrderBySellerRequest;
import org.food.sudaeda.dto.response.CreateOrderResponse;
import org.food.sudaeda.dto.response.GetOrderResponse;
import org.food.sudaeda.dto.response.ProcessNewOrderBySellerResponse;
import org.food.sudaeda.exception.AccessViolationException;
import org.food.sudaeda.exception.NotFoundException;
import org.food.sudaeda.exception.WrongSellerRoleException;
import org.springframework.stereotype.Service;
import org.food.sudaeda.core.model.Order;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public CreateOrderResponse createNewOrder(CreateOrderRequest request) {
        User seller = userRepository.findById(request.getSellerId()).orElseThrow(() -> new NotFoundException("User not found"));
        if (seller.getRole() != Role.SELLER) throw new WrongSellerRoleException("Found user has wrong role");
        Order order = new Order();
        order.setSeller(seller);
        order.setStatus(OrderStatus.NEW_ORDER);
        Order savedOrder = orderRepository.save(order);
        new Thread(
                () -> {
                    try {
                        Thread.sleep(Duration.ofMinutes(10).toMillis());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Order foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow(() -> new NotFoundException("Order not found"));
                    if (foundOrder.getStatus() == OrderStatus.NEW_ORDER) {
                        foundOrder.setStatus(OrderStatus.SELLER_NOT_ANSWERED);
                    }
                    orderRepository.save(foundOrder);
                }
        ).start();
        return new CreateOrderResponse(savedOrder.getId());
    }

    public GetOrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        return new GetOrderResponse(
                order.getId(),
                order.getStatus()
        );
    }

    public ProcessNewOrderBySellerResponse processNewOrder(Long orderId, ProcessNewOrderBySellerRequest request, boolean accepted) {
        User seller = userRepository.findById(request.getSellerId()).orElseThrow(() -> new NotFoundException("User not found"));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));

        if (seller.getRole() != Role.SELLER) throw new WrongSellerRoleException("Found user has wrong role");
        if (!order.getSeller().equals(seller)) {
            throw new AccessViolationException("You are not allowed to reject this order");
        }

        if (accepted) {
            return acceptNewOrder(order);
        }
        return rejectNewOrder(order);
    }

    private ProcessNewOrderBySellerResponse acceptNewOrder(Order order) {
        order.setStatus(OrderStatus.APPROVED_BY_COURIER);
        // TODO add further logic for accepting order
        Order savedOrder = orderRepository.save(order);
        return new ProcessNewOrderBySellerResponse(savedOrder.getId(), savedOrder.getStatus());
    }

    private ProcessNewOrderBySellerResponse rejectNewOrder(Order order) {
        order.setStatus(OrderStatus.REJECTED_BY_SELLER);
        Order savedOrder = orderRepository.save(order);
        return new ProcessNewOrderBySellerResponse(savedOrder.getId(), savedOrder.getStatus());
    }
}
