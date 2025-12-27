package com.cairedine.gestion.contact.security;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.domain.service.IOidcUserSyncService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtService jwtService;
    private final IOidcUserSyncService oidcUserSyncService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // -------------------------------
                // 1. Configuration de base & Sécurité
                // -------------------------------
                .csrf(AbstractHttpConfigurer::disable)
                // Désactiver la protection par frames pour permettre l'affichage de la console H2
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // -------------------------------
                // 2. Règles d'accès
                // -------------------------------
                .authorizeHttpRequests(auth -> auth
                        // Ressources statiques et publiques
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        // Console H2 : Il faut impérativement "/**"
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        // Routes protégées
                        .requestMatchers("/contacts/**").authenticated()
                        .anyRequest().authenticated()
                )

                // -------------------------------
                // 3. Login Google OIDC
                // -------------------------------
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {

                            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

                            // Synchronisation avec la base de données
                            DBUser dbUser = oidcUserSyncService.sync(oidcUser);

                            // Génération du JWT incluant le rôle et le subject
                            String jwt = jwtService.generateToken(dbUser);

                            // Déposer le JWT dans un cookie sécurisé (HttpOnly)
                            Cookie cookie = new Cookie("JWT", jwt);
                            cookie.setHttpOnly(true);
                            cookie.setSecure(false); // Mettre à true en production (HTTPS)
                            cookie.setPath("/");
                            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 jours
                            response.addCookie(cookie);

                            response.sendRedirect("/contacts");
                        })
                )

                // -------------------------------
                // 4. Ajouter le filtre JWT
                // -------------------------------
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
