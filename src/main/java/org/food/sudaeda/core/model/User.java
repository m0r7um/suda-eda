package org.food.sudaeda.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.food.sudaeda.core.enums.Role;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    private Long id;
    private Role role;
}