package org.food.sudaeda.analytics.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.food.sudaeda.analytics.model.OrderUpdate;

@Repository
public interface OrderUpdateRepository extends JpaRepository<OrderUpdate, Long> {
    List<OrderUpdate> findByIsSent(Boolean isSent, Pageable pageable);
}
