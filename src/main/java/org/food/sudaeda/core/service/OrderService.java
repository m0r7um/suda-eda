package org.food.sudaeda.core.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.enums.Role;
import org.food.sudaeda.core.enums.SuggestedOrderStatus;
import org.food.sudaeda.core.mappers.OrderMapper;
import org.food.sudaeda.core.model.SuggestedOrder;
import org.food.sudaeda.core.model.User;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.core.repository.SuggestedOrdersRepository;
import org.food.sudaeda.core.repository.UserRepository;
import org.food.sudaeda.dto.request.*;
import org.food.sudaeda.dto.response.*;
import org.food.sudaeda.exception.*;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.food.sudaeda.core.model.Order;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SuggestedOrdersRepository suggestedOrdersRepository;
    private final DeliveryService deliveryService;
    private final OrderMapper orderMapper;

    public CreateOrderResponse createNewOrder(CreateOrderRequest request) {
        User seller = userRepository.findById(request.getSellerId()).orElseThrow(() -> new NotFoundException("User not found"));
        if (seller.getRole() != Role.SELLER) throw new WrongSellerRoleException("Found user has wrong role");
        Order order = new Order();
        order.setSeller(seller);
        order.setCreatedAt(LocalDateTime.now());
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
        return orderMapper.orderToResponse(order);
    }

    public ProcessNewOrderBySellerResponse processNewOrder(Long orderId, ProcessNewOrderBySellerRequest request, boolean accepted) {
        Order order = validateOrderUpdate(orderId, request.getSellerId());

        if (!order.getStatus().equals(OrderStatus.NEW_ORDER)) {
            throw new IllegalTransitionException("You can accept/reject only new orders");
        }

        if (accepted) {
            return acceptNewOrder(order);
        }
        return rejectNewOrder(order);
    }

    private ProcessNewOrderBySellerResponse acceptNewOrder(Order order) {
        order.setStatus(OrderStatus.APPROVED_BY_SELLER);
        findCourier(order);
        Order savedOrder = orderRepository.save(order);
        return new ProcessNewOrderBySellerResponse(savedOrder.getId(), savedOrder.getStatus());
    }

    private ProcessNewOrderBySellerResponse rejectNewOrder(Order order) {
        order.setStatus(OrderStatus.REJECTED_BY_SELLER);
        Order savedOrder = orderRepository.save(order);
        return new ProcessNewOrderBySellerResponse(savedOrder.getId(), savedOrder.getStatus());
    }

    private void findCourier(Order order) {
        new Timer().schedule(new TimerTask() {
            final Instant start = Instant.now();
            final Instant finish = Instant.now().plusSeconds(FINDING_SCHEDULE_TIMEOUT_SECONDS);
            @Override
            public void run() {
                Optional<SuggestedOrder> orderSuggestion = suggestedOrdersRepository.findByOrderAndStatusNot(
                        order,
                        SuggestedOrderStatus.REJECTED
                );
                if (orderSuggestion.isEmpty()) {
                    SuggestedOrder newOrderSuggestion = new SuggestedOrder();
                    User courier = findCourier();
                    newOrderSuggestion.setCourier(courier);
                    newOrderSuggestion.setOrder(order);
                    newOrderSuggestion.setStatus(SuggestedOrderStatus.PENDING);
                    suggestedOrdersRepository.save(newOrderSuggestion);
                } else {
                    SuggestedOrder suggestedOrder = orderSuggestion.get();
                    if (suggestedOrder.getStatus().equals(SuggestedOrderStatus.ACCEPTED)) {
                        order.setStatus(OrderStatus.APPROVED_BY_COURIER);
                    }
                    cancel();
                    return;
                }

                if (start.compareTo(finish) > 0) {
                    order.setStatus(OrderStatus.COURIER_NOT_FOUND);
                    cancel();
                }
            }
        },0, FINDING_SCHEDULE_PERIOD_MILLIS);
    }

    private User findCourier() {
        List<User> freeCourier = userRepository.findFreeCouriers(Limit.of(1));
        if (freeCourier.isEmpty()) throw new NotFoundException("Free courier not found");
        return freeCourier.get(0);
    }

    public MarkAsStartedResponse markAsStarted(Long orderId, MarkAsStartedRequest request) {
        Order order = updateStatus(
                validateOrderUpdate(orderId, request.getSellerId()),
                OrderStatus.APPROVED_BY_COURIER,
                OrderStatus.ORDER_IN_PROGRESS
        );
        return new MarkAsStartedResponse(order.getId(), order.getStatus());
    }

    public MarkAsReadyResponse markAsReady(Long orderId, MarkAsReadyRequest request) {
        Order order = updateStatus(
                validateOrderUpdate(orderId, request.getSellerId()),
                OrderStatus.ORDER_IN_PROGRESS,
                OrderStatus.ORDER_READY
        );
        LocalDateTime deliveryTime = LocalDateTime.now().plus(deliveryService.getDeliveryTime(order));
        order.setDeliveryTime(deliveryTime);

        new Thread(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Order not found"));

            try {
                Duration orderDeliveryTime = Duration.between(LocalDateTime.now(), foundOrder.getDeliveryTime());
                Duration deliveryTimeDeadline = Collections.max(Arrays.asList(
                        orderDeliveryTime,
                        deliveryService.getDeliveryTime(foundOrder)
                ), Duration::compareTo);

                Thread.sleep(deliveryTimeDeadline.toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (foundOrder.getStatus() == OrderStatus.ORDER_READY) {
                foundOrder.setStatus(OrderStatus.ORDER_NOT_PICKED_UP_BY_COURIER);
            }
            orderRepository.save(foundOrder);
        }).start();

        return new MarkAsReadyResponse(order.getId(), order.getStatus(), deliveryTime);
    }

    public MarkAsPickedUpResponse markAsPickedUp(Long orderId, MarkAsPickedUpRequest request) {
        Order order = updateStatus(
                validateOrderUpdate(orderId, request.getSellerId()),
                OrderStatus.ORDER_READY,
                OrderStatus.ORDER_PICKED_UP_BY_COURIER
        );
        return new MarkAsPickedUpResponse(order.getId(), order.getStatus());
    }

    private Order updateStatus(Order order, OrderStatus fromStatus, OrderStatus toStatus) {
        if (!order.getStatus().equals(fromStatus)) {
            throw new IllegalTransitionException("Transition between statuses " + fromStatus + " and " + toStatus + "is not allowed.");
        }

        order.setStatus(toStatus);
        return orderRepository.save(order);
    }

    private Order validateOrderUpdate(Long orderId, Long sellerId) {
        User seller = userRepository.findById(sellerId).orElseThrow(() -> new NotFoundException("User not found"));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        if (seller.getRole() != Role.SELLER) throw new WrongSellerRoleException("Found user has wrong role");

        if (!order.getSeller().equals(seller)) {
            throw new AccessViolationException("You are not allowed to update this order");
        }

        return order;
    }

    private static final Integer FINDING_SCHEDULE_TIMEOUT_SECONDS = 10 * 60;
    private static final Integer FINDING_SCHEDULE_PERIOD_MILLIS = 2 * 1000;
}
