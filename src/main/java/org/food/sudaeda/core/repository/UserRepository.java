package org.food.sudaeda.core.repository;

import java.util.List;
import java.util.Optional;
import org.food.sudaeda.core.enums.Role;
import org.food.sudaeda.core.model.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT c FROM User c WHERE c.role = 'COURIER' AND NOT EXISTS(SELECT so FROM SuggestedOrder so WHERE so.courier.id <> c.id AND so.status != 'REJECTED')")
    List<User> findFreeCouriers(Limit limit);
    Optional<User> findByIdAndRole(Long id, Role role);
}
