package org.food.sudaeda.core.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.quartz.*;

import org.food.sudaeda.analytics.service.OrderUpdateService;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.enums.SuggestedOrderStatus;
import org.food.sudaeda.core.model.Order;
import org.food.sudaeda.core.model.SuggestedOrder;
import org.food.sudaeda.core.model.User;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.core.repository.SuggestedOrdersRepository;
import org.food.sudaeda.core.repository.UserRepository;
import org.food.sudaeda.exception.NotFoundException;
import org.food.sudaeda.utils.TransactionHelper;

import java.util.List;
import java.util.Optional;

@Slf4j
public class CourierFindingJob implements InterruptableJob {
    private volatile boolean interrupted = false;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Started courier finding");

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        SuggestedOrdersRepository suggestedOrdersRepository =
                (SuggestedOrdersRepository) dataMap.get("suggestedOrdersRepository");
        OrderRepository orderRepository = (OrderRepository) dataMap.get("orderRepository");
        UserRepository userRepository = (UserRepository) dataMap.get("userRepository");
        TransactionHelper transactionHelper = (TransactionHelper) dataMap.get("transactionHelper");
        OrderUpdateService orderUpdateService = (OrderUpdateService) dataMap.get("orderUpdateService");
        Long orderId = dataMap.getLong("orderId");

        log.debug("Attempt to find courier");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        Optional<SuggestedOrder> orderSuggestion = suggestedOrdersRepository
                .findByOrderAndStatusNot(order, SuggestedOrderStatus.REJECTED);

        if (orderSuggestion.isEmpty()) {
            log.debug("No suggested order found with not rejected status");
            SuggestedOrder newOrderSuggestion = new SuggestedOrder();
            User courier = findCourier(userRepository);
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
                    orderUpdateService.add(order.getId(),
                            OrderStatus.APPROVED_BY_SELLER,
                            OrderStatus.APPROVED_BY_COURIER);
                    orderRepository.save(order);
                    transactionHelper.commit(status);
                } catch (Exception e) {
                    transactionHelper.rollback(status);
                    throw new JobExecutionException(e);
                }
                log.debug("Courier found {}", suggestedOrder.getCourier().getId());
                return;
            }
        }

        if (interrupted) {
            var status = transactionHelper.createTransaction("courierNotfound");
            try {
                order.setStatus(OrderStatus.COURIER_NOT_FOUND);
                orderUpdateService.add(order.getId(),
                        OrderStatus.APPROVED_BY_SELLER,
                        OrderStatus.COURIER_NOT_FOUND);
                orderRepository.save(order);
                transactionHelper.commit(status);
            } catch (Exception e) {
                transactionHelper.rollback(status);
                throw new JobExecutionException(e);
            }
        }
    }

    private User findCourier(UserRepository userRepository) {
        List<User> freeCourier = userRepository.findFreeCouriers(Limit.of(1));
        if (freeCourier.isEmpty()) throw new NotFoundException("Free courier not found");
        return freeCourier.get(0);
    }

    @Override
    public void interrupt() {
        interrupted = true;
    }
}
