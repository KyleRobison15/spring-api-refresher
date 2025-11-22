package com.codewithmosh.store.users;

import com.krd.auth.repository.BaseUserRepository;

/**
 * User repository extending BaseUserRepository from krd-spring-starters.
 *
 * Inherited from BaseUserRepository:
 * - findByEmail(String email)
 * - existsByEmail(String email)
 * - All standard JpaRepository methods
 *
 * Add domain-specific queries here if needed.
 */
public interface UserRepository extends BaseUserRepository<User> {
    // No custom queries needed yet - all required methods inherited from BaseUserRepository
}
