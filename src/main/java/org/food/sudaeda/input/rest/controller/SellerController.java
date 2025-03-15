package org.food.sudaeda.input.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.service.SellerService;
import org.food.sudaeda.dto.response.GetOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<GetOrderResponse>> getSellerOrders(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.getSellerOrders(id));
    }
}
