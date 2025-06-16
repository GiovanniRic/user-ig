package it.ig.user.domain.model.entity;

import jakarta.persistence.*;

import lombok.*;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
@Getter
@Setter
@EqualsAndHashCode
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "email", updatable = false, unique = true)
    private String email;

    @Column(name = "username")
    private String username;

    @Column(name = "tax_code")
    private String taxCode;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    private Set<RoleEntity> roles;
}
