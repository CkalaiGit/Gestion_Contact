package com.cairedine.gestion.contact.infrastructure.repository;

import com.cairedine.gestion.contact.domain.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IContactRepository extends JpaRepository<Contact, Long> {

    @Query("""
       SELECT c
       FROM Contact c
       WHERE LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(c.email)     LIKE LOWER(CONCAT('%', :q, '%'))
       """)
    Page<Contact> search(@Param("q") String q, Pageable pageable);
}

