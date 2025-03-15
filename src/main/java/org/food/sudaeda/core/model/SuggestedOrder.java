package org.food.sudaeda.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.food.sudaeda.core.enums.SuggestedOrderStatus;

@Entity
@Table(name = "suggested_orders")
@Getter
@Setter
public class SuggestedOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private User courier;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private SuggestedOrderStatus status;
}
