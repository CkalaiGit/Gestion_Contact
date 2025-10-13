package com.cairedine.gestion.contact.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;
    @Email
    @NotBlank(message = "L’email est obligatoire")
    private String email;
    @Pattern(
            regexp = "^(?:0[1-9]|\\+33[1-9])([ .-]?\\d{2}){4}$",
            message = "Numéro de téléphone invalide (ex: 06 12 34 56 78 ou +33612345678)"
    )
    @NotBlank(message = "Le Numéro de téléphone est obligatoire")
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // clé étrangère dans la table contact
    private DBUser owner;

    public Contact(Long id, String ada, String lovelace, String mail, String number) {
        this.id = id;
        this.firstName = ada;
        this.lastName = lovelace;
        this.email = mail;
        this.phone = number;
    }

    public Contact(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }
}
