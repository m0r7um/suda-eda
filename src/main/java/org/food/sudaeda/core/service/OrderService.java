package org.food.sudaeda.core.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.sudaeda.analytics.service.OrderUpdateService;
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
import org.food.sudaeda.utils.SecurityUtils;
import org.food.sudaeda.utils.TransactionHelper;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.food.sudaeda.core.model.Order;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SuggestedOrdersRepository suggestedOrdersRepository;
    private final DeliveryService deliveryService;
    private final OrderUpdateService orderUpdateService;
    private final OrderMapper orderMapper;
    private final TransactionHelper transactionHelper;

    public CreateOrderResponse createNewOrder(CreateOrderRequest request) {
        User seller = userRepository.findById(request.getSellerId()).orElseThrow(() -> new NotFoundException("User not found"));
        if (seller.getRole().getAuthority() != Role.ROLE_SELLER)
            throw new WrongSellerRoleException("Found user has wrong role");
        Order order = new Order();
        order.setSeller(seller);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW_ORDER);

        var status = transactionHelper.createTransaction("createOrder");
        Order savedOrder;
        try {
            savedOrder = orderRepository.save(order);
            orderUpdateService.add(savedOrder.getId(), null, OrderStatus.NEW_ORDER);
            transactionHelper.commit(status);
        } catch (Exception e) {
            transactionHelper.rollback(status);
            throw e;
        }

        new Thread(
            () -> {
                try {
                    Thread.sleep(Duration.ofMinutes(10).toMillis());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                var updateStatus = transactionHelper.createTransaction("updateStatusSellerNotAnswered");
                try {
                    Order foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow(() -> new NotFoundException("Order not found"));
                    if (foundOrder.getStatus() == OrderStatus.NEW_ORDER) {
                        foundOrder.setStatus(OrderStatus.SELLER_NOT_ANSWERED);
                        orderUpdateService.add(foundOrder.getId(), OrderStatus.NEW_ORDER, OrderStatus.SELLER_NOT_ANSWERED);
                    }
                    orderRepository.save(foundOrder);
                    transactionHelper.commit(updateStatus);
                } catch (Exception e) {
                    transactionHelper.rollback(updateStatus);
                    throw e;
                }
            }
        ).start();

        return new CreateOrderResponse(savedOrder.getId());
    }

    public GetOrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        return orderMapper.orderToResponse(order);
    }

    public ProcessNewOrderBySellerResponse processNewOrder(Long orderId, boolean accepted) {
        Order order = validateOrderUpdate(orderId, SecurityUtils.getUserId());

        if (!order.getStatus().equals(OrderStatus.NEW_ORDER)) {
            throw new IllegalTransitionException("You can accept/reject only new orders");
        }

        if (accepted) {
            return acceptNewOrder(order);
        }
        return rejectNewOrder(order);
    }

    private ProcessNewOrderBySellerResponse acceptNewOrder(Order order) {
        var status = transactionHelper.createTransaction("acceptNewOrder");

        Order savedOrder;
        try {
            order.setStatus(OrderStatus.APPROVED_BY_SELLER);
            orderUpdateService.add(order.getId(), OrderStatus.NEW_ORDER, OrderStatus.APPROVED_BY_SELLER);
            savedOrder = orderRepository.save(order);
            transactionHelper.commit(status);
        } catch (Exception e) {
            transactionHelper.rollback(status);
            throw e;
        }

        findCourier(savedOrder);
        return new ProcessNewOrderBySellerResponse(savedOrder.getId(), savedOrder.getStatus());
    }

    private ProcessNewOrderBySellerResponse rejectNewOrder(Order order) {
        var status = transactionHelper.createTransaction("rejectNewOrder");

        Order savedOrder;
        try {
            order.setStatus(OrderStatus.REJECTED_BY_SELLER);
            orderUpdateService.add(order.getId(), OrderStatus.NEW_ORDER, OrderStatus.REJECTED_BY_SELLER);
            savedOrder = orderRepository.save(order);
            transactionHelper.commit(status);
        } catch (Exception e) {
            transactionHelper.rollback(status);
            throw e;
        }

        return new ProcessNewOrderBySellerResponse(savedOrder.getId(), savedOrder.getStatus());
    }

    private void findCourier(Order order) {
        log.debug("Started courier finding");
        new Timer().schedule(new TimerTask() {
            final Instant start = Instant.now();
            final Instant finish = Instant.now().plusSeconds(FINDING_SCHEDULE_TIMEOUT_SECONDS);
            @Override
            public void run() {
                log.debug("Attempt to find courier");
                Optional<SuggestedOrder> orderSuggestion = suggestedOrdersRepository.findByOrderAndStatusNot(
                        order,
                        SuggestedOrderStatus.REJECTED
                );
                if (orderSuggestion.isEmpty()) {
                    log.debug("No suggested order found with not rejected status");
                    SuggestedOrder newOrderSuggestion = new SuggestedOrder();
                    User courier = findCourier();
                    newOrderSuggestion.setCourier(courier);
                    newOrderSuggestion.setOrder(order);
                    newOrderSuggestion.setStatus(SuggestedOrderStatus.PENDING);
                    suggestedOrdersRepository.save(newOrderSuggestion);
                } else {
                    SuggestedOrder suggestedOrder = orderSuggestion.get();

                    if (suggestedOrder.getStatus().equals(SuggestedOrderStatus.ACCEPTED)) {
                        var status = transactionHelper.createTransaction("suggestedOrderAccepted");
                        try {
                            order.setStatus(OrderStatus.APPROVED_BY_COURIER);
                            orderUpdateService.add(order.getId(), OrderStatus.APPROVED_BY_SELLER, OrderStatus.APPROVED_BY_COURIER);
                            orderRepository.save(order);
                            transactionHelper.commit(status);
                        } catch (Exception e) {
                            transactionHelper.rollback(status);
                            throw e;
                        }

                        log.debug("Courier found {}", suggestedOrder.getCourier().getId());
                        cancel();
                    }
                    return;
                }

                if (start.compareTo(finish) > 0) {
                    var status = transactionHelper.createTransaction("courierNotfound");
                    try {
                        order.setStatus(OrderStatus.COURIER_NOT_FOUND);
                        orderUpdateService.add(order.getId(), OrderStatus.APPROVED_BY_SELLER, OrderStatus.COURIER_NOT_FOUND);
                        orderRepository.save(order);
                        transactionHelper.commit(status);
                    } catch (Exception e) {
                        transactionHelper.rollback(status);
                        throw e;
                    }

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

    public MarkAsStartedResponse markAsStarted(Long orderId) {
        Order order = updateStatus(
                validateOrderUpdate(orderId, SecurityUtils.getUserId()),
                OrderStatus.APPROVED_BY_COURIER,
                OrderStatus.ORDER_IN_PROGRESS
        );
        return new MarkAsStartedResponse(order.getId(), order.getStatus());
    }

    public MarkAsReadyResponse markAsReady(Long orderId) {
        Order order = updateStatus(
                validateOrderUpdate(orderId, SecurityUtils.getUserId()),
                OrderStatus.ORDER_IN_PROGRESS,
                OrderStatus.ORDER_READY
        );
        LocalDateTime deliveryTime = LocalDateTime.now().plus(deliveryService.getDeliveryTime(order));
        order.setDeliveryTime(deliveryTime);
        orderRepository.save(order);

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

            var status = transactionHelper.createTransaction("markAsReady");
            try {
                if (foundOrder.getStatus() == OrderStatus.ORDER_READY) {
                    foundOrder.setStatus(OrderStatus.ORDER_NOT_PICKED_UP_BY_COURIER);
                    orderUpdateService.add(foundOrder.getId(), OrderStatus.APPROVED_BY_COURIER, OrderStatus.ORDER_NOT_PICKED_UP_BY_COURIER);
                    orderRepository.save(order);
                }
                transactionHelper.commit(status);
            } catch (Exception e) {
                transactionHelper.rollback(status);
                throw e;
            }
        }).start();

        return new MarkAsReadyResponse(order.getId(), order.getStatus(), deliveryTime);
    }

    public MarkAsPickedUpResponse markAsPickedUp(Long orderId) {
        Order order = updateStatus(
                validateOrderUpdate(orderId, SecurityUtils.getUserId()),
                OrderStatus.ORDER_READY,
                OrderStatus.ORDER_PICKED_UP_BY_COURIER
        );
        return new MarkAsPickedUpResponse(order.getId(), order.getStatus());
    }

    private Order updateStatus(Order order, OrderStatus fromStatus, OrderStatus toStatus) {
        if (!order.getStatus().equals(fromStatus)) {
            throw new IllegalTransitionException("Transition between statuses " + fromStatus + " and " + toStatus + "is not allowed.");
        }

        var status = transactionHelper.createTransaction("updateStatus");

        Order savedOrder;
        try {
            order.setStatus(toStatus);
            orderUpdateService.add(order.getId(), fromStatus, toStatus);
            savedOrder = orderRepository.save(order);
            transactionHelper.commit(status);
        } catch (Exception e) {
            transactionHelper.rollback(status);
            throw e;
        }

        return savedOrder;
    }

    private Order validateOrderUpdate(Long orderId, Long sellerId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getSeller().getId().equals(sellerId)) {
            throw new AccessViolationException("You are not allowed to update this order");
        }

        return order;
    }

    private static final Integer FINDING_SCHEDULE_TIMEOUT_SECONDS = 10 * 60;
    private static final Integer FINDING_SCHEDULE_PERIOD_MILLIS = 2 * 1000;
}
