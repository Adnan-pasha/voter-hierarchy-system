package com.election.voterhierarchy.dto;

import com.election.voterhierarchy.enums.PersonStatus;
import com.election.voterhierarchy.enums.RelationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonUpdateDTO {
    
    private Long personId;
    private RelationType relationType;
    private Integer age;
    private PersonStatus status;
    
    // 2002 Voter Details
    private String name2002;
    private String parentSpouseName2002;
    private String epicNo2002;
    private String acNo2002;
    private String partNo2002;
    private String serialNo2002;
    
    // Current Voter Details
    private String nameCurrent;
    private String parentSpouseNameCurrent;
    private String epicNoCurrent;
    private String acNoCurrent;
    private String partNoCurrent;
    private String serialNoCurrent;
    private String bloName;
    private String bloMobile;
}