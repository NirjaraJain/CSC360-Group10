package com.library.model;

/**
 * Enum representing loan circulation states.
 */
public enum LoanStatus {
    /** The book is currently on loan and the due date has not passed. */
    ACTIVE("Active"),
    /** The book is on loan and the due date has passed. */
    OVERDUE("Overdue"),
    /** The book has been returned to the library. */
    RETURNED("Returned");

    /** Human-readable label shown in the UI. */
    private final String displayName;

    /**
     * Creates a status with the given display label.
     *
     * @param displayName the label presented to the user
     */
    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable label for this status.
     *
     * @return the display name, never {@code null}
     */
    public String getDisplayName() {
        return displayName;
    }
}
