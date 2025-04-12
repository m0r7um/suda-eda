package org.food.sudaeda.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.food.sudaeda.core.enums.OrderStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders_updates")
@Setter
@Getter
public class OrderUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_id_seq")
    @SequenceGenerator(name = "orders_id_seq", sequenceName = "orders_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus fromStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus toStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
