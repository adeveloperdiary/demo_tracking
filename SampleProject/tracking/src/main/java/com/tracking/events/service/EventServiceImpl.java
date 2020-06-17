package com.tracking.events.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracking.events.repository.EventRepository;
import com.tracking.events.repository.EventWeeklyReportRepository;
import com.tracking.events.repository.entity.Event;
import com.tracking.events.repository.entity.WeeklyReport;
import com.tracking.exception.APIException;
import com.tracking.rsql.CustomRsqlVisitor;
import com.tracking.users.repository.UserRepository;
import com.tracking.users.repository.entity.User;
import com.tracking.util.TrackingErrorConstant;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the event rest api service class. This class implements all the methods needed for the business
 * functionalities.
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@Service
public class EventServiceImpl implements EventService {
    @Autowired
    TrackingErrorConstant trackingErrorConstant;

    Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${app.config.weather.api.key:b1b15e88fa797225412429c1c50c122a1}")
    private String strWeatherAPIKey;

    @Value("${app.config.weather.api.endpoint:http://localhost:8008/weather}")
    public String strEndpointURL = "http://localhost:8008/weather";

    @Value("${app.config.weather.api.type:hour}")
    private String strType;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventWeeklyReportRepository reportRepository;

    /**
     * This method consists of the logic for finding all the events from te database.
     * It validates the logged in user role, then executes the appropriate query using JPA.
     *
     * @param pageable
     * @param authentication
     * @return Page<Event>
     */
    public Page<Event> findAllEvents(Pageable pageable, OAuth2Authentication authentication) {
        /* Fetch the principle data ( email ) and the role from security context */
        String auth = (String) authentication.getUserAuthentication().getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        /* If the Role is USER then need to add the filter*/
        if (role.equals(User.Role.USER.name())) {
            /* Find the user by querying db, otherwise throw APIException */
            User user = userRepository.findByEmail(auth).orElseThrow(() -> new APIException(User.class, "email", auth));

            return eventRepository.findAllByUser(user, pageable);
        }
        return eventRepository.findAll(pageable);
    }

    /**
     * This method consists of the logic for creating the weekly report for average speed and distance.
     *
     * @param id
     * @param authentication
     * @return ResponseEntity<List < WeeklyReport>>
     */
    public ResponseEntity<List<WeeklyReport>> createWeeklyReport(Long id, OAuth2Authentication authentication) {

        /* Fetch the principle data ( email ) and the role from security context */
        String auth = (String) authentication.getUserAuthentication().getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        /* If the Role is USER then need to add the filter*/
        if (role.equals(User.Role.USER.name())) {
            /* Find the user by querying db, otherwise throw APIException */
            User user = userRepository.findByEmail(auth).orElseThrow(() -> new APIException(User.class, "email", auth));

            /* Pass the user id as the argument of the findWeeklyReport() function */
            return new ResponseEntity<List<WeeklyReport>>(reportRepository.findWeeklyReport(user.getId().intValue()), HttpStatus.OK);
        }

        if (id != null) {
            /* Find the user by querying db, otherwise throw APIException */
            User user = userRepository.findById(id).orElseThrow(() -> new APIException(User.class, "id", id.toString()));

            /* Pass the user id as the argument of the findWeeklyReport() function */
            return new ResponseEntity<List<WeeklyReport>>(reportRepository.findWeeklyReport(id.intValue()), HttpStatus.OK);
        } else {
            /* Throw error if id was not passed by consumer */
            throw new APIException(User.class, "id", "");
        }
    }

    /**
     * This method is used for preprocessing the event before creating a new one. It first validates the event data sent
     * by consumer then invokes the weather service to populate all the data.
     *
     * @param event
     * @return Event
     */
    @Override
    public Event processEvent(Event event) {

        /* Set id to null if id is passed by consumer */
        if (event.getId() != null) {
            event.setId(null);
        }
        // Validate distance value
        if (event.getDistance().doubleValue() < 0.0) {
            throw new APIException(Event.class, " Invalid distance", event.getDistance().toString());
        }
        // Validate Latitude value
        if (event.getLat().doubleValue() < -90.0 || event.getLat().doubleValue() > 90.0) {
            throw new APIException(Event.class, " Invalid latitude", event.getLat().toString());
        }
        // Validate Longitude value

        if (event.getLon().doubleValue() < -180.0 || event.getLon().doubleValue() > 180.0) {
            throw new APIException(Event.class, " Invalid longitude", event.getLon().toString());
        }
        // Fetch the weather condition data from external service
        String strWeatherCondition = fetchWeatherCondition(event);
        if (strWeatherCondition == null) {
            throw new APIException(Event.class, " Invalid weather condition", "null");
        }
        //Set the weather condition
        event.setWeatherCondition(strWeatherCondition);

        event.setCreatedDate(LocalDateTime.now());
        event.setModifiedDate(event.getCreatedDate());

        return event;
    }

    /**
     * This method is used for validating the event before updating the existing one.
     *
     * @param event
     * @return Event
     */
    @Override
    public Event validateEvent(Event event) {

        // Validate distance value
        if (event.getDistance().doubleValue() < 0.0) {
            throw new APIException(Event.class, " Invalid distance ", event.getDistance().toString());
        }
        // Validate Latitude value
        if (event.getLat().doubleValue() < -90.0 || event.getLat().doubleValue() > 90.0) {
            throw new APIException(Event.class, " Invalid latitude", event.getLat().toString());
        }
        // Validate Longitude value

        if (event.getLon().doubleValue() < -180.0 || event.getLon().doubleValue() > 180.0) {
            throw new APIException(Event.class, " Invalid longitude", event.getLon().toString());
        }
        //Set the weather condition
        if (event.getWeatherCondition() == null || event.getWeatherCondition().trim().equalsIgnoreCase("")) {
            throw new APIException(Event.class, " Invalid weather condition", "null or empty");
        }

        //Set the modified date
        event.setModifiedDate(LocalDateTime.now());

        return event;
    }

    /**
     * This method is uses RestTemplate to invoke the weather rest api for getting the past weather data based on
     * location and date.
     *
     * @param event
     * @return String
     */
    @Override
    public String fetchWeatherCondition(Event event) {
        String strWeatherCondition = null;
        try {

            // Create the header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            // Use SimpleDateFormat to convert the string to date
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            // Use UriComponentsBuilder to create the request URL
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(strEndpointURL)
                    .queryParam("lat", event.getLat().doubleValue())
                    .queryParam("lon", event.getLon().doubleValue())
                    .queryParam("start", format.parse(event.getDate().toString() + " " + event.getTime().toString()).getTime())
                    .queryParam("appid", strWeatherAPIKey)
                    .queryParam("type", strType)
                    .queryParam("cnt", 1);

            // Set the header
            HttpEntity<?> entity = new HttpEntity<>(headers);

            // Use Spring RestTemplate for service invocation
            RestTemplate restTemplate = new RestTemplate();

            // Call the weather service.
            HttpEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class);

            // Create Map object from JSON String using Jackson Library
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(response.getBody(), Map.class);

            // Get the weather condition from the json response root=>list[]=>weather[]=>main
            if (result.containsKey("list")) {
                List list = (List) result.get("list");
                if (list != null && list.size() > 0) {
                    Map<String, Object> elementOne = (Map<String, Object>) list.get(0);
                    if (elementOne != null && elementOne.containsKey("weather")) {
                        List weathers = (List) elementOne.get("weather");
                        if (weathers != null && weathers.size() > 0) {
                            Map<String, Object> weatherElementOne = (Map<String, Object>) weathers.get(0);
                            if (weatherElementOne != null && weatherElementOne.containsKey("main")) {
                                strWeatherCondition = (String) weatherElementOne.get("main");
                            }
                        }
                    }
                }

            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new APIException(Event.class, "Error Retrieving Weather Service", e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new APIException(Event.class, "Error Retrieving Weather Service", e.getMessage());
        }

        return strWeatherCondition;
    }

    /**
     * This method implements the search functionality for any event using advance filter operation. Using rsql-parser
     * for the advance filter capabilities. The rsql-parser default code needed to be updated for supporting LocalDate
     * and other fields. Please refer the package com.tracking.rsql for more details.
     *
     * @param search
     * @param pageable
     * @param authentication
     * @return Page<Event>
     */
    public Page<Event> find(String search, Pageable pageable, OAuth2Authentication authentication) {

        /* Need to fetch the email and the role of the user */
        String auth = (String) authentication.getUserAuthentication().getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        /* If the Role is USER then need to add the filter*/
        if (role.equals(User.Role.USER.name())) {

            /* Find the user by querying db, otherwise throw APIException */
            User user = userRepository.findByEmail(auth).orElseThrow(() -> new APIException(User.class, "email", auth));

            /* Create a new  Specification object to add additional filter criteria */
            Specification<Event> other = new Specification<Event>() {
                @Override
                public Predicate toPredicate(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    /* Build the equal criteria by providing the user entity. */
                    return criteriaBuilder.equal(root.get("user"), user);
                }
            };

            /* Parse the search string using RSQLParser and save the node object */
            Node rootNode = new RSQLParser().parse(search);

            /* Create a new Specification object using the node object and CustomRsqlVisitor */
            Specification<Event> spec = rootNode.accept(new CustomRsqlVisitor<Event>());

            /* Invoke the repository function findAll() by passing the rsql Specification and then add the
             * user filter as well. The  findAll function also takes pageable as the 2nd argument as the api
             * support automatic pagination and sorting */
            return eventRepository.findAll(Specification.where(spec).and(other), pageable);

        }
        /* Parse the search string using RSQLParser and save the node object */
        Node rootNode = new RSQLParser().parse(search);

        /* Create a new Specification object using the node object and CustomRsqlVisitor */
        Specification<Event> spec = rootNode.accept(new CustomRsqlVisitor<Event>());

        /* Invoke the repository function findAll() by passing the rsql Specification.The findAll function also takes
         * pageable as the 2nd argument as the api support automatic pagination and sorting */
        return eventRepository.findAll(spec, pageable);
    }
}
