package com.tracking.events.controller;


import com.tracking.events.repository.entity.WeeklyReport;
import com.tracking.events.service.EventService;

import com.tracking.events.repository.EventRepository;
import com.tracking.events.repository.entity.Event;
import com.tracking.exception.APIException;
import com.tracking.users.repository.UserRepository;
import com.tracking.users.repository.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller class of the event rest api endpoints. This class implements the CRUD operations
 * using Spring Boot's @RestController annotation.
 *
 * @version 1.0
 * @dete 05-18-2020
 */

@RestController
@RequestMapping("/api/event")
@Validated
public class EventController {

    Logger logger = LoggerFactory.getLogger(getClass());

    /* The instance variable of EventService class */
    @Autowired
    private EventService service;

    /* The instance variable of EventRepository class */
    @Autowired
    private EventRepository repository;

    /* The instance variable of UserRepository class */
    @Autowired
    private UserRepository userRepository;


    public EventController(EventService service, EventRepository repository, UserRepository userRepository) {
        this.service = service;
        this.repository = repository;
        this.userRepository = userRepository;
    }

    /**
     * This method implements the endpoint to return the list of all the events as json array.
     *
     * @param pageable
     * @param authentication
     * @return Page<Event>
     */

    @GetMapping
    public Page<Event> all(Pageable pageable, OAuth2Authentication authentication) {
        return service.findAllEvents(pageable, authentication);
    }

    /**
     * This method implements the endpoint to generate weekly report for getting average speed and distance per user.
     *
     * @param id
     * @param authentication
     * @return ResponseEntity<List < WeeklyReport>>
     */
    @GetMapping("/report")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('USER')")
    public ResponseEntity<List<WeeklyReport>> report(@RequestParam(value = "id", required = false) Long id,
                                                     OAuth2Authentication authentication) {
        return service.createWeeklyReport(id, authentication);
    }

    /**
     * This method implements the endpoint to search for any event using advance filter operation. Using rsql-parser
     * for the advance filter capabilities. The rsql-parser default code needed to be updated for supporting LocalDate
     * and other fields. Please refer the package com.tracking.rsql for more details.
     *
     * @param search
     * @param pageable
     * @param authentication
     * @return Page<Event>
     */
    @GetMapping("/find")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('USER')")
    public Page<Event> find(@RequestParam(value = "search") String search,
                            Pageable pageable, OAuth2Authentication authentication) {
        return service.find(search, pageable, authentication);
    }

    /**
     * This method implements the endpoint to fetch an existing event from the database. The @PostAuthorize annotation was
     * added to make sure only ADMIN can fetch event for any user or user can fetch events only for him/her self.
     * In case the role is USER and the id passed does not match with the one from OAuth server, then the function
     * will be executed but unauthorised error will be sent back to consumer.
     *
     * @param id
     * @return Event
     */
    @GetMapping("/{id}")
    @PostAuthorize("hasAuthority('ADMIN') || (returnObject.user == @userRepository.findByEmail(authentication.principal).get() && hasAuthority('USER'))")
    public Event one(@PathVariable Long id) {
        /* Find the event or throw error if not found*/
        return repository.findById(id).orElseThrow(() -> new APIException(Event.class, " Event not found with id ", id.toString()));
    }

    /**
     * This method implements the endpoint to update an existing event in the database. The @PreAuthorize annotation was
     * added to make sure only ADMIN can update event for any user or user can update events only for him/her self.
     * In case the role is USER and the id passed does not match with the one from OAuth server, then the function
     * wont be executed and unauthorised error will be sent back to consumer. The newly updated event will be returned
     * back to consumer as JSON object.The @Valid annotation has been used to make sure all the required parameters are
     * provided by consumer.
     *
     * @param id
     * @param newEvent
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') || (@eventRepository.findById(#id).orElse(new com.tracking.events.repository.entity.Event()).user == @userRepository.findByEmail(authentication.principal).get() && hasAuthority('USER'))")
    public void update(@PathVariable Long id, @Valid @RequestBody Event newEvent) {
        /* Make sure the event is available in the db */
        if (repository.existsById(id)) {

            /* Set the id of the event */
            newEvent.setId(id);

            /* Find the existing event */
            Event existingEvent = repository.findById(id).get();

            /* Set the created date and user */
            newEvent.setCreatedDate(existingEvent.getCreatedDate());
            newEvent.setUser(existingEvent.getUser());

            /* validate the event by validating the event data */
            newEvent = service.validateEvent(newEvent);

            /* Save the event */
            repository.save(newEvent);
        } else {
            /* Throw error if no event with the given id is present in the database */
            throw new APIException(Event.class, " Event not found with id ", id.toString());
        }
    }

    /**
     * This method implements the endpoint to create a new event in the database. The @PreAuthorize annotation was
     * added to make sure only ADMIN can add new event for any user or user can add events only for him/her self.
     * In case the role is USER and the id passed does not match with the one from OAuth server, then the function
     * wont be executed and unauthorised error will be sent back to consumer. The newly added event will be returned
     * back to consumer as JSON object.The @Valid annotation has been used to make sure all the required parameters are
     * provided by consumer.
     *
     * @param event
     * @return Event
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') || (#event != null && #event.user.id == @userRepository.findByEmail(authentication.principal).get().id && hasAuthority('USER'))")
    public Event create(@Valid @RequestBody Event event) {

        /* first processing the event by validating the event data and fetching weather api service */
        event = service.processEvent(event);

        /* Save the new event to the database*/
        Event newEvent = repository.save(event);
        newEvent.setUser(userRepository.findById(newEvent.getUser().getId()).get());

        return newEvent;
    }

    /**
     * This method implements the endpoint to delete an event from the database. The @PreAuthorize annotation was
     * added to make sure only ADMIN can delete event for any user or user can delete events only for him/her self.
     * In case the role is USER and the id passed does not match with the one from OAuth server, then the function
     * wont be executed and unauthorised error will be sent back to consumer.
     *
     * @param id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') || (@eventRepository.findById(#id).orElse(new com.tracking.events.repository.entity.Event()).user == @userRepository.findByEmail(authentication.principal).get() && hasAuthority('USER'))")
    public void delete(@PathVariable Long id) {

        /* Make sure the event is available in the db */
        if (repository.existsById(id)) {
            /* Save the event */
            repository.deleteById(id);
        } else {
            /* Throw error if no event with the given id is present in the database */
            throw new APIException(Event.class, " Event not found with id ", id.toString());
        }
    }

}
