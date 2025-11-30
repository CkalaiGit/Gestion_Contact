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
            throw new EmailAlreadyExistsException(
                    String.format("Email déjà utilisé: %s", contact.getEmail())
            );
        }

        DBUser owner = userRepository.findBySub(sub)
                .orElseThrow(() -> new IllegalArgumentException(
                        // Remplacement STR par String.format
                        String.format("Utilisateur introuvable pour sub: %s", sub)
                ));

        contact.setOwner(owner);
        contactRepository.save(contact);
    }


    @Override
    @Transactional
    public void updateForUser(String sub, Long id, Contact contact) {
        Contact existingContact = contactRepository.findByIdAndOwnerUsername(id, sub)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Contact introuvable ou non autorisé: %d", id)
                ));

        String newEmail = contact.getEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(existingContact.getEmail()) && contactRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new EmailAlreadyExistsException(
                    String.format("Email déjà utilisé: %s", newEmail)
            );
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
                    .orElseThrow(() -> new IllegalArgumentException(
                            // Remplacement STR par String.format (%d pour Long)
                            String.format("Contact introuvable: %d", id)
                    ));
        }
        return contactRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new IllegalArgumentException(
                        // Remplacement STR par String.format (%d pour Long)
                        String.format("Contact introuvable ou non autorisé: %d", id)
                ));
    }

}