package com.election.voterhierarchy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {
    private String memberName;
    private String relationType;
    private String errorMessage;
    private String expectedValue;
    private String foundValue;
}
