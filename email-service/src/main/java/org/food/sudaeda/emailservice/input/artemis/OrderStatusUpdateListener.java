package org.food.sudaeda.emailservice.input.artemis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.sudaeda.emailservice.core.service.EmailService;
import org.food.sudaeda.emailservice.dto.StatusUpdateNotification;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusUpdateListener {
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${artemis.order-status-update-queue}")
    public void receiveMessage(String message) {
        try {
            StatusUpdateNotification notification = objectMapper.readValue(message, StatusUpdateNotification.class);
            emailService.sendNotification(
                    notification.getOrderId(),
                    notification.getOldStatus(),
                    notification.getNewStatus(),
                    notification.getAddressee());
        } catch (JsonProcessingException e) {
            log.error("Неверный формат сообщения: {}", message);
        }
    }
}
