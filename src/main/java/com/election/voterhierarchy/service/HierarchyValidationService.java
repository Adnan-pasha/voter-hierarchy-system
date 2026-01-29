package com.election.voterhierarchy.service;

import com.election.voterhierarchy.dto.ValidationError;
import com.election.voterhierarchy.entity.Person;
import com.election.voterhierarchy.entity.VoterDetails2002;
import com.election.voterhierarchy.entity.VoterDetailsCurrent;
import com.election.voterhierarchy.enums.RelationType;
import com.election.voterhierarchy.util.StringNormalizationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class HierarchyValidationService {

    /**
     * Validates family hierarchy based on CURRENT voter ID data only.
     * Validation Rules:
     * - SPOUSE: Member.Parent(Current) == FamilyHead.Name(2002)
     * - SON/DAUGHTER/DEPENDENT: Member.Parent(Current) == FamilyHead.Name(2002) OR Any Spouse.Name(2002)
     */
    public List<ValidationError> validateHierarchy(Person familyHead, List<Person> members) {
        List<ValidationError> errors = new ArrayList<>();

        if (familyHead == null || familyHead.getVoterDetails2002() == null) {
            log.warn("Family head or 2002 details missing, skipping validation");
            return errors;
        }

        String familyHeadName2002 = StringNormalizationUtil.normalize(
            familyHead.getVoterDetails2002().getName()
        );

        // Collect all spouse names from 2002 data
        Set<String> spouseNames2002 = new HashSet<>();
        for (Person member : members) {
            if (member.getRelationType() == RelationType.SPOUSE && 
                member.getVoterDetails2002() != null) {
                spouseNames2002.add(StringNormalizationUtil.normalize(
                    member.getVoterDetails2002().getName()
                ));
            }
        }

        log.debug("Family head name (2002): {}", familyHeadName2002);
        log.debug("Spouse names (2002): {}", spouseNames2002);

        // Validate each member
        for (Person member : members) {
            ValidationError error = validateMember(
                member, 
                familyHeadName2002, 
                spouseNames2002
            );
            
            if (error != null) {
                errors.add(error);
            }
        }

        return errors;
    }

    private ValidationError validateMember(Person member, String familyHeadName2002, Set<String> spouseNames2002) {
        // Skip validation if no current voter details (expired members without current ID)
        VoterDetailsCurrent currentDetails = member.getVoterDetailsCurrent();
        if (currentDetails == null) {
            log.debug("Skipping validation for member without current voter details");
            return null;
        }

        String memberParentCurrent = StringNormalizationUtil.normalize(
            currentDetails.getParentSpouseName()
        );
        String memberNameCurrent = StringNormalizationUtil.normalize(
            currentDetails.getName()
        );

        RelationType relationType = member.getRelationType();

        log.debug("Validating {} - Parent/Spouse (Current): {}", relationType, memberParentCurrent);

        if (relationType == RelationType.SPOUSE) {
            // SPOUSE validation: Member.Parent(Current) must match FamilyHead.Name(2002)
            if (!StringNormalizationUtil.equalsIgnoreCaseNormalized(memberParentCurrent, familyHeadName2002)) {
                return ValidationError.builder()
                    .memberName(memberNameCurrent)
                    .relationType(relationType.getDisplayName())
                    .errorMessage("Parent/Spouse mismatch (CURRENT VOTER ID)")
                    .expectedValue(familyHeadName2002)
                    .foundValue(memberParentCurrent)
                    .build();
            }
        } else if (relationType == RelationType.SON || 
                   relationType == RelationType.DAUGHTER || 
                   relationType == RelationType.DEPENDENT) {
            // SON/DAUGHTER/DEPENDENT validation: 
            // Member.Parent(Current) must match FamilyHead.Name(2002) OR any Spouse.Name(2002)
            boolean matchesHead = StringNormalizationUtil.equalsIgnoreCaseNormalized(
                memberParentCurrent, familyHeadName2002
            );
            
            boolean matchesSpouse = spouseNames2002.stream()
                .anyMatch(spouseName -> StringNormalizationUtil.equalsIgnoreCaseNormalized(
                    memberParentCurrent, spouseName
                ));

            if (!matchesHead && !matchesSpouse) {
                String expectedValues = familyHeadName2002;
                if (!spouseNames2002.isEmpty()) {
                    expectedValues += " OR " + String.join(" OR ", spouseNames2002);
                }

                return ValidationError.builder()
                    .memberName(memberNameCurrent)
                    .relationType(relationType.getDisplayName())
                    .errorMessage("Parent/Spouse mismatch (CURRENT VOTER ID)")
                    .expectedValue(expectedValues)
                    .foundValue(memberParentCurrent)
                    .build();
            }
        }

        return null; // No error
    }
}
