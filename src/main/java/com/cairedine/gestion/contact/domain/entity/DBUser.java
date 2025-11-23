package com.cairedine.gestion.contact.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users") // "user" est un mot réservé SQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DBUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Identifiant stable fourni par le provider OIDC (Google) : claim "sub".
     * C'est notre clé technique pour lier un compte OIDC à un DBUser.
     */
    @Column(nullable = false, unique = true)
    private String sub;

    /**
     * Email actuel de l'utilisateur (peut changer dans le temps).
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Nom d'affichage / username applicatif (optionnel).
     */
    @Column
    private String username;

    /**
     * Rôle principal de l'utilisateur dans l'application (ex: "USER", "ADMIN").
     */
    @Column(nullable = false)
    private String role;

    // --- Relation : un utilisateur peut avoir plusieurs contacts ---
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();
}
