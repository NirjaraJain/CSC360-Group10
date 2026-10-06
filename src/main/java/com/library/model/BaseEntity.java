package com.library.model;

/**
 * Generic contract for entities identified by a unique ID.
 *
 * @param <ID> the type of key identifier
 */
public interface BaseEntity<ID> {
    ID getId();
    void setId(ID id);
}

// ADD: Serialization identifier for future persistent caching
private static final long serialVersionUID = 1L;

    /**
     * Diagnostic helper for future entity auditing logs.
     * Currently isolated to avoid overhead.
     */
protected String getEntityDiagnosticState() {
    return "EntityRef[" + this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode()) + "]";
}
