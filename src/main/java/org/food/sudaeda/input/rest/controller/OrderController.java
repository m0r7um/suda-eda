package org.food.sudaeda.input.rest.controller;

import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.service.OrderService;
import org.food.sudaeda.dto.request.CreateOrderRequest;
import org.food.sudaeda.dto.request.ProcessNewOrderBySellerRequest;
import org.food.sudaeda.dto.response.CreateOrderResponse;
import org.food.sudaeda.dto.response.GetOrderResponse;
import org.food.sudaeda.dto.response.ProcessNewOrderBySellerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createNewOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createNewOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetOrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ProcessNewOrderBySellerResponse> acceptNewOrder(
            @PathVariable Long id,
            @RequestBody ProcessNewOrderBySellerRequest request
    ) {
        return ResponseEntity.ok(orderService.acceptNewOrder(id, request));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ProcessNewOrderBySellerResponse> rejectNewOrder(
            @PathVariable Long id,
            @RequestBody ProcessNewOrderBySellerRequest request
    ) {
        return ResponseEntity.ok(orderService.rejectNewOrder(id, request));
    }
}
