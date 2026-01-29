package com.election.voterhierarchy.dto;

import com.election.voterhierarchy.enums.PersonStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyUpdateDTO {
    
    @NotBlank(message = "Contact person is required")
    private String contactPerson;
    
    @NotBlank(message = "Contact number is required")
    private String contactNumber;
    
    // Family Head 2002 Details
    @NotBlank(message = "Name (2002) is required")
    private String headName2002;
    
    @NotBlank(message = "Parent/Spouse name (2002) is required")
    private String headParentSpouseName2002;
    
    @NotBlank(message = "EPIC No (2002) is required")
    private String headEpicNo2002;
    
    @NotBlank(message = "AC No (2002) is required")
    private String headAcNo2002;
    
    @NotBlank(message = "Part No (2002) is required")
    private String headPartNo2002;
    
    @NotBlank(message = "Serial No (2002) is required")
    private String headSerialNo2002;
    
    private PersonStatus headStatus;
    
    // Family Head Current Details (if active)
    private String headNameCurrent;
    private String headParentSpouseNameCurrent;
    private String headEpicNoCurrent;
    private String headAcNoCurrent;
    private String headPartNoCurrent;
    private String headSerialNoCurrent;
    private String headBloName;
    private String headBloMobile;
}