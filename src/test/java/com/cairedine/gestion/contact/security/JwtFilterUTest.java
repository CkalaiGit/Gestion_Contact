package com.cairedine.gestion.contact.security;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.infrastructure.repository.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtFilterUTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtService jwtService;
    @Mock
    private IUserRepository iUserRepository;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChain_WhenNoCookiesArePresent() throws ServletException, IOException {
        // GIVEN
        given(request.getCookies()).willReturn(null);

        // WHEN
        jwtFilter.doFilter(request, response, filterChain);

        // THEN
        verify(filterChain).doFilter(request, response);
        verify(iUserRepository, never()).findBySub(anyString());
    }

    @Test
    void shouldAuthenticateUser_WhenTokenIsValid() throws ServletException, IOException {
        // GIVEN
        String token = "valid.jwt.token";
        Cookie jwtCookie = new Cookie("JWT", token);
        DBUser dbUser = new DBUser();

        given(request.getCookies()).willReturn(new Cookie[]{jwtCookie});
        given(jwtService.isValid(token)).willReturn(true);
        given(jwtService.extractSub(token)).willReturn("user123");
        given(jwtService.extractRole(token)).willReturn("USER");

        given(iUserRepository.findBySub("user123")).willReturn(Optional.of(dbUser));

        // WHEN
        jwtFilter.doFilterInternal(request, response, filterChain);

        // THEN
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "L'authentification ne devrait pas être nulle");
        assertEquals(dbUser, auth.getPrincipal(), "L'utilisateur principal devrait être notre dbUser");
        boolean hasRoleUser = auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_USER"));
        assertTrue(hasRoleUser, "L'utilisateur devrait avoir le rôle ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }


}