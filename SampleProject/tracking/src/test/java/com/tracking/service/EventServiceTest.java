package com.tracking.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.tracking.events.repository.EventRepository;
import com.tracking.events.repository.entity.Event;
import com.tracking.exception.APIException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import com.tracking.events.repository.EventWeeklyReportRepository;
import com.tracking.events.repository.entity.WeeklyReport;
import com.tracking.events.service.EventServiceImpl;
import com.tracking.users.repository.UserRepository;
import com.tracking.users.repository.entity.User;
import com.tracking.users.repository.entity.User.Role;
import org.springframework.web.client.RestTemplate;

/**
 * Unit Testing class for EventService
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {

    @InjectMocks
    private EventServiceImpl eventServiceImpl;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventWeeklyReportRepository reportRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RestTemplate restTemplate;


    @Before
    public void setup() {
        List<WeeklyReport> weeklyReportList = new ArrayList<WeeklyReport>();

        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);

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
        Mockito.when(userRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findAllByUser(Mockito.any(User.class), Mockito.any(Pageable.class))).thenReturn(pagedResponse);
        Mockito.when(eventRepository.findAll(Mockito.any(Pageable.class))).thenReturn(pagedResponse);

        Mockito.when(reportRepository.findWeeklyReport(Mockito.anyInt())).thenReturn(weeklyReportList);

    }

    @Test
    public void reportUSERRole() {
        Set<String> setString = new HashSet<String>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        Map<String, String> requestParameters = new HashMap<String, String>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "clientId", authorities, true, setString,
                setString, "url", setString, extensionProperties);

        SimpleGrantedAuthority SimpleGrantedAuthority = new SimpleGrantedAuthority(Role.USER.name());
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(SimpleGrantedAuthority);

        Authentication userAuthentication = new UsernamePasswordAuthenticationToken("UserName", "Password",
                grantedAuthorityList);

        OAuth2Authentication OAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);
        eventServiceImpl.createWeeklyReport(123L, OAuth2Authentication);

    }

    @Test
    public void reportADMINRole() {
        Set<String> setString = new HashSet<String>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        Map<String, String> requestParameters = new HashMap<String, String>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "clientId", authorities, true, setString,
                setString, "url", setString, extensionProperties);

        SimpleGrantedAuthority SimpleGrantedAuthority = new SimpleGrantedAuthority(Role.ADMIN.name());
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(SimpleGrantedAuthority);

        Authentication userAuthentication = new UsernamePasswordAuthenticationToken("UserName", "Password",
                grantedAuthorityList);

        OAuth2Authentication OAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);
        eventServiceImpl.createWeeklyReport(123L, OAuth2Authentication);

    }

    @Test(expected = APIException.class)
    public void reportADMINRoleEntityNotFoundException() {
        Set<String> setString = new HashSet<String>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        Map<String, String> requestParameters = new HashMap<String, String>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "clientId", authorities, true, setString,
                setString, "url", setString, extensionProperties);

        SimpleGrantedAuthority SimpleGrantedAuthority = new SimpleGrantedAuthority(Role.ADMIN.name());
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(SimpleGrantedAuthority);

        Authentication userAuthentication = new UsernamePasswordAuthenticationToken("UserName", "Password",
                grantedAuthorityList);

        OAuth2Authentication OAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);
        eventServiceImpl.createWeeklyReport(null, OAuth2Authentication);

    }

    @Test
    public void findAllEventsUSERRole() {
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

        eventServiceImpl.findAllEvents(pageable, oAuth2Authentication);

    }

    @Test
    public void findAllEventsADMINRole() {
        Set<String> setString = new HashSet<String>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        Map<String, String> requestParameters = new HashMap<String, String>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "clientId", authorities, true, setString,
                setString, "url", setString, extensionProperties);

        SimpleGrantedAuthority SimpleGrantedAuthority = new SimpleGrantedAuthority(User.Role.ADMIN.name());
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(SimpleGrantedAuthority);

        Authentication userAuthentication = new UsernamePasswordAuthenticationToken("UserName", "Password",
                grantedAuthorityList);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);

        Pageable pageable = PageRequest.of(0, 2);

        eventServiceImpl.findAllEvents(pageable, oAuth2Authentication);

    }

    @Test
    public void processEvent() {
        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);

        Event event = new Event();
        event.setId(1L);
        event.setModifiedDate(LocalDateTime.now());
        event.setCreatedDate(LocalDateTime.now());
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLon(-85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");
        event.setUser(user);

        eventServiceImpl.processEvent(event);
    }

    @Test(expected = APIException.class)
    public void processEventDistanceError() {
        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);

        Event event = new Event();
        event.setId(1L);
        event.setModifiedDate(LocalDateTime.now());
        event.setCreatedDate(LocalDateTime.now());
        event.setDate(LocalDate.now());
        event.setDistance(-100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLon(-85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");
        event.setUser(user);

        eventServiceImpl.processEvent(event);
    }

    @Test(expected = APIException.class)
    public void processEventLatError() {

        Event event = new Event();
        event.setId(1L);
        event.setModifiedDate(LocalDateTime.now());
        event.setCreatedDate(LocalDateTime.now());
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(-91.5086);
        event.setLon(85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");

        eventServiceImpl.processEvent(event);
    }


    @Test(expected = APIException.class)
    public void processEventLonError() {

        Event event = new Event();
        event.setId(1L);
        event.setModifiedDate(LocalDateTime.now());
        event.setCreatedDate(LocalDateTime.now());
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLon(-8500.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");

        eventServiceImpl.processEvent(event);
    }

    @Test
    public void validateEvent() {
        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);

        Event event = new Event();
        event.setId(1L);
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLon(-85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");

        eventServiceImpl.validateEvent(event);
    }

    @Test(expected = APIException.class)
    public void validateEventDistanceError() {
        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);

        Event event = new Event();
        event.setId(1L);
        event.setDate(LocalDate.now());
        event.setDistance(-100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLon(-85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");
        event.setUser(user);

        eventServiceImpl.validateEvent(event);
    }

    @Test(expected = APIException.class)
    public void validateEventLatError() {

        Event event = new Event();
        event.setId(1L);
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(-91.5086);
        event.setLon(85.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");

        eventServiceImpl.validateEvent(event);
    }


    @Test(expected = APIException.class)
    public void validateEventLonError() {

        Event event = new Event();
        event.setId(1L);
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLon(-8500.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("sunny");

        eventServiceImpl.validateEvent(event);
    }

    @Test(expected = APIException.class)
    public void validateEventWeatherNullError() {

        Event event = new Event();
        event.setId(1L);
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLon(-8500.3454);
        event.setTime(LocalTime.now());

        eventServiceImpl.validateEvent(event);
    }

    @Test(expected = APIException.class)
    public void validateEventWeatherEmptyError() {

        Event event = new Event();
        event.setId(1L);
        event.setDate(LocalDate.now());
        event.setDistance(100.0);
        event.setDuration(10.5);
        event.setLat(34.5086);
        event.setLon(-8500.3454);
        event.setTime(LocalTime.now());
        event.setWeatherCondition("");

        eventServiceImpl.validateEvent(event);
    }

    @Test
    public void findUSERRole() {

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

        eventServiceImpl.find("distance>800 and weatherCondition==sunny", pageable, oAuth2Authentication);

    }

    @Test
    public void findADMINRole() {

        Set<String> setString = new HashSet<String>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        Map<String, String> requestParameters = new HashMap<String, String>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "clientId", authorities, true, setString,
                setString, "url", setString, extensionProperties);

        SimpleGrantedAuthority SimpleGrantedAuthority = new SimpleGrantedAuthority(User.Role.ADMIN.name());
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(SimpleGrantedAuthority);

        Authentication userAuthentication = new UsernamePasswordAuthenticationToken("UserName", "Password",
                grantedAuthorityList);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);

        Pageable pageable = PageRequest.of(0, 2);

        eventServiceImpl.find("distance>800 and weatherCondition==sunny", pageable, oAuth2Authentication);

    }

    @Test
    public void findADMINRoleQueries() {

        Set<String> setString = new HashSet<String>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        Map<String, String> requestParameters = new HashMap<String, String>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "clientId", authorities, true, setString,
                setString, "url", setString, extensionProperties);

        SimpleGrantedAuthority SimpleGrantedAuthority = new SimpleGrantedAuthority(User.Role.ADMIN.name());
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(SimpleGrantedAuthority);

        Authentication userAuthentication = new UsernamePasswordAuthenticationToken("UserName", "Password",
                grantedAuthorityList);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);

        Pageable pageable = PageRequest.of(0, 2);

        eventServiceImpl.find("distance>800 or weatherCondition==sunny", pageable, oAuth2Authentication);
        eventServiceImpl.find("( distance<=3.0 and distance >100 ) and date==2008-11-04", pageable, oAuth2Authentication);
        eventServiceImpl.find("(distance>800 or distance < 400) and weatherCondition==sunny", pageable, oAuth2Authentication);

    }

}
