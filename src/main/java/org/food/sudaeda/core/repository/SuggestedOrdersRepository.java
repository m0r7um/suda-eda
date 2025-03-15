package org.food.sudaeda.core.repository;

import java.util.Optional;
import org.food.sudaeda.core.enums.SuggestedOrderStatus;
import org.food.sudaeda.core.model.Order;
import org.food.sudaeda.core.model.SuggestedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestedOrdersRepository extends JpaRepository<SuggestedOrder, Long> {
    Optional<SuggestedOrder> findByOrderAndStatusNot(Order order, SuggestedOrderStatus status);
    Optional<SuggestedOrder> findByOrderAndStatus(Order order, SuggestedOrderStatus status);
}
