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
    public Page<Contact> findPageForUser(String username, String query, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "lastName").and(Sort.by(Sort.Direction.ASC, "firstName"));
        Pageable pageable = PageRequest.of(page, size, sort);
        if (query == null || query.trim().isEmpty()) {
            // Tous les contacts de l’utilisateur, sans filtre de recherche
            return contactRepository.findAllByOwnerUsername(username, pageable);
        }
        return contactRepository.searchForUser(username, query, pageable);
    }

    @Override
    @Transactional
    public void createForUser(String username, Contact contact) {
        if (contactRepository.existsByEmailIgnoreCase(contact.getEmail())) {
            throw new EmailAlreadyExistsException("Email déjà utilisé: " + contact.getEmail());
        }

        DBUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + username));

        contact.setOwner(owner);
        contactRepository.save(contact);
    }

    @Override
    @Transactional
    public void updateForUser(String username, Long id, Contact contact) {
        Contact existingContact = contactRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contact introuvable ou non autorisé: " + id));

        String newEmail = contact.getEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(existingContact.getEmail())) {
            if (contactRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new EmailAlreadyExistsException("Email déjà utilisé: " + newEmail);
            }
        }

        existingContact.setFirstName(contact.getFirstName());
        existingContact.setLastName(contact.getLastName());
        existingContact.setEmail(contact.getEmail());
        existingContact.setPhone(contact.getPhone());

        contactRepository.save(existingContact);
    }

    @Override
    public Contact findById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact introuvable: " + id));
    }

    @Override
    public void deleteById(Long id) {
        contactRepository.deleteById(id);
    }

    @Override
    public Contact findByIdForUser(String username, Long id) {
        return null;
    }

    @Override
    public void deleteForUser(String username, Long id) {
    }
}
