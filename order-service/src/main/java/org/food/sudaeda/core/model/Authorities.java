package org.food.sudaeda.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.food.sudaeda.core.enums.Role;

@Entity
@Table(name = "authorities")
@Setter
@Getter
public class Authorities {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mySeqGen")
    @SequenceGenerator(name = "mySeqGen", sequenceName = "authorities_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "authority")
    @Enumerated(EnumType.STRING)
    private Role authority;
}
