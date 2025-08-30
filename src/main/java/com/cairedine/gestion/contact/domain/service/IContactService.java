package com.cairedine.gestion.contact.domain.service;

import com.cairedine.gestion.contact.domain.entity.Contact;
import java.util.List;

public interface IContactService {

    List<Contact> findAllSorted();

}
