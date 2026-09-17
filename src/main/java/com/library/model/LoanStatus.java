package com.library.model;

/**
 * Enum representing loan circulation states.
 */
public enum LoanStatus {
    ACTIVE("Active"),
    OVERDUE("Overdue"),
    RETURNED("Returned");

    private final String displayName;

    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
