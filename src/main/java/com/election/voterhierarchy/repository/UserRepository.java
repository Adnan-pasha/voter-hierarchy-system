package com.election.voterhierarchy.repository;

import com.election.voterhierarchy.entity.User;
import com.election.voterhierarchy.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role AND u.active = true")
    Long countActiveUsersByRole(@Param("role") Role role);
}