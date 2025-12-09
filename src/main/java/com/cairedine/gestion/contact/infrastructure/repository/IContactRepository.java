package com.cairedine.gestion.contact.infrastructure.repository;

import com.cairedine.gestion.contact.domain.entity.Contact;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IContactRepository extends JpaRepository<@NonNull Contact, @NonNull Long> {

    @Query("""
            SELECT c
            FROM Contact c
            WHERE c.owner.sub = :sub
              AND (
                  LOWER(c.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR
                  LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR
                  LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            """)
    Page<@NonNull Contact> searchForUser(@Param("sub") String sub,
                                @Param("q") String q,
                                Pageable pageable);

    @Query("""
            SELECT c
            FROM Contact c
            WHERE c.owner.sub = :sub
            """)
    Page<@NonNull Contact> findAllByOwnerSub(@Param("sub") String sub, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Contact> findByIdAndOwnerSub(Long id, String sub);
}

