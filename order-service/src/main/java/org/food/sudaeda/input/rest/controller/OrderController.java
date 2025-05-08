package org.food.sudaeda.input.rest.controller;

import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.service.OrderService;
import org.food.sudaeda.dto.request.*;
import org.food.sudaeda.dto.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CreateOrderResponse> createNewOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createNewOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetOrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProcessNewOrderBySellerResponse> acceptNewOrder(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.processNewOrder(id,true));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProcessNewOrderBySellerResponse> rejectNewOrder(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.processNewOrder(id, false));
    }

    @PostMapping("/{id}/mark-as-started")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<MarkAsStartedResponse> markAsStarted(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.markAsStarted(id));
    }

    @PostMapping("/{id}/mark-as-ready")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<MarkAsReadyResponse> markAsReady(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.markAsReady(id));
    }

    @PostMapping("/{id}/mark-picked-up")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<MarkAsPickedUpResponse> markAsPickedUp(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.markAsPickedUp(id));
    }
}
