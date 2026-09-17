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
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean deleteById(ID id);
    List<T> search(Predicate<T> predicate);
    long count();
    void clear();
}
