package org.food.sudaeda.input.rest.controller;

import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.service.OrderService;
import org.food.sudaeda.dto.request.CreateOrderRequest;
import org.food.sudaeda.dto.response.CreateOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createNewOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createNewOrder(request));
    }
}
