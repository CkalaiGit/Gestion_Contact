package com.cairedine.gestion.contact.dataseed.service;

import com.cairedine.gestion.contact.domain.Contact;
import java.util.List;


public interface IContactProvider {
    List<Contact> getAll();
}
