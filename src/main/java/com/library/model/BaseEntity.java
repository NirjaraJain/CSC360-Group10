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
