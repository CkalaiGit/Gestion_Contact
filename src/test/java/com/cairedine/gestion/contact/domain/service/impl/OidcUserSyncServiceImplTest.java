package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.infrastructure.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OidcUserSyncServiceImplTest {

    @Mock
    private OidcUser oidcUser;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private OidcUserSyncServiceImpl oidcUserSyncService;

    @Test
    void sync_shouldUpdateExistingUser_whenUserFoundBySub() {

        //given
        String sub = "auth0|123";
        String email = "jean@test.com";
        String name = "Jean Dupont";

        given(oidcUser.getSubject()).willReturn(sub);
        given(oidcUser.getEmail()).willReturn(email);
        given(oidcUser.getFullName()).willReturn(name);

        DBUser existingUser = DBUser.builder().sub(sub).email("ancien@email.com").build();
        given(userRepository.findBySub(sub)).willReturn(Optional.of(existingUser));
        given(userRepository.save(any(DBUser.class))).willAnswer(invocation -> invocation.getArgument(0));
        /*
         * Avec willAnswer, on crée un comportement dynamique :
         * L'interception : Mockito "attrape" l'appel à .save().
         * L'invocation : L'objet invocation contient tout ce qui a été envoyé à la méthode (les arguments).

         * Le miroir : getArgument(0) dit à Mockito : "Prends le premier paramètre qu'on vient de te donner
         * (l'utilisateur modifié) et renvoie-le immédiatement."

         * Cela permet au result dans le test de contenir
         * toutes les modifications (setSub, setEmail, etc.) effectuées par le service.
         **/
        //when
        DBUser result = oidcUserSyncService.sync(oidcUser);
        //then
        assertEquals(email, result.getEmail());
        assertEquals(name, result.getUsername());
        verify(userRepository, times(1)).save(any(DBUser.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void sync_shouldCreateNewUser_whenUserNotFoundAndEmailIsStandard() {

        String standardEmail = "jean.dupont@test.com"; // Un email lambda
        String sub = "auth0|456";
        String name = "Jean Dupont";

        //given
        given(userRepository.findBySub(sub)).willReturn(Optional.empty());
        given(userRepository.findByEmail(standardEmail)).willReturn(Optional.empty());
        given(oidcUser.getSubject()).willReturn(sub);
        given(oidcUser.getEmail()).willReturn(standardEmail);
        given(oidcUser.getFullName()).willReturn(name);
        given(userRepository.save(any(DBUser.class))).willAnswer(invocation -> invocation.getArgument(0));
        //when
        DBUser result = oidcUserSyncService.sync(oidcUser);
        //then
        assertEquals(standardEmail, result.getEmail());
        assertEquals(name, result.getUsername());
        assertEquals("USER", result.getRole());
        verify(userRepository, times(1)).save(any(DBUser.class));
    }

}