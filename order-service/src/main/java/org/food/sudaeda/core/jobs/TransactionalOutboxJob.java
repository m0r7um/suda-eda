package org.food.sudaeda.core.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.food.sudaeda.analytics.model.OrderUpdate;
import org.food.sudaeda.analytics.repository.OrderUpdateRepository;
import org.food.sudaeda.core.model.Order;
import org.food.sudaeda.core.repository.OrderRepository;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;

@Slf4j
@DisallowConcurrentExecution
public class TransactionalOutboxJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        log.info("TransactionalOutboxJob start");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        OrderUpdateRepository orderUpdateRepository = (OrderUpdateRepository) dataMap.get("orderUpdateRepository");
        OrderRepository orderRepository = (OrderRepository) dataMap.get("orderRepository");
        JmsTemplate jmsTemplate = (JmsTemplate) dataMap.get("jmsTemplate");
        String topicName = (String) dataMap.get("topicName");
        ObjectMapper objectMapper = (ObjectMapper) dataMap.get("objectMapper");
        EntityManagerFactory entityManagerFactory = (EntityManagerFactory) dataMap.get("entityManagerFactory");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        List<OrderUpdate> updatedOrders = orderUpdateRepository.findByIsSent(false, PageRequest.of(0, 100));
        while (!updatedOrders.isEmpty()) {
            for (OrderUpdate orderUpdate : updatedOrders) {
                Map<String, String> message = new HashMap<>();
                message.put("order_id", orderUpdate.getOrderId().toString());
                message.compute("new_status", (String value, String x) -> orderUpdate.getToStatus().toString());
                message.put("old_status", String.valueOf(orderUpdate.getFromStatus()));

                Optional<Order> order = orderRepository.findById(orderUpdate.getOrderId());
                if (order.isPresent()) {
                    message.put("addressee", order.get().getBuyer().getEmail());
                } else continue;

                try {
                    entityManager.getTransaction().begin();
                    orderUpdate.setIsSent(true);
                    orderUpdateRepository.save(orderUpdate);
                    jmsTemplate.convertAndSend(topicName, objectMapper.writeValueAsString(message));
                    entityManager.getTransaction().commit();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    entityManager.getTransaction().rollback();
                }
                updatedOrders = orderUpdateRepository.findByIsSent(false, PageRequest.of(0, 100));
            }
        }
        log.info("TransactionalOutboxJob finished");
    }
}
