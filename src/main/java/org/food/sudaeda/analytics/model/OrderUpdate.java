package org.food.sudaeda.analytics.model;

import jakarta.persistence.*;
import lombok.*;
import org.food.sudaeda.core.enums.OrderStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders_updates")
public class OrderUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_updates_id_seq")
    @SequenceGenerator(name = "orders_updates_id_seq", sequenceName = "orders_updates_id_seq", allocationSize = 1)
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
