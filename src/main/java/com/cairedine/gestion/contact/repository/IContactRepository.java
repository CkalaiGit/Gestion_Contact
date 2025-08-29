package com.cairedine.gestion.contact.repository;

import com.cairedine.gestion.contact.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IContactRepository extends JpaRepository<Contact, Long> {}
