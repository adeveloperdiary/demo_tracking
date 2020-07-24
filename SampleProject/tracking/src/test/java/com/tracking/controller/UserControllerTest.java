package com.tracking.controller;

import com.tracking.exception.APIException;
import com.tracking.users.controller.UserController;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import javax.validation.ConstraintViolationException;
import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit Testing class for UserController
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Before
    public void setup() {

        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("pwd");

        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);
        user.setPassword(passwordEncoder.encode("pwd"));

        Mockito.when(userRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        Mockito.when(userRepository.existsById(Mockito.any(Long.class))).thenReturn(true);
        Mockito.doNothing().when(userRepository).deleteById(Mockito.any(Long.class));

        List<User> users = new ArrayList<>();
        users.add(user);
        Page<User> pagedResponse = new PageImpl<>(users);

        Mockito.when(userRepository.findAllByEmail(Mockito.anyString(), Mockito.any(Pageable.class))).thenReturn(pagedResponse);
        Mockito.when(userRepository.findByEmailContains(Mockito.anyString(), Mockito.any(Pageable.class))).thenReturn(pagedResponse);

    }

    @Test
    public void one() {
        Object user = userController.one(123L);
        assertTrue("[UserControllerTest->one()->User Object received", user instanceof User);
    }

    @Test(expected = APIException.class)
    public void oneDoesNotExists() {
        Mockito.when(userRepository.findById(Mockito.any(Long.class))).thenThrow(APIException.class);
        Object user = userController.one(123L);
    }

    @Test
    public void update() {
        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);


        userController.update(123L, user);
    }

    @Test
    public void create() {
        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);

        Object obj = userController.create(user);
        assertTrue("[UserControllerTest->create()->User Object received", obj instanceof User);
    }

    @Test
    public void delete() {
        userController.delete(123L);
    }

    @Test(expected = APIException.class)
    public void deleteException() {
        Mockito.when(userRepository.existsById(Mockito.any(Long.class))).thenReturn(false);
        userController.delete(123L);
    }

    @Test(expected = ConstraintViolationException.class)
    public void changePasswordConstraintViolationException() {
        userController.changePassword(123L, "pwd_wrong", "pwd1");
    }

    @Test(expected = APIException.class)
    public void changePasswordEntityNotFoundException() {
        Mockito.when(userRepository.findById(Mockito.any(Long.class))).thenThrow(APIException.class);
        userController.changePassword(123L, "pwd", "pwd1");
    }

    @Test
    public void changePassword() {
        Mockito.when(passwordEncoder.matches(Mockito.any(), Mockito.any())).thenReturn(true);
        userController.changePassword(123L, "pwd", "pwd1");
    }


    @Test
    public void allUSERRole() {
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

        userController.all(pageable, oAuth2Authentication);
    }

    @Test
    public void all_ADMIN_Role() {
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

        userController.all(pageable, oAuth2Authentication);
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

        userController.search("user1@user1.com", pageable, oAuth2Authentication);
    }
}
