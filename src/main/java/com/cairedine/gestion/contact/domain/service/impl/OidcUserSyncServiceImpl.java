package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.domain.service.IOidcUserSyncService;
import com.cairedine.gestion.contact.infrastructure.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

        Optional<DBUser> userOpt = userRepository.findBySub(sub);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(email);
        }

        if (userOpt.isPresent()) {
            DBUser existingUser = userOpt.get();
            existingUser.setSub(sub);
            existingUser.setEmail(email);
            existingUser.setUsername(name);
            return userRepository.save(existingUser);
        } else {
            return createNewUser(sub, email, name);
        }
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
