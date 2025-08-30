package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.service.IContactService;
import com.cairedine.gestion.contact.infrastructure.repository.IContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Page<Contact> findPage(String query, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "lastName").and(Sort.by(Sort.Direction.ASC, "firstName"));
        Pageable pageable = PageRequest.of(page, size, sort);
        if (query == null || query.isBlank()) {
            return contactRepository.findAll(pageable);
        }
        return contactRepository.search(query.trim(), pageable);
    }
}
