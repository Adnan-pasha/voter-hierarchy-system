package com.election.voterhierarchy.dto;

import com.election.voterhierarchy.enums.PersonStatus;
import com.election.voterhierarchy.enums.RelationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HierarchyNode {
    private Long personId;
    private String name2002;
    private String parentSpouseName2002;
    private String nameCurrent;
    private String parentSpouseNameCurrent;
    private RelationType relationType;
    private PersonStatus status;
    private boolean isFamilyHead;
    
    @Builder.Default
    private List<HierarchyNode> children = new ArrayList<>();
    
    public void addChild(HierarchyNode child) {
        children.add(child);
    }
}
