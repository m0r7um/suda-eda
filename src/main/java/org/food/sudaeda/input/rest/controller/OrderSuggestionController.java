package org.food.sudaeda.input.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.food.sudaeda.core.service.SuggestedOrderService;
import org.food.sudaeda.dto.response.SuggestedOrderResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-suggestions")
@RequiredArgsConstructor
@Slf4j
public class OrderSuggestionController {
    private final SuggestedOrderService suggestedOrderService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('COURIER')")
    public List<SuggestedOrderResponse> suggestedOrders(@RequestParam("courierId") Long courierId) {
        return suggestedOrderService.findPendingByCourier(courierId);
    }

    @PostMapping("/{id}/accept")
    public SuggestedOrderResponse acceptSuggestedOrder(@PathVariable Long id) {
        return suggestedOrderService.acceptSuggestion(id);
    }
}
