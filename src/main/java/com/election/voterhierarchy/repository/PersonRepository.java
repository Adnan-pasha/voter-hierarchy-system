package com.election.voterhierarchy.repository;

import com.election.voterhierarchy.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    List<Person> findByFamilyIdAndIsFamilyHead(Long familyId, Boolean isFamilyHead);
}
