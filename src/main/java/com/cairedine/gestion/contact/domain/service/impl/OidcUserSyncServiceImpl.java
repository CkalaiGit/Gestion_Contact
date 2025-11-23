package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.domain.service.IOidcUserSyncService;
import com.cairedine.gestion.contact.infrastructure.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OidcUserSyncServiceImpl implements IOidcUserSyncService {

    private final IUserRepository userRepository;

    @Override
    @Transactional
    public DBUser sync(OidcUser oidcUser) {

        String sub = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        return userRepository.findBySub(sub)
                .map(existingUser -> updateExistingUser(existingUser, email, name))
                .orElseGet(() -> createNewUser(sub, email, name));
    }

    private DBUser updateExistingUser(DBUser user, String email, String name) {

        // Mettre à jour l'email si besoin
        if (email != null && !email.equalsIgnoreCase(user.getEmail())) {
            user.setEmail(email);
        }

        // Mettre à jour le username (nom d’affichage)
        if (name != null) {
            user.setUsername(name);
        }

        return userRepository.save(user);
    }

    private DBUser createNewUser(String sub, String email, String name) {

        String role = email.equalsIgnoreCase("cairedine@gmail.com")
                ? "ADMIN"
                : "USER";

        DBUser user = DBUser.builder()
                .sub(sub)
                .email(email)
                .username(name)
                .role(role)
                .build();

        return userRepository.save(user);
    }
}
