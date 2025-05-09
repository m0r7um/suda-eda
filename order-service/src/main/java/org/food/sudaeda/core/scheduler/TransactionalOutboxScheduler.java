package org.food.sudaeda.core.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.food.sudaeda.analytics.repository.OrderUpdateRepository;
import org.food.sudaeda.configuration.properties.ArtemisProperties;
import org.food.sudaeda.core.jobs.TransactionalOutboxJob;
import org.food.sudaeda.core.repository.OrderRepository;
import org.quartz.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionalOutboxScheduler {
    private final Scheduler scheduler;
    private final OrderUpdateRepository orderUpdateRepository;
    private final JmsTemplate jmsTemplate;
    private final ArtemisProperties artemisProperties;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final EntityManagerFactory analyticsEntityManagerFactory;

    @PostConstruct
    public void init() throws SchedulerException {
        JobDetail job = JobBuilder.newJob(TransactionalOutboxJob.class)
                .withIdentity("transactional-outbox")
                .usingJobData(createJobDataMap())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(10).repeatForever())
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    private JobDataMap createJobDataMap() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("orderUpdateRepository", orderUpdateRepository);
        dataMap.put("orderRepository", orderRepository);
        dataMap.put("jmsTemplate", jmsTemplate);
        dataMap.put("topicName", artemisProperties.getOrderStatusUpdateQueue());
        dataMap.put("objectMapper", objectMapper);
        dataMap.put("entityManagerFactory", analyticsEntityManagerFactory);
        return dataMap;
    }
}
