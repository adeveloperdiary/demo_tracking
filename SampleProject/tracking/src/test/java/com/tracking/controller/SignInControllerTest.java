package com.tracking.controller;

import com.tracking.events.repository.entity.WeeklyReport;
import com.tracking.users.controller.SignInController;
import com.tracking.users.repository.UserRepository;
import com.tracking.users.repository.entity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.Assert.*;

import java.util.*;

/**
 * Unit Testing class for SignInController
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@RunWith(MockitoJUnitRunner.class)
public class SignInControllerTest {

    @InjectMocks
    private SignInController signInController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Before
    public void setup() {
        User user = new User();
        user.setId(123L);
        user.setEmail("user1@user1.com");
        user.setRole(User.Role.USER);

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        Mockito.when(userRepository.existsByEmail(Mockito.any(String.class))).thenReturn(true);

    }

    @Test
    public void signin() {
        signInController = new SignInController(userRepository, passwordEncoder);
        Object user = signInController.signin("user1@user1.com", "pwd");
        assertTrue("[SignInControllerTest->signin()->User Object received", user instanceof User);
    }

    @Test
    public void emailExists() {
        boolean exists = signInController.emailExists("user1@user1.com");
        assertEquals("[SignInControllerTest->emailExists()->User Exists", true, exists);
    }

}
