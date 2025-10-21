package com.cairedine.gestion.contact.infrastructure.repository;

import com.cairedine.gestion.contact.domain.entity.DBUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<DBUser, Long> {

    @Query("SELECT U FROM USER U WHERE U.USERNAME = ?1")
    Optional<DBUser> findUserWithName(String username);
}
