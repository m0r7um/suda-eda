package org.food.sudaeda.input.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.service.SuggestedOrderService;
import org.food.sudaeda.dto.response.SuggestedOrderResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-suggestions")
@RequiredArgsConstructor
public class OrderSuggestionController {
    private final SuggestedOrderService suggestedOrderService;

    @GetMapping("/pending")
    public List<SuggestedOrderResponse> suggestedOrders(@RequestParam("courierId") Long courierId) {
        return suggestedOrderService.findPendingByCourier(courierId);
    }

    @PostMapping("/{id}/accept")
    public SuggestedOrderResponse acceptSuggestedOrder(@PathVariable Long id) {
        return suggestedOrderService.acceptSuggestion(id);
    }
}
