package org.food.sudaeda.emailservice.core.service;

import lombok.RequiredArgsConstructor;
import org.food.sudaeda.emailservice.configuration.properties.EmailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;

    public void sendNotification(String orderId, String oldStatus, String newStatus, String addressee) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(emailProperties.getFrom());
        mailMessage.setTo(addressee);
        mailMessage.setText(String.format("Статус вашего заказа №%s изменился с %s на %s", orderId, oldStatus, newStatus));
        mailMessage.setSubject(emailProperties.getSubject());

        javaMailSender.send(mailMessage);
    }
}
