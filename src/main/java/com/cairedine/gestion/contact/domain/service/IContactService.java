package com.cairedine.gestion.contact.domain.service;

import com.cairedine.gestion.contact.domain.entity.Contact;
import org.springframework.data.domain.Page;

public interface IContactService {

    void deleteById(Long id);

    Page<Contact> findPageForUser(String username, String query, int page, int size);
    Contact findByIdForUser(String username, Long id, boolean checkOwnership);

    // écriture (associe le propriétaire, vérifie ownership côté service)
    void createForUser(String username, Contact contact);
    void updateForUser(String username, Long id, Contact contact);
}
