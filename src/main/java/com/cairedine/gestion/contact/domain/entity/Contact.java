package com.cairedine.gestion.contact.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Email
    private String email;
    private String phone;

    public Contact(String ada, String lovelace, String mail, String number) {
        this.firstName = ada;
        this.lastName = lovelace;
        this.email = mail;
        this.phone = number;
    }
}
