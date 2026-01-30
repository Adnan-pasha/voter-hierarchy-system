package com.election.voterhierarchy.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Administrator", "Full system access including user management"),
    OPERATOR("Operator", "Create and manage family data"),
    VIEWER("Viewer", "Read-only access to family data");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}