package org.food.sudaeda.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.quartz.*;

import org.food.sudaeda.analytics.service.OrderUpdateService;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.enums.Role;
import org.food.sudaeda.core.jobs.CourierFindingJob;
import org.food.sudaeda.core.jobs.DeliveryCheckJob;
import org.food.sudaeda.core.jobs.OrderTimeoutJob;
import org.food.sudaeda.core.mappers.OrderMapper;
import org.food.sudaeda.core.model.User;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.core.repository.SuggestedOrdersRepository;
import org.food.sudaeda.core.repository.UserRepository;
import org.food.sudaeda.dto.request.*;
import org.food.sudaeda.dto.response.*;
import org.food.sudaeda.exception.*;
import org.food.sudaeda.utils.SecurityUtils;
import org.food.sudaeda.utils.TransactionHelper;
import org.food.sudaeda.core.model.Order;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final Scheduler scheduler;

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

        scheduleOrderTimeoutCheck(savedOrder.getId());
        return new CreateOrderResponse(savedOrder.getId());
    }

    private void scheduleOrderTimeoutCheck(Long orderId) {
        JobDetail job = JobBuilder.newJob(OrderTimeoutJob.class)
                .withIdentity("orderTimeout-" + orderId)
                .usingJobData(createCommonJobDataMap())
                .usingJobData("orderId", orderId)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(Instant.now().plus(Duration.ofMinutes(10))))
                .build();

        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule order timeout check", e);
        }
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
        JobDetail job = JobBuilder.newJob(CourierFindingJob.class)
                .withIdentity("courierFinding-" + order.getId())
                .usingJobData(createCommonJobDataMap())
                .usingJobData("orderId", order.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(FINDING_SCHEDULE_PERIOD_MILLIS)
                        .repeatForever())
                .endAt(Date.from(Instant.now().plusSeconds(FINDING_SCHEDULE_TIMEOUT_SECONDS)))
                .build();

        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule courier finding", e);
        }
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

        scheduleDeliveryCheck(order.getId(), deliveryTime);
        return new MarkAsReadyResponse(order.getId(), order.getStatus(), deliveryTime);
    }

    private void scheduleDeliveryCheck(Long orderId, LocalDateTime deliveryTime) {
        try {
            Order foundOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order not found"));

            // Рассчитываем оставшееся время до указанной даты доставки
            Duration orderDeliveryDuration = Duration.between(LocalDateTime.now(), deliveryTime);

            // Получаем стандартное время доставки из сервиса
            Duration serviceDeliveryDuration = deliveryService.getDeliveryTime(foundOrder);

            // Выбираем максимальную длительность
            Duration finalDuration = Collections.max(
                    Arrays.asList(orderDeliveryDuration, serviceDeliveryDuration),
                    Duration::compareTo
            );

            // Создаем триггер с рассчитанной задержкой
            Trigger trigger = TriggerBuilder.newTrigger()
                    .startAt(Date.from(Instant.now().plus(finalDuration)))
                    .build();

            // Создаем задание с передачей orderId
            JobDetail job = JobBuilder.newJob(DeliveryCheckJob.class)
                    .withIdentity("deliveryCheck-" + orderId)
                    .usingJobData(createCommonJobDataMap())
                    .usingJobData("orderId", orderId)
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException | NotFoundException e) {
            throw new RuntimeException("Failed to schedule delivery check", e);
        }
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
            throw new IllegalTransitionException("Transition between statuses " + fromStatus + " and " + toStatus + "is not allowed. Current status: " + order.getStatus());
        }

        var status = transactionHelper.createTransaction("updateStatus");

        Order savedOrder;
        try {
            order.setStatus(toStatus);
            savedOrder = orderRepository.save(order);
            orderUpdateService.add(savedOrder.getId(), fromStatus, toStatus);
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

    private JobDataMap createCommonJobDataMap() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("orderRepository", orderRepository);
        dataMap.put("userRepository", userRepository);
        dataMap.put("suggestedOrdersRepository", suggestedOrdersRepository);
        dataMap.put("transactionHelper", transactionHelper);
        dataMap.put("orderUpdateService", orderUpdateService);
        dataMap.put("deliveryService", deliveryService);
        return dataMap;
    }

    private static final Integer FINDING_SCHEDULE_TIMEOUT_SECONDS = 10 * 60;
    private static final Integer FINDING_SCHEDULE_PERIOD_MILLIS = 2 * 1000;
}
