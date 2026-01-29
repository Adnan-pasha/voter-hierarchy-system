package com.election.voterhierarchy.dto;

import com.election.voterhierarchy.enums.PersonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyHeadDTO {
    
    @NotBlank(message = "Contact person is required")
    private String contactPerson;
    
    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    // 2002 Voter Details (Always mandatory)
    @NotBlank(message = "Name (2002) is required")
    private String name2002;

    @NotBlank(message = "Parent/Spouse name (2002) is required")
    private String parentSpouseName2002;

    @NotBlank(message = "EPIC No (2002) is required")
    private String epicNo2002;

    @NotBlank(message = "AC No (2002) is required")
    private String acNo2002;

    @NotBlank(message = "Part No (2002) is required")
    private String partNo2002;

    @NotBlank(message = "Serial No (2002) is required")
    private String serialNo2002;

    // Status
    @NotNull(message = "Status is required")
    private PersonStatus status;

    // Current Voter Details (Conditional - only if Active)
    private String nameCurrent;
    private String parentSpouseNameCurrent;
    private String epicNoCurrent;
    private String acNoCurrent;
    private String partNoCurrent;
    private String serialNoCurrent;
    private String bloName;
    private String bloMobile;

}
