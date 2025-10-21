package com.cairedine.gestion.contact.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "admin"; // remplace par "admin" ou autre
        String hashedPassword = encoder.encode(rawPassword);

        System.out.println("Mot de passe : " + rawPassword);
        System.out.println("Hash BCrypt : " + hashedPassword);
    }
}
