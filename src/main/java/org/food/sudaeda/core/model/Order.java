package org.food.sudaeda.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.food.sudaeda.core.enums.OrderStatus;
import org.springframework.stereotype.Service;

@Entity
@Table(name = "orders")
@Service
@Setter
@Getter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "seller_id")
    private User seller;

    private OrderStatus status;
}
