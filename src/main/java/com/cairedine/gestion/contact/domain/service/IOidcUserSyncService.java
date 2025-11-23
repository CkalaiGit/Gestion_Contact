package com.cairedine.gestion.contact.domain.service;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface IOidcUserSyncService {

    /**
     * Synchronise un utilisateur local (DBUser) avec les données OIDC Google.
     *
     * @param oidcUser utilisateur Google connecté
     * @return DBUser synchronisé (créé ou mis à jour)
     */
    DBUser sync(OidcUser oidcUser);
}
