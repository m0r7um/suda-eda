package org.food.sudaeda.input.rest.controller;

import lombok.RequiredArgsConstructor;
import org.food.sudaeda.core.service.UserAttributeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserAttributeController {

    private final UserAttributeService userAttributeService;

    @GetMapping("/{userId}/attributes/{attribute}")
    public ResponseEntity<String> getAttribute(@PathVariable String userId, @PathVariable String attribute) {
        return ResponseEntity.ok(
                String.join(",", userAttributeService.getAttribute(userId, attribute))
        );
    }

    @PostMapping("/{userId}/attributes/{attribute}")
    public void updateAttribute(@PathVariable String userId,
                                @PathVariable String attribute,
                                @RequestBody String value) {
        userAttributeService.updateAttribute(userId, attribute, value);
    }
}
