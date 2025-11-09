package com.cairedine.gestion.contact.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Table(name = "users") // "user" est un mot réservé SQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DBUser implements UserDetails, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    // --- Relation : un utilisateur peut avoir plusieurs contacts ---
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts;

    // --- Méthodes UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Transforme chaque rôle "USER" en "ROLE_USER" pour Spring Security
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
