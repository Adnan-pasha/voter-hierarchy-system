package com.election.voterhierarchy.enums;

public enum PersonStatus {
    ACTIVE("Active"),
    EXPIRED("Expired/Died");

    private final String displayName;

    PersonStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
