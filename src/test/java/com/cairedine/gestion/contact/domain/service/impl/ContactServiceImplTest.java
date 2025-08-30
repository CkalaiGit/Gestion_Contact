package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.infrastructure.repository.IContactRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    IContactRepository iContactRepository;
    @InjectMocks
    ContactServiceImpl contactService;

    @Test
    void findAll_sortsByLastThenFirstName(){
        var cairedine = Contact.builder().firstName("cairedine").lastName("KALAI").build();
        var ada  = Contact.builder().firstName("ada").lastName("Lovelace").build();
        when(iContactRepository.findAll(any(Sort.class))).thenReturn(List.of(ada, cairedine));

        var list = contactService.findAllSorted();

        // Création d'un "captor" pour des arguments de type Sort
        ArgumentCaptor<Sort> sortCap = ArgumentCaptor.forClass(Sort.class);

        /*
        Quand Mockito exécute verify(...), il ne se contente pas de dire “ok, l’appel a eu lieu”,
           il stocke l’argument réel passé dans sortCap.
         */

        verify(iContactRepository).findAll(sortCap.capture());

        var sort = sortCap.getValue().toString();
        assertTrue(sort.contains("lastName: ASC"));
        assertTrue(sort.contains("firstName: ASC"));
        assertIterableEquals(List.of(ada, cairedine), list);

    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { " ", "\t", "\n", "   " })
    void findPage_noQuery_usesFindAll(String query) {
        // Arrange
        var expectedContact = Contact.builder().firstName("John").lastName("Doe").build();
        Page<Contact> expectedPage = new PageImpl<>(List.of(expectedContact));
        Mockito.when(iContactRepository.findAll(Mockito.any(Pageable.class))).thenReturn(expectedPage);

        // Act
        Page<Contact> result = contactService.findPage(query, 0, 10);

        // Assert
        Assertions.assertEquals(expectedPage, result);

        // Vérifie que search N'EST PAS appelé
        Mockito.verify(iContactRepository, Mockito.never())
                .search(Mockito.anyString(), Mockito.any(Pageable.class));

        // Capture et vérifie le Pageable utilisé par findAll
        ArgumentCaptor<Pageable> pageableCap = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(iContactRepository).findAll(pageableCap.capture());

        Pageable used = pageableCap.getValue();
        Assertions.assertEquals(0, used.getPageNumber());
        Assertions.assertEquals(10, used.getPageSize());

        // Vérifie le tri: lastName ASC puis firstName ASC
        Sort sort = used.getSort();
        Assertions.assertEquals(Sort.Direction.ASC, sort.getOrderFor("lastName").getDirection());
        Assertions.assertEquals(Sort.Direction.ASC, sort.getOrderFor("firstName").getDirection());
    }

    @Test
    void testFindPageWithQuery() {
        String query = "Doe";
        Pageable pageable = PageRequest.of(1, 5, Sort.by("lastName").and(Sort.by("firstName")));
        var contact = Contact.builder().firstName("John").lastName("Doe").build();
        Page<Contact> expectedPage = new PageImpl<>(List.of(contact));

        Mockito.when(iContactRepository.search(query.trim(), pageable)).thenReturn(expectedPage);

        Page<Contact> result = contactService.findPage(query, 1, 5);

        Assertions.assertEquals(expectedPage, result);
        Mockito.verify(iContactRepository).search(query.trim(), pageable);
        Mockito.verify(iContactRepository, Mockito.never()).findAll((Example<Contact>) any());
    }
}