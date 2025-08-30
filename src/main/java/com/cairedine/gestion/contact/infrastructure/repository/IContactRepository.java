package com.cairedine.gestion.contact.infrastructure.repository;

import com.cairedine.gestion.contact.domain.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IContactRepository extends JpaRepository<Contact, Long> {}
