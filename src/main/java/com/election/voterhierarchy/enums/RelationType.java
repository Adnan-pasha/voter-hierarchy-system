package com.election.voterhierarchy.enums;

public enum RelationType {
    FAMILY_HEAD("Family Head"),
    SPOUSE("Spouse"),
    SON("Son"),
    DAUGHTER("Daughter"),
    DEPENDENT("Dependent");

    private final String displayName;

    RelationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
