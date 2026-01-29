package com.election.voterhierarchy.dto;

import com.election.voterhierarchy.enums.PersonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyFilterDTO {
    private String familyCode;
    private String contactNumber;
    private String contactPerson;
    private PersonStatus status;
    private String familyHeadName;
}