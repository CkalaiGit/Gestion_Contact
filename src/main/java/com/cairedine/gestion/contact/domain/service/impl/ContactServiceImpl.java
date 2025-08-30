package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.service.IContactService;
import com.cairedine.gestion.contact.infrastructure.repository.IContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements IContactService {

    private final IContactRepository contactRepository;

    @Override
    public List<Contact> findAllSorted() {
        return contactRepository.findAll(Sort.by(ASC, "lastName")
                .and(Sort.by(ASC, "firstName")));
    }
}
