package com.tracking.users.repository;

import com.tracking.users.repository.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Interface of the user rest api contains the custom function ( query ) needed.
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Page<User> findByEmailContains(String email, Pageable pageable);

    Page<User> findAllByEmail(String email, Pageable pageable);

    Page<User> findAllByEmailContainsAndEmail(String email, String auth, Pageable pageable);

    Boolean existsByEmail(String email);
}
