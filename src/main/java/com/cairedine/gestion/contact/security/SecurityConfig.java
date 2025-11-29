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
                // 1. Stateless
                // -------------------------------
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // -------------------------------
                // 2. Règles d'accès
                // -------------------------------
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/h2-console").permitAll()
                        .requestMatchers("/login").permitAll()   // Google redirige ici au besoin
                        .requestMatchers("/contacts/**").authenticated()
                        .anyRequest().authenticated()
                )

                // -------------------------------
                // 3. Login Google OIDC
                // -------------------------------
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {

                            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

                            DBUser dbUser = oidcUserSyncService.sync(oidcUser);

                            String jwt = jwtService.generateToken(dbUser);

                            //Déposer le JWT dans un cookie sécurisé
                            Cookie cookie = new Cookie("JWT", jwt);
                            cookie.setHttpOnly(true);
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
