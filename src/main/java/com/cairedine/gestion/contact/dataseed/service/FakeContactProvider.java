package com.cairedine.gestion.contact.dataseed.service;

import com.cairedine.gestion.contact.domain.Contact;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class FakeContactProvider implements IContactProvider {
    private static final AtomicLong SEQ = new AtomicLong(1);

    @Override
    public List<Contact> getAll() {
        return List.of(
                mk("Ada", "Lovelace", "ada@acme.com"),
                mk("Alan", "Turing", "alan@acme.com"),
                mk("Grace", "Hopper", "grace@acme.com")
        );
    }

    private Contact mk(String fn, String ln, String email) {
        var c = new Contact();
        c.setId(1L);
        c.setFirstName(fn);
        c.setLastName(ln);
        c.setEmail(email);
        return c;
    }
}
