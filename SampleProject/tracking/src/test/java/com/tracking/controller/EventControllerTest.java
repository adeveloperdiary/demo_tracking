package com.tracking.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import com.tracking.events.repository.EventRepository;
import com.tracking.events.repository.entity.Event;
import com.tracking.exception.APIException;
import com.tracking.users.repository.UserRepository;
import com.tracking.users.repository.entity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import com.tracking.events.controller.EventController;
import com.tracking.events.repository.entity.WeeklyReport;
import com.tracking.events.service.EventService;

import static org.junit.Assert.assertTrue;

/**
 * Unit Testing class for EventController
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@RunWith(MockitoJUnitRunner.class)
public class EventControllerTest {

    @InjectMocks
    private EventController eventController;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventService eventService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Before
    public void setup() {
        ResponseEntity<List<WeeklyReport>> responseEntity = new ResponseEntity<List<WeeklyReport>>(HttpStatus.OK);

        Mockito.when(eventService.createWeeklyReport(Mockito.anyLong(), Mockito.any()))
                .thenReturn(responseEntity);


        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("pwd");

        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);
        user.setPassword(passwordEncoder.encode("pwd"));

        Event event = new Event();
        event.setId(1L);
        event.setModifiedDate(LocalDateTime.now());
        event.setCreatedDate(LocalDateTime.now());
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLat(-85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");
        event.setUser(user);


        List<Event> events = new ArrayList<>();
        events.add(event);
        Page<Event> pagedResponse = new PageImpl<>(events);

        Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        Mockito.when(eventService.findAllEvents(Mockito.any(Pageable.class), Mockito.any(OAuth2Authentication.class))).thenReturn(pagedResponse);

        Mockito.when(eventService.find(Mockito.anyString(), Mockito.any(Pageable.class), Mockito.any(OAuth2Authentication.class))).thenReturn(pagedResponse);

        Mockito.when(eventRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(event));

        Mockito.when(eventRepository.existsById(Mockito.any(Long.class))).thenReturn(true);

        Mockito.when(eventRepository.save(Mockito.any(Event.class))).thenReturn(event);

        Mockito.when(eventService.processEvent(Mockito.any(Event.class))).thenReturn(event);

    }


    @Test
    public void report() {
        Set<String> setString = new HashSet<String>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        Map<String, String> requestParameters = new HashMap<String, String>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "clientId", authorities, true, setString,
                setString, "url", setString, extensionProperties);
        Authentication userAuthentication = new UsernamePasswordAuthenticationToken("admin1@admin1.com", "pwd");
        OAuth2Authentication OAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);
        eventController.report(123L, OAuth2Authentication);
    }

    @Test
    public void all() {
        Set<String> setString = new HashSet<String>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        Map<String, String> requestParameters = new HashMap<String, String>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "clientId", authorities, true, setString,
                setString, "url", setString, extensionProperties);

        SimpleGrantedAuthority SimpleGrantedAuthority = new SimpleGrantedAuthority(User.Role.USER.name());
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(SimpleGrantedAuthority);

        Authentication userAuthentication = new UsernamePasswordAuthenticationToken("UserName", "Password",
                grantedAuthorityList);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);

        Pageable pageable = PageRequest.of(0, 2);

        eventController.all(pageable, oAuth2Authentication);
    }

    @Test
    public void search() {
        Set<String> setString = new HashSet<String>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        Map<String, String> requestParameters = new HashMap<String, String>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "clientId", authorities, true, setString,
                setString, "url", setString, extensionProperties);

        SimpleGrantedAuthority SimpleGrantedAuthority = new SimpleGrantedAuthority(User.Role.USER.name());
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(SimpleGrantedAuthority);

        Authentication userAuthentication = new UsernamePasswordAuthenticationToken("UserName", "Password",
                grantedAuthorityList);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);

        Pageable pageable = PageRequest.of(0, 2);

        eventController.find("distance>800 and weatherCondition==sunny", pageable, oAuth2Authentication);
    }

    @Test
    public void one() {
        Object event = eventController.one(123L);
        assertTrue("[EventControllerTest->one()->User Object received", event instanceof Event);
    }

    @Test(expected = APIException.class)
    public void oneEntityNotFoundException() {
        Mockito.when(eventRepository.findById(Mockito.anyLong())).thenThrow(APIException.class);

        Object event = eventController.one(123L);
        assertTrue("[EventControllerTest->one()->User Object received", event instanceof Event);
    }

    @Test
    public void update() {
        Event event = new Event();
        event.setId(1L);
        event.setModifiedDate(LocalDateTime.now());
        event.setCreatedDate(LocalDateTime.now());
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLat(-85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");

        eventController.update(123L, event);
    }

    @Test(expected = APIException.class)
    public void updateEntityNotFoundException() {
        Event event = new Event();
        event.setId(1L);
        event.setModifiedDate(LocalDateTime.now());
        event.setCreatedDate(LocalDateTime.now());
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLat(-85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");

        Mockito.when(eventRepository.existsById(Mockito.any(Long.class))).thenReturn(false);

        eventController.update(123L, event);
    }

    @Test
    public void create() {
        Event event = new Event();
        event.setId(1L);
        event.setModifiedDate(LocalDateTime.now());
        event.setCreatedDate(LocalDateTime.now());
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLat(-85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");

        Object obj = eventController.create(event);
        assertTrue("[EventControllerTest->create()->Event Object received", obj instanceof Event);
    }

    @Test
    public void delete() {
        eventController.delete(123L);
    }

    @Test(expected = APIException.class)
    public void deleteException() {
        Mockito.when(eventRepository.existsById(Mockito.any(Long.class))).thenReturn(false);
        eventController.delete(123L);
    }
}