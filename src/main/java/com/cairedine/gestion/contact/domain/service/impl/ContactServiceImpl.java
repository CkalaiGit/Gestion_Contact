package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.domain.exception.EmailAlreadyExistsException;
import com.cairedine.gestion.contact.domain.service.IContactService;
import com.cairedine.gestion.contact.infrastructure.repository.IContactRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements IContactService {

    private final IContactRepository contactRepository;

    @Override
    @Transactional
    public Page<@NonNull Contact> findPageForUser(String sub, String query, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "lastName", "firstName");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<@NonNull Contact> result = (query == null || query.trim().isEmpty())
                ? contactRepository.findAllByOwnerSub(sub, pageable)
                : contactRepository.searchForUser(sub, query, pageable);

        return result != null ? result : Page.empty();
    }

    @Override
    @Transactional
    public void createForUser(DBUser owner, Contact contact) {

        if (contactRepository.existsByEmailIgnoreCase(contact.getEmail())) {
            throw new EmailAlreadyExistsException(
                    String.format("Email déjà utilisé: %s", contact.getEmail())
            );
        }

        contact.setOwner(owner);
        contactRepository.save(contact);
    }


    @Override
    @Transactional
    public void updateForUser(String sub, Long id, Contact contact) {
        Contact existingContact = contactRepository.findByIdAndOwnerSub(id, sub)
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
    public Contact findByIdForUser(String sub, Long id, boolean isAdmin) {
        if (isAdmin) {
            return contactRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Contact introuvable: %d", id)
                    ));
        }
        return contactRepository.findByIdAndOwnerSub(id, sub)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Contact introuvable ou non autorisé: %d", id)
                ));
    }

}