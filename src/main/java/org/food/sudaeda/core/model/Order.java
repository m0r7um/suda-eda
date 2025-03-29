package org.food.sudaeda.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.food.sudaeda.core.enums.OrderStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Setter
@Getter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    private LocalDateTime createdAt;

    private LocalDateTime deliveryTime;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
