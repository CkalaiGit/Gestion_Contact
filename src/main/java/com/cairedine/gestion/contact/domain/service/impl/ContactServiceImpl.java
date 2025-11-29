package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.domain.exception.EmailAlreadyExistsException;
import com.cairedine.gestion.contact.domain.service.IContactService;
import com.cairedine.gestion.contact.infrastructure.repository.IContactRepository;
import com.cairedine.gestion.contact.infrastructure.repository.IUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements IContactService {

    private final IContactRepository contactRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public Page<Contact> findPageForUser(String sub, String query, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "lastName").and(Sort.by(Sort.Direction.ASC, "firstName"));
        Pageable pageable = PageRequest.of(page, size, sort);
        if (query == null || query.trim().isEmpty()) {
            return contactRepository.findAllByOwnerUsername(sub, pageable);
        }
        return contactRepository.searchForUser(sub, query, pageable);
    }

    @Override
    @Transactional
    public void createForUser(String sub, Contact contact) {

        if (contactRepository.existsByEmailIgnoreCase(contact.getEmail())) {
            throw new EmailAlreadyExistsException(STR."Email déjà utilisé: \{contact.getEmail()}");
        }

        DBUser owner = userRepository.findBySub(sub)
                .orElseThrow(() -> new IllegalArgumentException(STR."Utilisateur introuvable pour sub: \{sub}"));

        contact.setOwner(owner);
        contactRepository.save(contact);
    }


    @Override
    @Transactional
    public void updateForUser(String sub, Long id, Contact contact) {
        Contact existingContact = contactRepository.findByIdAndOwnerUsername(id, sub)
                .orElseThrow(() -> new IllegalArgumentException(
                        STR."Contact introuvable ou non autorisé: \{id}"));

        String newEmail = contact.getEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(existingContact.getEmail())) {
            if (contactRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new EmailAlreadyExistsException(STR."Email déjà utilisé: \{newEmail}");
            }
        }

        existingContact.setFirstName(contact.getFirstName());
        existingContact.setLastName(contact.getLastName());
        existingContact.setEmail(contact.getEmail());
        existingContact.setPhone(contact.getPhone());

        contactRepository.save(existingContact);
    }

    @Override
    public void deleteById(Long id) {
        contactRepository.deleteById(id);
    }

    @Override
    public Contact findByIdForUser(String username, Long id, boolean isAdmin) {
        if (isAdmin) {
            return contactRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(STR."Contact introuvable: \{id}"));
        }
        return contactRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new IllegalArgumentException(
                        STR."Contact introuvable ou non autorisé: \{id}"));
    }

}
