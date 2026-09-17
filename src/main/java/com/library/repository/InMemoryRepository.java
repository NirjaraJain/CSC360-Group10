package com.library.repository;

import com.library.model.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic thread-safe in-memory repository implementation using ConcurrentHashMap.
 *
 * @param <T>  Entity type
 * @param <ID> Key identifier type
 */
public class InMemoryRepository<T extends BaseEntity<ID>, ID> implements GenericRepository<T, ID> {
    private final Map<ID, T> storage = new ConcurrentHashMap<>();

    @Override
    public T save(T entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Entity and Entity ID cannot be null");
        }
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(ID id) {
        if (id == null) return false;
        return storage.remove(id) != null;
    }

    @Override
    public List<T> search(Predicate<T> predicate) {
        if (predicate == null) return findAll();
        return storage.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
