package com.cairedine.gestion.contact.domain.service.impl;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.infrastructure.repository.IContactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

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
    ContactServiceImpl service;

    @Test
    void findAll_sortsByLastThenFirstName(){
        var cairedine = Contact.builder().firstName("cairedine").lastName("KALAI").build();
        var ada  = Contact.builder().firstName("ada").lastName("Lovelace").build();
        when(iContactRepository.findAll(any(Sort.class))).thenReturn(List.of(ada, cairedine));

        var list = service.findAllSorted();
        ArgumentCaptor<Sort> sortCap = ArgumentCaptor.forClass(Sort.class);
        verify(iContactRepository).findAll(sortCap.capture());
        var sort = sortCap.getValue().toString();
        assertTrue(sort.contains("lastName: ASC"));
        assertTrue(sort.contains("firstName: ASC"));
        assertIterableEquals(List.of(ada, cairedine), list);

    }

}