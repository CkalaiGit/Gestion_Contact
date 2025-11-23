package com.cairedine.gestion.contact.security;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-in-seconds}") long expirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expirationSeconds * 1000;
    }

    // --------------------------
    // 1) Création du JWT
    // --------------------------
    public String generateToken(DBUser user) {

        return Jwts.builder()
                .setSubject(user.getSub())  // sub = identifiant stable
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // --------------------------
    // 2) Extraction du sub
    // --------------------------
    public String extractSub(String token) {
        return getClaims(token).getSubject();
    }

    // --------------------------
    // 3) Extraction du rôle
    // --------------------------
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // --------------------------
    // 4) Extraction de l'email
    // --------------------------
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // --------------------------
    // 5) Validation JWT
    // --------------------------
    public boolean isValid(String token) {
        try {
            getClaims(token); // Par sécurité : lève une exception si invalide
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // --------------------------
    // Méthode interne : lire les claims
    // --------------------------
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
