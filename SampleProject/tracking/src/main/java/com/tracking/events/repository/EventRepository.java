package com.tracking.events.repository;

import com.tracking.events.repository.entity.Event;

import com.tracking.users.repository.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Interface of the event rest api. The findAllByUser custom function has been defined.
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@Repository
public interface EventRepository
        extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event>,
        PagingAndSortingRepository<Event, Long> {
    Page<Event> findAllByUser(User user, Pageable pageable);
}
