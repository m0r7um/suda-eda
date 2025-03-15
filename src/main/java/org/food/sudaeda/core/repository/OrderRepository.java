package org.food.sudaeda.core.repository;

import java.util.List;

import org.food.sudaeda.core.enums.OrderStatus;
import org.food.sudaeda.core.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o where o.seller.id = :sellerId")
    List<Order> findBySellerId(@Param("sellerId") Long userId);

    @Query("SELECT o FROM Order o where o.seller.id = :sellerId and o.status = :orderStatus")
    List<Order> findByStatusAndSellerId(@Param("sellerId") Long userId, @Param("orderStatus") OrderStatus status);
}
