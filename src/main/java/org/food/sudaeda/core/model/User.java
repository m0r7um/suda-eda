package org.food.sudaeda.core.model;

import jakarta.persistence.*;
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
    @Enumerated(EnumType.STRING)
    private Role role;
}