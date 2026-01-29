package com.election.voterhierarchy.repository;

import com.election.voterhierarchy.entity.Family;
import com.election.voterhierarchy.enums.PersonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {

    Optional<Family> findByFamilyCode(String familyCode);

    @Override
    @Query("SELECT DISTINCT f FROM Family f")
    List<Family> findAll();

    @Query("SELECT f FROM Family f WHERE f.id = :id")
    Optional<Family> findByIdWithFullDetails(@Param("id") Long id);

    // Updated queries - join through members instead of familyHead
    @Query("SELECT DISTINCT f FROM Family f " +
           "LEFT JOIN f.members m " +
           "LEFT JOIN m.voterDetails2002 v2002 " +
           "WHERE (:familyCode IS NULL OR f.familyCode LIKE %:familyCode%) " +
           "AND (:contactNumber IS NULL OR f.contactNumber LIKE %:contactNumber%) " +
           "AND (:contactPerson IS NULL OR f.contactPerson LIKE %:contactPerson%) " +
           "AND (:status IS NULL OR (m.isFamilyHead = true AND m.status = :status)) " +
           "AND (:familyHeadName IS NULL OR (m.isFamilyHead = true AND v2002.name LIKE %:familyHeadName%))")
    List<Family> findByFilters(@Param("familyCode") String familyCode,
                              @Param("contactNumber") String contactNumber,
                              @Param("contactPerson") String contactPerson,
                              @Param("status") PersonStatus status,
                              @Param("familyHeadName") String familyHeadName);

    @Query("SELECT COUNT(DISTINCT f) FROM Family f")
    Long countAll();

    @Query("SELECT COUNT(DISTINCT f) FROM Family f " +
           "LEFT JOIN f.members m " +
           "WHERE m.isFamilyHead = true AND m.status = :status")
    Long countByStatus(@Param("status") PersonStatus status);

    @Query("SELECT SUM(SIZE(f.members)) FROM Family f")
    Long getTotalMembers();
}