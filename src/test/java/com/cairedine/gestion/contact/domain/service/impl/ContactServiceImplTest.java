package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.domain.exception.EmailAlreadyExistsException;
import com.cairedine.gestion.contact.infrastructure.repository.IContactRepository;
import com.cairedine.gestion.contact.infrastructure.repository.IUserRepository;
import lombok.NonNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    IContactRepository iContactRepository;
    @Mock
    IUserRepository iUserRepository;
    @InjectMocks
    ContactServiceImpl contactService;


    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "\t", "\n", "   "})
    void findPage_noQuery_usesFindAll(String query) {
        DBUser mockUser = DBUser.builder().sub("alice-sub-123").username("alice").build();

        var expectedContact = Contact.builder().firstName("John").lastName("Doe").build();
        Page<@NonNull Contact> expectedPage = new PageImpl<>(List.of(expectedContact));

        // On mock le repository en utilisant le sub de l'objet user
        when(iContactRepository.findAllByOwnerSub(eq(mockUser.getSub()), any(Pageable.class))).thenReturn(expectedPage);

        // 2. Act : On passe l'objet mockUser au lieu de la String "alice"
        Page<@NonNull Contact> result = contactService.findPageForUser(mockUser.getSub(), query, 0, 10);

        // 3. Assert
        assertEquals(expectedPage, result);

        // Vérifie que search N'EST PAS appelé
        Mockito.verify(iContactRepository, never()).searchForUser(anyString(), anyString(), any(Pageable.class));

        // Capture et vérifie le Pageable
        ArgumentCaptor<Pageable> pageableCap = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(iContactRepository).findAllByOwnerSub(eq(mockUser.getSub()), pageableCap.capture());

        Pageable used = pageableCap.getValue();
        assertEquals(0, used.getPageNumber());
        assertEquals(10, used.getPageSize());
    }

    @Test
    void testFindPageWithQuery() {
        DBUser mockUser = DBUser.builder().sub("alice-sub-123").username("alice").build();

        String query = "Doe";
        Pageable pageable = PageRequest.of(1, 5, Sort.by("lastName").and(Sort.by("firstName")));
        var contact = Contact.builder().firstName("John").lastName("Doe").build();
        Page<@NonNull Contact> expectedPage = new PageImpl<>(List.of(contact));

        when(iContactRepository.searchForUser(mockUser.getSub(), query.trim(), pageable)).thenReturn(expectedPage);

        Page<@NonNull Contact> result = contactService.findPageForUser(mockUser.getSub(), query, 1, 5);

        assertEquals(expectedPage, result);
        verify(iContactRepository).searchForUser(mockUser.getSub(), query.trim(), pageable);
    }

    @Test
    void create_should_throw_conflict_when_email_exists() {
        var email = "cairedine.kalai@afd_tech.com";
        var contact = Contact.builder().email(email).build();

        var mockOwner = DBUser.builder().sub("goku").username("Son Goku").build();

        // Simulation : l'email existe déjà en base
        given(iContactRepository.existsByEmailIgnoreCase(email)).willReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> contactService.createForUser(mockOwner, contact));

        then(iContactRepository).should().existsByEmailIgnoreCase(email);
        then(iContactRepository).should(never()).save(any(Contact.class));
        then(iUserRepository).shouldHaveNoInteractions();
    }

    @Test
    void create_should_save_when_email_is_free() {
        // Given
        var contact = Contact.builder().email("cairedine.kalai@afd_tech.com").build();

        var mockOwner = DBUser.builder().sub("goku").username("Son Goku").build();

        given(iContactRepository.existsByEmailIgnoreCase("cairedine.kalai@afd_tech.com")).willReturn(false);

        // When
        contactService.createForUser(mockOwner, contact);

        // Then
        InOrder inOrder = inOrder(iContactRepository);
        inOrder.verify(iContactRepository).existsByEmailIgnoreCase("cairedine.kalai@afd_tech.com");
        inOrder.verify(iContactRepository).save(contact);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void updateForUser_should_updateAndSaveContact_when_contactExistsAndEmailIsAvailable() {
        // Given
        var contact = Contact.builder().id(1L).firstName("cairedine").lastName("kalai").email("cairedine.kalai@afd_tech.com").build();

        var mockOwner = DBUser.builder().sub("goku").username("Son Goku").build();

        given(iContactRepository.findByIdAndOwnerSub(anyLong(), anyString())).willReturn(Optional.of(contact));

        given(iContactRepository.save(any(Contact.class))).willReturn(contact);
        //when
        contactService.updateForUser(mockOwner.getSub(), 1L, contact);
        //then
        InOrder inOrder = inOrder(iContactRepository);
        inOrder.verify(iContactRepository).findByIdAndOwnerSub(1L, mockOwner.getSub());

        ArgumentCaptor<Contact> contactCaptor = ArgumentCaptor.forClass(Contact.class);
        inOrder.verify(iContactRepository).save(contactCaptor.capture());

        Contact contactCapture = contactCaptor.getValue();
        assertEquals("cairedine.kalai@afd_tech.com", contactCapture.getEmail(), "L'email doit être mis à jour");
        assertEquals("cairedine", contactCapture.getFirstName(), "Le prénom doit être mis à jour");
        assertEquals("kalai", contactCapture.getLastName(), "Le nom doit être mis à jour");
        assertEquals(1L, contactCapture.getId(), "L'ID du contact doit rester identique");

        verifyNoMoreInteractions(iContactRepository);

    }

    @Test
    void updateForUser_should_throwException_when_emailAlreadyExists() {
        // Given
        var mockOwner = DBUser.builder().sub("goku").username("Son Goku").build();

        var existingContact = Contact.builder().id(1L).email("ancien@email.com").build();
        var updatedData = Contact.builder().email("deja.pris@email.com").build(); // Email différent !

        given(iContactRepository.findByIdAndOwnerSub(1L, "goku")).willReturn(Optional.of(existingContact));
        given(iContactRepository.existsByEmailIgnoreCase("deja.pris@email.com")).willReturn(true);

        //when
        assertThrows(EmailAlreadyExistsException.class, () ->  contactService.updateForUser(mockOwner.getSub(), 1L, updatedData));
        //then
        verify(iContactRepository).findByIdAndOwnerSub(1L, "goku");
        verify(iContactRepository).existsByEmailIgnoreCase("deja.pris@email.com");
        verify(iContactRepository, never()).save(any());

    }

    @Test
    void findByIdForUser_should_returnContactFromGeneralSearch_when_userIsAdmin() {
        // Given
        var mockAdminUser = DBUser.builder().sub("admin-sub-123").username("admin").role("ADMIN").build();
        var expectedContact = Contact.builder().id(1L).firstName("user").lastName("User").build();
        given(iContactRepository.findById(1L)).willReturn(Optional.of(expectedContact));

        // When
        Contact result = contactService.findByIdForUser(mockAdminUser.getSub(), 1L, true);

        // Then
        assertEquals(expectedContact, result);

        verify(iContactRepository).findById(1L);
        verify(iContactRepository, never()).findByIdAndOwnerSub(anyLong(), anyString());
    }

    @Test
    void findByIdForUser_should_returnContactFromSecureSearch_when_userIsNotAdmin() {
        // Given
        var mockUser = DBUser.builder().sub("user-sub-123").username("user").role("USER").build();
        var expectedContact = Contact.builder().id(1L).firstName("user").lastName("User").build();
        given(iContactRepository.findByIdAndOwnerSub(1L, mockUser.getSub())).willReturn(Optional.of(expectedContact));

        // When
        Contact result = contactService.findByIdForUser(mockUser.getSub(), 1L, false);

        // Then
        assertEquals(expectedContact, result);

        verify(iContactRepository).findByIdAndOwnerSub(1L, mockUser.getSub());
        verify(iContactRepository, never()).findById(anyLong());
    }

    @Test
    void findByIdForUser_should_throwException_when_notAdminAndContactNotFoundOrNotOwned() {
        // Given
        Long id = 99L;
        String sub = "user-123";

        // On simule que le repository ne trouve rien pour ce couple ID/Propriétaire
        given(iContactRepository.findByIdAndOwnerSub(id, sub))
                .willReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            contactService.findByIdForUser(sub, id, false)
        );

        // On vérifie que le message d'erreur correspond à celui défini dans ton service
        assertEquals("Contact introuvable ou non autorisé: 99", exception.getMessage());

        // Vérification finale : on s'assure que findById (la méthode admin) n'a jamais été appelée
        verify(iContactRepository).findByIdAndOwnerSub(id, sub);
        verify(iContactRepository, never()).findById(anyLong());
    }

    @Test
    void deleteById() {
        doNothing().when(iContactRepository).deleteById(1L);
        contactService.deleteById(1L);
        InOrder inOrder = inOrder(iContactRepository);
        inOrder.verify(iContactRepository).deleteById(1L);

        verify(iContactRepository, times(1)).deleteById(1L);
    }
}