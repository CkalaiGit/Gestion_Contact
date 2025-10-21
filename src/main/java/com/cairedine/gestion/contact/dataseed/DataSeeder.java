package com.cairedine.gestion.contact.dataseed;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.infrastructure.repository.IContactRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final IContactRepository contactRepository;

    public DataSeeder(IContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public void run(String... args) {
        if (contactRepository.count() == 0) {
            contactRepository.save(new Contact( "Ada", "Lovelace", "ada@acme.com", "0601010101"));
            contactRepository.save(new Contact( "Alan", "Turing", "alan@acme.com", "0602020202"));
            contactRepository.save(new Contact( "Grace", "Hopper", "grace@acme.com", "0603030303"));
            contactRepository.save(new Contact( "Cairedine", "KALAI", "ck@acme.com", "0603530378"));
        }
    }

}
