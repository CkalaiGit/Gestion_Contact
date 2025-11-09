package com.cairedine.gestion.contact.infrastructure.repository;

import com.cairedine.gestion.contact.domain.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IContactRepository extends JpaRepository<Contact, Long> {

   @Query("""
    SELECT c
    FROM Contact c
    WHERE c.owner.username = :username
      AND (
          LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :q, '%')) OR
          LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR
          LOWER(c.email)     LIKE LOWER(CONCAT('%', :q, '%'))
      )
    """)
    Page<Contact> searchForUser(@Param("username") String username, @Param("q") String q, Pageable pageable);


    @Query("""
    SELECT c
    FROM Contact c
    WHERE c.owner.username = :username
    """)
    Page<Contact> findAllByOwnerUsername(@Param("username") String username, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);
    Optional<Contact> findByIdAndOwnerUsername(Long id, String username);
}

