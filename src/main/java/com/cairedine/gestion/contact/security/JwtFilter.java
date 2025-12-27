package com.cairedine.gestion.contact.security;

import com.cairedine.gestion.contact.infrastructure.repository.IUserRepository;
import lombok.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final IUserRepository iUserRepository; // Vérifiez votre package

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) On cherche le cookie JWT
        String token = extractJwtFromCookies(request);

        if (token != null && jwtService.isValid(token)) {

            String sub = jwtService.extractSub(token);
            String role = jwtService.extractRole(token);

            // On récupère l'utilisateur en base via le sub
            // S'il n'existe pas, on laisse l'auth vide pour que Security bloque l'accès
            iUserRepository.findBySub(sub).ifPresent(dbUser -> {

                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // On passe dbUser (l'objet) au lieu de sub (la String)
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(dbUser, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("JWT".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
