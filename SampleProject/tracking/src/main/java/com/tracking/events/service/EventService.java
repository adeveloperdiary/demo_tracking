package com.tracking.events.service;

import com.tracking.events.repository.entity.Event;
import com.tracking.events.repository.entity.WeeklyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Interface of the event rest api service class.
 *
 * @version 1.0
 * @dete 05-18-2020
 */
public interface EventService {
    public Event processEvent(Event event);

    public Event validateEvent(Event event);

    public String fetchWeatherCondition(Event event);

    public Page<Event> findAllEvents(Pageable pageable, OAuth2Authentication authentication);

    public ResponseEntity<List<WeeklyReport>> createWeeklyReport(Long id, OAuth2Authentication authentication);

    public Page<Event> find(String search, Pageable pageable, OAuth2Authentication authentication);

}
