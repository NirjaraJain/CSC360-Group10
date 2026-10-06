package com.library.model;

/**
 * Generic contract for entities identified by a unique ID.
 *
 * @param <ID> the type of key identifier
 */
public interface BaseEntity<ID> {
        /**
     * Returns the unique identifier of this entity.
     *
     * @return the identifier, or {@code null} if one has not been assigned yet
     */
    ID getId();

    /**
     * Assigns the unique identifier of this entity.
     *
     * @param id the identifier to assign
     */
    void setId(ID id);
}
