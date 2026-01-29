package com.election.voterhierarchy.dto;

import com.election.voterhierarchy.enums.PersonStatus;
import com.election.voterhierarchy.enums.RelationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMemberDTO {

    @NotNull(message = "Relation type is required")
    private RelationType relationType;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    private Integer age;

    @NotNull(message = "Status is required")
    private PersonStatus status;

    // 2002 Voter Details (Conditional - only if age >= 41)
    private String name2002;
    private String parentSpouseName2002;
    private String epicNo2002;
    private String acNo2002;
    private String partNo2002;
    private String serialNo2002;

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
