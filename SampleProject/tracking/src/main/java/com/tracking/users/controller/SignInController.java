package com.tracking.users.controller;

import com.tracking.users.repository.UserRepository;
import com.tracking.users.repository.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller class sign in endpoint. The urls in this class are public and can be accessed by any one.
 * This should be used to create new user and validate existing user
 *
 * @version 1.0
 * @dete 05-18-2020
 */

@Slf4j
@Validated
@RestController
@RequestMapping("/api/signin")
public class SignInController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public SignInController(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public User signin(@RequestParam String email, @RequestParam String password) {
        /* Create the User entity Object */
        User u = new User(null, email, passwordEncoder.encode(password), User.Role.USER, null);

        /* Save the new User in the DB */
        return repository.save(u);
    }

    @PostMapping("/validateEmail")
    public Boolean emailExists(@RequestParam String email) {
        return repository.existsByEmail(email);
    }
}
