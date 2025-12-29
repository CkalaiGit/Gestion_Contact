package com.cairedine.gestion.contact.security;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceUTest {

    private JwtService jwtService;
    private SecretKey testSecretKey;

    @BeforeEach
    void setUp() {
        String rawSecret = "ma_super_cle_secrete_de_test_32_caracteres_minimum";
        jwtService = new JwtService(rawSecret, 3600L);
        testSecretKey = Keys.hmacShaKeyFor(rawSecret.getBytes());
    }

    @Test
    void shouldGenerateValidToken_WhenUserIsProvided() {
        // GIVEN
        // Création d'un utilisateur de test avec des données fictives
        DBUser user = new DBUser();
        user.setSub("user-123");
        user.setEmail("test@exemple.com");
        user.setRole("ADMIN");

        // WHEN
        String token = jwtService.generateToken(user);

        // THEN
        assertNotNull(token);
        Claims claims = Jwts.parser()
                .verifyWith(testSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("user-123", claims.getSubject());
        assertEquals("test@exemple.com", claims.get("email"));
        assertEquals("ADMIN", claims.get("role"));

        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void shouldReturnFalse_WhenTokenIsExpired() {
        // GIVEN : On crée un token qui a expiré il y a 1 heure
        Date pastDate = new Date(System.currentTimeMillis() - 3600000);
        String expiredToken = Jwts.builder()
                .subject("user-123")
                .expiration(pastDate)
                .signWith(testSecretKey)
                .compact();

        // WHEN
        boolean result = jwtService.isValid(expiredToken);

        // THEN
        assertFalse(result, "Le token devrait être invalide car il est expiré");
    }

    @Test
    void shouldExtractSub_WhenTokenIsValid() {
        // GIVEN
        DBUser user = new DBUser();
        user.setSub("mon-id-unique");
        String token = jwtService.generateToken(user);

        // WHEN
        String extractedSub = jwtService.extractSub(token);

        // THEN
        assertEquals("mon-id-unique", extractedSub);
    }
}