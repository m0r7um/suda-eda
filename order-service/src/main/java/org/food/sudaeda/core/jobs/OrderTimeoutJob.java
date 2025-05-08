package org.food.sudaeda.core.jobs;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.food.sudaeda.analytics.service.OrderUpdateService;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.model.Order;
import org.food.sudaeda.core.repository.OrderRepository;
import org.food.sudaeda.exception.NotFoundException;
import org.food.sudaeda.utils.TransactionHelper;

public class OrderTimeoutJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        OrderRepository orderRepository = (OrderRepository) dataMap.get("orderRepository");
        OrderUpdateService orderUpdateService = (OrderUpdateService) dataMap.get("orderUpdateService");
        TransactionHelper transactionHelper = (TransactionHelper) dataMap.get("transactionHelper");
        Long orderId = dataMap.getLong("orderId");

        var updateStatus = transactionHelper.createTransaction("updateStatusSellerNotAnswered");
        try {
            Order foundOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order not found"));

            if (foundOrder.getStatus() == OrderStatus.NEW_ORDER) {
                foundOrder.setStatus(OrderStatus.SELLER_NOT_ANSWERED);
                orderUpdateService.add(foundOrder.getId(),
                        OrderStatus.NEW_ORDER,
                        OrderStatus.SELLER_NOT_ANSWERED);
                orderRepository.save(foundOrder);
            }
            transactionHelper.commit(updateStatus);
        } catch (Exception e) {
            transactionHelper.rollback(updateStatus);
            throw new JobExecutionException(e);
        }
    }
}
