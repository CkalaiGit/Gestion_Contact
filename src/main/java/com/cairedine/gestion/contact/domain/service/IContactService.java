package com.cairedine.gestion.contact.domain.service;

import com.cairedine.gestion.contact.domain.entity.Contact;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IContactService {

    List<Contact> findAllSorted();
    Page<Contact> findPage(String query, int page, int size);
    void create(Contact contact);
    void update(Long id, Contact contact);
    Contact findById(Long id);
    void deleteById(Long id);

    Page<Contact> findPageForUser(String username, String query, int page, int size);
    Contact findByIdForUser(String username, Long id);

    // écriture (associe le propriétaire, vérifie ownership côté service)
    void createForUser(String username, Contact contact);
    void updateForUser(String username, Long id, Contact contact);

    // suppression (au choix : ownership ou admin-only)
    void deleteForUser(String username, Long id);
}
