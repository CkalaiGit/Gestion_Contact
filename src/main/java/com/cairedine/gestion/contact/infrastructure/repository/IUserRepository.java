package com.cairedine.gestion.contact.infrastructure.repository;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<DBUser, Long> {

    /**
     * Recherche l'utilisateur via son identifiant stable OIDC (sub).
     */
    Optional<DBUser> findBySub(String sub);

    /**
     * Recherche par email (utile pour mettre à jour l'email si besoin).
     */
    Optional<DBUser> findByEmail(String email);
}
