package org.food.sudaeda.input.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.service.SellerService;
import org.food.sudaeda.dto.response.GetOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping("/orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<GetOrderResponse>> getSellerOrders() {
        return ResponseEntity.ok(sellerService.getSellerOrders());
    }

    @GetMapping("/orders/ready")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<GetOrderResponse>> getSellerReadyOrders() {
        return ResponseEntity.ok(sellerService.getSellerOrdersByStatus(OrderStatus.ORDER_READY));
    }

    @GetMapping("/orders/available")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<GetOrderResponse>> getSellerAvailableOrders() {
        return ResponseEntity.ok(sellerService.getSellerOrdersByStatus(OrderStatus.APPROVED_BY_COURIER));
    }
}
