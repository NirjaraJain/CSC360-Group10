package com.library.repository;

import com.library.model.BaseEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Generic repository interface providing CRUD and query capabilities for domain entities.
 *
 * @param <T>  Entity type extending BaseEntity<ID>
 * @param <ID> Key type identifier
 */
public interface GenericRepository<T extends BaseEntity<ID>, ID> {
    /**
     * Inserts the entity, or replaces the existing one with the same ID.
     *
     * @param entity the entity to store
     * @return the stored entity
     */
    T save(T entity);

    /**
     * Looks up an entity by its identifier.
     *
     * @param id the identifier to look up
     * @return the matching entity, or an empty {@link Optional} if none exists
     */
    Optional<T> findById(ID id);

    /**
     * Returns every stored entity.
     *
     * @return a new list containing all entities (empty if the repository is empty)
     */
    List<T> findAll();

    /**
     * Removes the entity with the given identifier.
     *
     * @param id the identifier of the entity to remove
     * @return {@code true} if an entity was removed, {@code false} otherwise
     */
    boolean deleteById(ID id);

    /**
     * Returns all entities matching the given predicate.
     *
     * @param predicate the filter to apply; if {@code null}, all entities are returned
     * @return a new list of matching entities
     */
    List<T> search(Predicate<T> predicate);

    /**
     * Returns the number of stored entities.
     *
     * @return the current entity count
     */
    long count();

    /**
     * Removes all entities from the repository.
     */
    void clear();
}
