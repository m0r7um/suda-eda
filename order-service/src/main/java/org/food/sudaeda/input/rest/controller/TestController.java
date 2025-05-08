package org.food.sudaeda.input.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final JmsTemplate jmsTemplate;

    @GetMapping("/send")
    @Transactional
    public String sendMessage(@RequestParam String message) {
        jmsTemplate.convertAndSend("order-status-update-queue", message);
        return "Message sent!";
    }
}
