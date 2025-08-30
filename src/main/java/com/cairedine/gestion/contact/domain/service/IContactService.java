package com.cairedine.gestion.contact.domain.service;

import com.cairedine.gestion.contact.domain.entity.Contact;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IContactService {

    List<Contact> findAllSorted();
    Page<Contact> findPage(String query, int page, int size);

}
