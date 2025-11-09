package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.domain.exception.EmailAlreadyExistsException;
import com.cairedine.gestion.contact.infrastructure.repository.IContactRepository;
import com.cairedine.gestion.contact.infrastructure.repository.IUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Objects;
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
    @WithMockUser(username = "alice")
    void findPage_noQuery_usesFindAll(String query) {
        // Arrange
        var expectedContact = Contact.builder().firstName("John").lastName("Doe").build();
        Page<Contact> expectedPage = new PageImpl<>(List.of(expectedContact));
        when(iContactRepository.findAllByOwnerUsername(eq("alice"), any(Pageable.class))).thenReturn(expectedPage);

        // Act
        Page<Contact> result = contactService.findPageForUser("alice", query, 0, 10);

        // Assert
        assertEquals(expectedPage, result);

        // Vérifie que search N'EST PAS appelé
        Mockito.verify(iContactRepository, never())
                .searchForUser(anyString(), anyString(), any(Pageable.class));

        // Capture et vérifie le Pageable utilisé par findAll
        ArgumentCaptor<Pageable> pageableCap = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(iContactRepository).findAllByOwnerUsername(eq("alice"), pageableCap.capture());

        Pageable used = pageableCap.getValue();
        Assertions.assertEquals(0, used.getPageNumber());
        Assertions.assertEquals(10, used.getPageSize());

        // Vérifie le tri: lastName ASC puis firstName ASC
        Sort sort = used.getSort();
        Assertions.assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sort.getOrderFor("lastName")).getDirection());
        Assertions.assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sort.getOrderFor("firstName")).getDirection());
    }

    @Test
    @WithMockUser(username = "alice")
    void testFindPageWithQuery() {
        String query = "Doe";
        Pageable pageable = PageRequest.of(1, 5, Sort.by("lastName").and(Sort.by("firstName")));
        var contact = Contact.builder().firstName("John").lastName("Doe").build();
        Page<Contact> expectedPage = new PageImpl<>(List.of(contact));

        Mockito.when(iContactRepository.searchForUser("alice", query.trim(), pageable)).thenReturn(expectedPage);

        Page<Contact> result = contactService.findPageForUser("alice", query, 1, 5);

        Assertions.assertEquals(expectedPage, result);
        Mockito.verify(iContactRepository).searchForUser("alice", query.trim(), pageable);
    }

    @Test
    void create_should_throw_conflict_when_email_exists() {
        var contact = Contact.builder().email("cairedine.kalai@afd_tech.com").build();
        given(iContactRepository.existsByEmailIgnoreCase("cairedine.kalai@afd_tech.com")).willReturn(true);


        assertThrows(EmailAlreadyExistsException.class, () -> contactService.createForUser("goku",contact));

        then(iContactRepository).should().existsByEmailIgnoreCase("cairedine.kalai@afd_tech.com");
        then(iContactRepository).should(never()).save(any(Contact.class));
        then(iContactRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @WithMockUser(username = "alice")
    void create_should_save_when_email_is_free() {
        // Given
        var contact = Contact.builder().email("cairedine.kalai@afd_tech.com").build();
        given(iContactRepository.existsByEmailIgnoreCase("cairedine.kalai@afd_tech.com")).willReturn(false);

        DBUser dbUser = new DBUser();
        dbUser.setUsername("alice");
        given(iUserRepository.findByUsername("alice")).willReturn(Optional.of(dbUser));

        // When
        contactService.createForUser("alice", contact);

        // Then
        InOrder inOrder = inOrder(iContactRepository);
        inOrder.verify(iContactRepository).existsByEmailIgnoreCase("cairedine.kalai@afd_tech.com");
        inOrder.verify(iContactRepository).save(contact);
        inOrder.verifyNoMoreInteractions();
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