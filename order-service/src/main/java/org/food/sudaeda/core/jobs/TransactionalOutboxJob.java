package org.food.sudaeda.core.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.food.sudaeda.analytics.model.OrderUpdate;
import org.food.sudaeda.analytics.repository.OrderUpdateRepository;
import org.food.sudaeda.core.model.Order;
import org.food.sudaeda.core.repository.OrderRepository;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;

@Slf4j
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

        List<OrderUpdate> updatedOrders = orderUpdateRepository.findByIsSent(false, PageRequest.of(0,100));
        while (!updatedOrders.isEmpty()) {
            for (OrderUpdate orderUpdate : updatedOrders) {
                Map<String, String> message = new HashMap<>();
                message.put("order_id", orderUpdate.getOrderId().toString());
                message.put("new_status", orderUpdate.getToStatus().toString());
                message.put("old_status", orderUpdate.getFromStatus().toString());

                Optional<Order> order = orderRepository.findById(orderUpdate.getOrderId());
                if (order.isPresent()) {
                    message.put("addressee", order.get().getBuyer().getEmail());
                } else continue;

                try {
                    jmsTemplate.convertAndSend(topicName, objectMapper.writeValueAsString(message));
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage(), e);
                }
                updatedOrders = orderUpdateRepository.findByIsSent(false, PageRequest.of(0, 100));
                log.info("TransactionalOutboxJob start");

            }
        }
    }
}
