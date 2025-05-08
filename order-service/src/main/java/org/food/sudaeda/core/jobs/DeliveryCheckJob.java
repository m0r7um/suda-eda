package org.food.sudaeda.core.jobs;

import org.food.sudaeda.analytics.service.OrderUpdateService;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.model.Order;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.exception.NotFoundException;
import org.food.sudaeda.utils.TransactionHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DeliveryCheckJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        OrderRepository orderRepository = (OrderRepository) dataMap.get("orderRepository");
        OrderUpdateService orderUpdateService = (OrderUpdateService) dataMap.get("orderUpdateService");
        TransactionHelper transactionHelper = (TransactionHelper) dataMap.get("transactionHelper");
        Long orderId = dataMap.getLong("orderId");

        var status = transactionHelper.createTransaction("deliveryCheck");
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order not found"));

            if (order.getStatus() == OrderStatus.ORDER_READY) {
                order.setStatus(OrderStatus.ORDER_NOT_PICKED_UP_BY_COURIER);
                orderUpdateService.add(
                        order.getId(),
                        OrderStatus.ORDER_READY,
                        OrderStatus.ORDER_NOT_PICKED_UP_BY_COURIER
                );
                orderRepository.save(order);
            }
            transactionHelper.commit(status);
        } catch (Exception e) {
            transactionHelper.rollback(status);
            throw new JobExecutionException(e);
        }
    }
}
