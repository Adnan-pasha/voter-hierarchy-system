package com.election.voterhierarchy.service;

import org.hibernate.Hibernate;
import com.election.voterhierarchy.dto.*;
import com.election.voterhierarchy.entity.*;
import com.election.voterhierarchy.enums.PersonStatus;
import com.election.voterhierarchy.enums.RelationType;
import com.election.voterhierarchy.repository.FamilyRepository;
import com.election.voterhierarchy.repository.PersonRepository;
import com.election.voterhierarchy.util.StringNormalizationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final HierarchyValidationService validationService;
    private final PersonRepository personRepository;

    @Transactional
    public Family createFamilyWithHead(FamilyHeadDTO dto) {
        log.info("Creating new family with head");

        // Normalize all string inputs
        normalizeDTO(dto);

        // Validate conditional fields
        validateFamilyHeadDTO(dto);

        // Create family
        Family family = Family.builder()
            .familyCode(generateFamilyCode())
            .createdBy("OPERATOR") // TODO: Get from security context
            .contactPerson(dto.getContactPerson())
            .contactNumber(dto.getContactNumber())
            .build();

        // Create family head person
        Person familyHead = Person.builder()
            .family(family)
            .isFamilyHead(true)
            .relationType(RelationType.FAMILY_HEAD)
            .status(dto.getStatus())
            .build();

        // Create 2002 voter details (always mandatory for family head)
        VoterDetails2002 voterDetails2002 = VoterDetails2002.builder()
            .person(familyHead)
            .name(dto.getName2002())
            .parentSpouseName(dto.getParentSpouseName2002())
            .epicNo(dto.getEpicNo2002())
            .acNo(dto.getAcNo2002())
            .partNo(dto.getPartNo2002())
            .serialNo(dto.getSerialNo2002())
            .build();

        familyHead.setVoterDetails2002(voterDetails2002);

        // Create current voter details if status is ACTIVE
        if (dto.getStatus() == PersonStatus.ACTIVE) {
            VoterDetailsCurrent currentDetails = VoterDetailsCurrent.builder()
                .person(familyHead)
                .name(dto.getNameCurrent())
                .parentSpouseName(dto.getParentSpouseNameCurrent())
                .epicNo(dto.getEpicNoCurrent())
                .acNo(dto.getAcNoCurrent())
                .partNo(dto.getPartNoCurrent())
                .serialNo(dto.getSerialNoCurrent())
                .build();

            BloDetails bloDetails = BloDetails.builder()
                .voterDetailsCurrent(currentDetails)
                .bloName(dto.getBloName())
                .bloMobile(dto.getBloMobile())
                .build();

            currentDetails.setBloDetails(bloDetails);
            familyHead.setVoterDetailsCurrent(currentDetails);
        }

		family.addMember(familyHead);

        Family savedFamily = familyRepository.save(family);
        log.info("Family created with ID: {} and code: {}", savedFamily.getId(), savedFamily.getFamilyCode());

        return savedFamily;
    }

    @Transactional
    public void addFamilyMember(Long familyId, FamilyMemberDTO dto) {
        log.info("Adding member to family ID: {}", familyId);

        // Normalize all string inputs
        normalizeDTO(dto);

        // Validate conditional fields
        validateFamilyMemberDTO(dto);

        Family family = familyRepository.findById(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found with ID: " + familyId));

        // Create family member person
        Person member = Person.builder()
            .family(family)
            .isFamilyHead(false)
            .relationType(dto.getRelationType())
            .age(dto.getAge())
            .status(dto.getStatus())
            .build();

        // Create 2002 voter details if age >= 41
        if (dto.getAge() >= 41) {
            VoterDetails2002 voterDetails2002 = VoterDetails2002.builder()
                .person(member)
                .name(dto.getName2002())
                .parentSpouseName(dto.getParentSpouseName2002())
                .epicNo(dto.getEpicNo2002())
                .acNo(dto.getAcNo2002())
                .partNo(dto.getPartNo2002())
                .serialNo(dto.getSerialNo2002())
                .build();

            member.setVoterDetails2002(voterDetails2002);
        }

        // Create current voter details if status is ACTIVE
        if (dto.getStatus() == PersonStatus.ACTIVE) {
            VoterDetailsCurrent currentDetails = VoterDetailsCurrent.builder()
                .person(member)
                .name(dto.getNameCurrent())
                .parentSpouseName(dto.getParentSpouseNameCurrent())
                .epicNo(dto.getEpicNoCurrent())
                .acNo(dto.getAcNoCurrent())
                .partNo(dto.getPartNoCurrent())
                .serialNo(dto.getSerialNoCurrent())
                .build();

            BloDetails bloDetails = BloDetails.builder()
                .voterDetailsCurrent(currentDetails)
                .bloName(dto.getBloName())
                .bloMobile(dto.getBloMobile())
                .build();

            currentDetails.setBloDetails(bloDetails);
            member.setVoterDetailsCurrent(currentDetails);
        }

        family.addMember(member);
        familyRepository.save(family);

        log.info("Member added successfully to family ID: {}", familyId);
    }

    @Transactional(readOnly = true)
    public HierarchyNode buildHierarchy(Long familyId) {
        Family family = familyRepository.findByIdWithFullDetails(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found with ID: " + familyId));

        Person familyHead = family.getFamilyHead();
        if (familyHead == null) {
            throw new RuntimeException("Family head not found for family ID: " + familyId);
        }

        // Build root node (family head)
        HierarchyNode rootNode = buildNodeFromPerson(familyHead);

        // Add all NON-HEAD members as children
        for (Person member : family.getMembers()) {
            if (member.getIsFamilyHead() == null || !member.getIsFamilyHead()) {
                HierarchyNode childNode = buildNodeFromPerson(member);
                rootNode.addChild(childNode);
            }
        }

        return rootNode;
    }

    @Transactional(readOnly = true)
    public List<ValidationError> validateFamily(Long familyId) {
        Family family = familyRepository.findByIdWithFullDetails(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found with ID: " + familyId));

        Person familyHead = family.getFamilyHead();
        
        // Get only non-head members for validation
        List<Person> nonHeadMembers = family.getMembers().stream()
            .filter(p -> p.getIsFamilyHead() == null || !p.getIsFamilyHead())
            .collect(Collectors.toList());

        return validationService.validateHierarchy(familyHead, nonHeadMembers);  
    }

    @Transactional(readOnly = true)
	public List<Family> getAllFamilies() {
		return familyRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Family getFamilyById(Long id) {
		return familyRepository.findByIdWithFullDetails(id)
			.orElseThrow(() -> new RuntimeException("Family not found with ID: " + id));
	}

    private HierarchyNode buildNodeFromPerson(Person person) {
        HierarchyNode.HierarchyNodeBuilder builder = HierarchyNode.builder()
            .personId(person.getId())
            .relationType(person.getRelationType())
            .status(person.getStatus())
            .isFamilyHead(person.getIsFamilyHead());

        // Add 2002 data
        if (person.getVoterDetails2002() != null) {
            builder.name2002(person.getVoterDetails2002().getName())
                   .parentSpouseName2002(person.getVoterDetails2002().getParentSpouseName());
        }

        // Add current data
        if (person.getVoterDetailsCurrent() != null) {
            builder.nameCurrent(person.getVoterDetailsCurrent().getName())
                   .parentSpouseNameCurrent(person.getVoterDetailsCurrent().getParentSpouseName());
        }

        return builder.build();
    }

    private String generateFamilyCode() {
        return "FAM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void normalizeDTO(FamilyHeadDTO dto) {
        dto.setContactPerson(StringNormalizationUtil.normalize(dto.getContactPerson()));
        dto.setContactNumber(StringNormalizationUtil.normalize(dto.getContactNumber()));
        dto.setName2002(StringNormalizationUtil.normalize(dto.getName2002()));
        dto.setParentSpouseName2002(StringNormalizationUtil.normalize(dto.getParentSpouseName2002()));
        dto.setEpicNo2002(StringNormalizationUtil.normalize(dto.getEpicNo2002()));
        dto.setAcNo2002(StringNormalizationUtil.normalize(dto.getAcNo2002()));
        dto.setPartNo2002(StringNormalizationUtil.normalize(dto.getPartNo2002()));
        dto.setSerialNo2002(StringNormalizationUtil.normalize(dto.getSerialNo2002()));

        if (dto.getStatus() == PersonStatus.ACTIVE) {
            dto.setNameCurrent(StringNormalizationUtil.normalize(dto.getNameCurrent()));
            dto.setParentSpouseNameCurrent(StringNormalizationUtil.normalize(dto.getParentSpouseNameCurrent()));
            dto.setEpicNoCurrent(StringNormalizationUtil.normalize(dto.getEpicNoCurrent()));
            dto.setAcNoCurrent(StringNormalizationUtil.normalize(dto.getAcNoCurrent()));
            dto.setPartNoCurrent(StringNormalizationUtil.normalize(dto.getPartNoCurrent()));
            dto.setSerialNoCurrent(StringNormalizationUtil.normalize(dto.getSerialNoCurrent()));
            dto.setBloName(StringNormalizationUtil.normalize(dto.getBloName()));
            dto.setBloMobile(StringNormalizationUtil.normalize(dto.getBloMobile()));
        }
    }

    private void normalizeDTO(FamilyMemberDTO dto) {
        if (dto.getAge() >= 41) {
            dto.setName2002(StringNormalizationUtil.normalize(dto.getName2002()));
            dto.setParentSpouseName2002(StringNormalizationUtil.normalize(dto.getParentSpouseName2002()));
            dto.setEpicNo2002(StringNormalizationUtil.normalize(dto.getEpicNo2002()));
            dto.setAcNo2002(StringNormalizationUtil.normalize(dto.getAcNo2002()));
            dto.setPartNo2002(StringNormalizationUtil.normalize(dto.getPartNo2002()));
            dto.setSerialNo2002(StringNormalizationUtil.normalize(dto.getSerialNo2002()));
        }

        if (dto.getStatus() == PersonStatus.ACTIVE) {
            dto.setNameCurrent(StringNormalizationUtil.normalize(dto.getNameCurrent()));
            dto.setParentSpouseNameCurrent(StringNormalizationUtil.normalize(dto.getParentSpouseNameCurrent()));
            dto.setEpicNoCurrent(StringNormalizationUtil.normalize(dto.getEpicNoCurrent()));
            dto.setAcNoCurrent(StringNormalizationUtil.normalize(dto.getAcNoCurrent()));
            dto.setPartNoCurrent(StringNormalizationUtil.normalize(dto.getPartNoCurrent()));
            dto.setSerialNoCurrent(StringNormalizationUtil.normalize(dto.getSerialNoCurrent()));
            dto.setBloName(StringNormalizationUtil.normalize(dto.getBloName()));
            dto.setBloMobile(StringNormalizationUtil.normalize(dto.getBloMobile()));
        }
    }

    private void validateFamilyHeadDTO(FamilyHeadDTO dto) {
        if (dto.getContactPerson() == null || dto.getContactPerson().isBlank()) {
                throw new IllegalArgumentException("Contact Person is required to proceed");
        }
        if (dto.getContactNumber() == null || dto.getContactNumber().isBlank()) {
            throw new IllegalArgumentException("Contact Number is required to proceed");
        }
        if (dto.getStatus() == PersonStatus.ACTIVE) {
            if (dto.getNameCurrent() == null || dto.getNameCurrent().isBlank()) {
                throw new IllegalArgumentException("Current Name is required for Active status");
            }
            if (dto.getParentSpouseNameCurrent() == null || dto.getParentSpouseNameCurrent().isBlank()) {
                throw new IllegalArgumentException("Current Parent/Spouse Name is required for Active status");
            }
            if (dto.getEpicNoCurrent() == null || dto.getEpicNoCurrent().isBlank()) {
                throw new IllegalArgumentException("Current EPIC No is required for Active status");
            }
            if (dto.getAcNoCurrent() == null || dto.getAcNoCurrent().isBlank()) {
                throw new IllegalArgumentException("Current AC No is required for Active status");
            }
            if (dto.getPartNoCurrent() == null || dto.getPartNoCurrent().isBlank()) {
                throw new IllegalArgumentException("Current Part No is required for Active status");
            }
            if (dto.getSerialNoCurrent() == null || dto.getSerialNoCurrent().isBlank()) {
                throw new IllegalArgumentException("Current Serial No is required for Active status");
            }
            if (dto.getBloName() == null || dto.getBloName().isBlank()) {
                throw new IllegalArgumentException("BLO Name is required for Active status");
            }
            if (dto.getBloMobile() == null || dto.getBloMobile().isBlank()) {
                throw new IllegalArgumentException("BLO Mobile is required for Active status");
            }
        }
    }

    private void validateFamilyMemberDTO(FamilyMemberDTO dto) {
        // Validate 2002 details if age >= 41
        if (dto.getAge() >= 41) {
            if (dto.getName2002() == null || dto.getName2002().isBlank()) {
                throw new IllegalArgumentException("2002 Name is required for age >= 41");
            }
            if (dto.getParentSpouseName2002() == null || dto.getParentSpouseName2002().isBlank()) {
                throw new IllegalArgumentException("2002 Parent/Spouse Name is required for age >= 41");
            }
            if (dto.getEpicNo2002() == null || dto.getEpicNo2002().isBlank()) {
                throw new IllegalArgumentException("2002 EPIC No is required for age >= 41");
            }
            if (dto.getAcNo2002() == null || dto.getAcNo2002().isBlank()) {
                throw new IllegalArgumentException("2002 AC No is required for age >= 41");
            }
            if (dto.getPartNo2002() == null || dto.getPartNo2002().isBlank()) {
                throw new IllegalArgumentException("2002 Part No is required for age >= 41");
            }
            if (dto.getSerialNo2002() == null || dto.getSerialNo2002().isBlank()) {
                throw new IllegalArgumentException("2002 Serial No is required for age >= 41");
            }
        }

        // Validate current details if status is ACTIVE
        if (dto.getStatus() == PersonStatus.ACTIVE) {
            if (dto.getNameCurrent() == null || dto.getNameCurrent().isBlank()) {
                throw new IllegalArgumentException("Current Name is required for Active status");
            }
            if (dto.getParentSpouseNameCurrent() == null || dto.getParentSpouseNameCurrent().isBlank()) {
                throw new IllegalArgumentException("Current Parent/Spouse Name is required for Active status");
            }
            if (dto.getEpicNoCurrent() == null || dto.getEpicNoCurrent().isBlank()) {
                throw new IllegalArgumentException("Current EPIC No is required for Active status");
            }
            if (dto.getAcNoCurrent() == null || dto.getAcNoCurrent().isBlank()) {
                throw new IllegalArgumentException("Current AC No is required for Active status");
            }
            if (dto.getPartNoCurrent() == null || dto.getPartNoCurrent().isBlank()) {
                throw new IllegalArgumentException("Current Part No is required for Active status");
            }
            if (dto.getSerialNoCurrent() == null || dto.getSerialNoCurrent().isBlank()) {
                throw new IllegalArgumentException("Current Serial No is required for Active status");
            }
            if (dto.getBloName() == null || dto.getBloName().isBlank()) {
                throw new IllegalArgumentException("BLO Name is required for Active status");
            }
            if (dto.getBloMobile() == null || dto.getBloMobile().isBlank()) {
                throw new IllegalArgumentException("BLO Mobile is required for Active status");
            }
        }
    }

    @Transactional
    public void updateFamily(Long familyId, FamilyUpdateDTO dto) {
        log.info("Updating family ID: {}", familyId);
        
        Family family = familyRepository.findById(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found with ID: " + familyId));
        
        // Update family contact info
        family.setContactPerson(StringNormalizationUtil.normalize(dto.getContactPerson()));
        family.setContactNumber(StringNormalizationUtil.normalize(dto.getContactNumber()));
        
        // Update family head (get from members)
        Person familyHead = family.getFamilyHead();
        if (familyHead != null) {
            familyHead.setStatus(dto.getHeadStatus());
            
            // Update 2002 details
            if (familyHead.getVoterDetails2002() != null) {
                VoterDetails2002 details2002 = familyHead.getVoterDetails2002();
                details2002.setName(StringNormalizationUtil.normalize(dto.getHeadName2002()));
                details2002.setParentSpouseName(StringNormalizationUtil.normalize(dto.getHeadParentSpouseName2002()));
                details2002.setEpicNo(StringNormalizationUtil.normalize(dto.getHeadEpicNo2002()));
                details2002.setAcNo(StringNormalizationUtil.normalize(dto.getHeadAcNo2002()));
                details2002.setPartNo(StringNormalizationUtil.normalize(dto.getHeadPartNo2002()));
                details2002.setSerialNo(StringNormalizationUtil.normalize(dto.getHeadSerialNo2002()));
            }
            
            // Update or create current details if status is ACTIVE
            if (dto.getHeadStatus() == PersonStatus.ACTIVE) {
                VoterDetailsCurrent currentDetails = familyHead.getVoterDetailsCurrent();
                
                if (currentDetails == null) {
                    currentDetails = new VoterDetailsCurrent();
                    familyHead.setVoterDetailsCurrent(currentDetails);
                }
                
                currentDetails.setName(StringNormalizationUtil.normalize(dto.getHeadNameCurrent()));
                currentDetails.setParentSpouseName(StringNormalizationUtil.normalize(dto.getHeadParentSpouseNameCurrent()));
                currentDetails.setEpicNo(StringNormalizationUtil.normalize(dto.getHeadEpicNoCurrent()));
                currentDetails.setAcNo(StringNormalizationUtil.normalize(dto.getHeadAcNoCurrent()));
                currentDetails.setPartNo(StringNormalizationUtil.normalize(dto.getHeadPartNoCurrent()));
                currentDetails.setSerialNo(StringNormalizationUtil.normalize(dto.getHeadSerialNoCurrent()));
                
                // Update BLO details
                BloDetails bloDetails = currentDetails.getBloDetails();
                if (bloDetails == null) {
                    bloDetails = new BloDetails();
                    currentDetails.setBloDetails(bloDetails);
                }
                bloDetails.setBloName(StringNormalizationUtil.normalize(dto.getHeadBloName()));
                bloDetails.setBloMobile(StringNormalizationUtil.normalize(dto.getHeadBloMobile()));
                
            } else {
                // Remove current details if status changed to EXPIRED
                familyHead.setVoterDetailsCurrent(null);
            }
        }
        
        familyRepository.save(family);
        log.info("Family updated successfully: {}", familyId);
    }

    @Transactional
    public void updateMember(Long memberId, PersonUpdateDTO dto) {
        log.info("Updating member ID: {}", memberId);
        
        Person member = personRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));
        
        // Update basic info
        member.setRelationType(dto.getRelationType());
        member.setAge(dto.getAge());
        member.setStatus(dto.getStatus());
        
        // Update 2002 details if age >= 41
        if (dto.getAge() >= 41) {
            VoterDetails2002 details2002 = member.getVoterDetails2002();
            
            if (details2002 == null) {
                details2002 = new VoterDetails2002();
                member.setVoterDetails2002(details2002);
            }
            
            details2002.setName(StringNormalizationUtil.normalize(dto.getName2002()));
            details2002.setParentSpouseName(StringNormalizationUtil.normalize(dto.getParentSpouseName2002()));
            details2002.setEpicNo(StringNormalizationUtil.normalize(dto.getEpicNo2002()));
            details2002.setAcNo(StringNormalizationUtil.normalize(dto.getAcNo2002()));
            details2002.setPartNo(StringNormalizationUtil.normalize(dto.getPartNo2002()));
            details2002.setSerialNo(StringNormalizationUtil.normalize(dto.getSerialNo2002()));
        } else {
            // Remove 2002 details if age < 41
            member.setVoterDetails2002(null);
        }
        
        // Update current details if status is ACTIVE
        if (dto.getStatus() == PersonStatus.ACTIVE) {
            VoterDetailsCurrent currentDetails = member.getVoterDetailsCurrent();
            
            if (currentDetails == null) {
                currentDetails = new VoterDetailsCurrent();
                member.setVoterDetailsCurrent(currentDetails);
            }
            
            currentDetails.setName(StringNormalizationUtil.normalize(dto.getNameCurrent()));
            currentDetails.setParentSpouseName(StringNormalizationUtil.normalize(dto.getParentSpouseNameCurrent()));
            currentDetails.setEpicNo(StringNormalizationUtil.normalize(dto.getEpicNoCurrent()));
            currentDetails.setAcNo(StringNormalizationUtil.normalize(dto.getAcNoCurrent()));
            currentDetails.setPartNo(StringNormalizationUtil.normalize(dto.getPartNoCurrent()));
            currentDetails.setSerialNo(StringNormalizationUtil.normalize(dto.getSerialNoCurrent()));
            
            // Update BLO details
            BloDetails bloDetails = currentDetails.getBloDetails();
            if (bloDetails == null) {
                bloDetails = new BloDetails();
                currentDetails.setBloDetails(bloDetails);
            }
            bloDetails.setBloName(StringNormalizationUtil.normalize(dto.getBloName()));
            bloDetails.setBloMobile(StringNormalizationUtil.normalize(dto.getBloMobile()));
            
        } else {
            // Remove current details if status changed to EXPIRED
            member.setVoterDetailsCurrent(null);
        }
        
        personRepository.save(member);
        log.info("Member updated successfully: {}", memberId);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        log.info("Deleting member ID: {}", memberId);
        
        Person member = personRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));
        
        if (member.getIsFamilyHead() != null && member.getIsFamilyHead()) {
            throw new RuntimeException("Cannot delete family head. Delete the entire family instead.");
        }
        
        personRepository.delete(member);
        log.info("Member deleted successfully: {}", memberId);
    }

    @Transactional(readOnly = true)
    public Person getMemberById(Long memberId) {
        return personRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));
    }

    @Transactional
    public void deleteFamily(Long familyId) {
        log.info("Deleting family ID: {}", familyId);
        
        Family family = familyRepository.findById(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found with ID: " + familyId));
        
        familyRepository.delete(family);
        log.info("Family deleted successfully: {}", familyId);
    }

    @Transactional(readOnly = true)
    public List<Family> searchFamilies(FamilyFilterDTO filter) {
        return familyRepository.findByFilters(
            filter.getFamilyCode(),
            filter.getContactNumber(),
            filter.getContactPerson(),
            filter.getStatus(),
            filter.getFamilyHeadName()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalFamilies", familyRepository.countAll());
        stats.put("activeFamilies", familyRepository.countByStatus(PersonStatus.ACTIVE));
        stats.put("expiredFamilies", familyRepository.countByStatus(PersonStatus.EXPIRED));
        stats.put("totalMembers", familyRepository.getTotalMembers());
        
        return stats;
    }
}
