package com.tracking.users.controller;

import com.tracking.exception.APIException;
import com.tracking.users.repository.UserRepository;
import com.tracking.users.repository.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.HashSet;

/**
 * Controller class for the user entity. All the roles will have full / limited access to the endpoints.
 *
 * @version 1.0
 * @dete 05-18-2020
 */

@RestController
@RequestMapping("/api/users")
@Slf4j
@Validated
public class UserController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    UserController(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * This method implements the endpoint to return the list of all the users as json array.
     * ADMIN and USER_MANAGER can find all the users, however USER can only find his/her details
     *
     * @param pageable
     * @param authentication
     * @return Page<Event>
     */
    @GetMapping
    public Page<User> all(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageable, OAuth2Authentication authentication) {
        /* Find the authorization details */
        String auth = (String) authentication.getUserAuthentication().getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        /* USER Role can only find his/her details */
        if (role.equals(User.Role.USER.name())) {
            return repository.findAllByEmail(auth, pageable);
        }

        /* USER MANAGER/ADMIN Role can find any record */
        return repository.findAll(pageable);
    }

    /**
     * This method implements the endpoint to search for any user using the email address. Partial email address can
     * be used to make the search. USER role does not have access to this endpoint.
     *
     * @param email
     * @param pageable
     * @param authentication
     * @return Page<Event>
     */
    @GetMapping("/search")
    @PreAuthorize("!hasAuthority('USER')")
    public Page<User> search(@RequestParam String email, Pageable pageable, OAuth2Authentication authentication) {
        /* USER MANAGER/ADMIN Role can find any record */
        return repository.findByEmailContains(email, pageable);
    }

    /*
    @GetMapping("/findByEmail")
    @PreAuthorize("!hasAuthority('USER') || (authentication.principal == #email)")
    User findByEmail(@RequestParam String email, OAuth2Authentication authentication) {
        return repository.findByEmail(email).orElseThrow(() -> new APIException(User.class, "email", email));
    }*/

    /**
     * This method implements the endpoint to fetch an existing user from the database. The @PostAuthorize annotation was
     * added to make sure only ADMIN can fetch any user or user can fetch details for him/her self only.
     * In case the role is USER and the id passed does not match with the one from OAuth server, then the function
     * will be executed but unauthorised error will be sent back to consumer.
     *
     * @param id
     * @return Event
     */
    @GetMapping("/{id}")
    @PostAuthorize("!hasAuthority('USER') || (returnObject != null && returnObject.email == authentication.principal)")
    public User one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new APIException(User.class, " User not found with id ", id.toString()));
    }

    /**
     * This method implements the endpoint to update an existing user in the database. The @PreAuthorize annotation was
     * added to make sure only ADMIN can update any user or user can update only him/her self.
     * In case the role is USER and the id passed does not match with the one from OAuth server, then the function
     * wont be executed and unauthorised error will be sent back to consumer. The newly updated user will be returned
     * back to consumer as JSON object. The @Valid annotation has been used to make sure all the required parameters are
     * provided by consumer.
     *
     * @param id
     * @param user
     */
    @PutMapping("/{id}")
    @PreAuthorize("!hasAuthority('USER') || (authentication.principal == @userRepository.findById(#id).orElse(new com.tracking.users.repository.entity.User()).email)")
    public void update(@PathVariable Long id, @Valid @RequestBody User user) {

        /* Find the existing user by user id */
        User u = repository.findById(id).orElseThrow(() -> new APIException(User.class, " User not found with id ", id.toString()));

        /* Override the  password, events and role */
        user.setPassword(u.getPassword());
        user.setEvents(u.getEvents());
        user.setRole(u.getRole());
        user.setId(id);
        repository.save(user);
    }

    /**
     * This method implements the endpoint to create a new user in the database. The @PreAuthorize annotation was
     * added to make sure only ADMIN can add new user. The @Valid annotation has been used to make sure all the
     * required parameters are provided by consumer.
     *
     * @param user
     * @return Event
     */
    @PostMapping
    @PreAuthorize("!hasAuthority('USER')")
    public User create(@Valid @RequestBody User user) {
        /* Find the existing user by user id */
        if (user.getId() != null) {
            user.setId(null);
        }

        return repository.save(user);
    }

    /**
     * This method implements the endpoint to delete an user from the database. The @PreAuthorize annotation was
     * added to make sure only ADMIN can delete any user or user can delete profile for him/her self.
     * In case the role is USER and the id passed does not match with the one from OAuth server, then the function
     * wont be executed and unauthorised error will be sent back to consumer.
     *
     * @param id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("!hasAuthority('USER') || (authentication.principal == @userRepository.findById(#id).orElse(new com.tracking.users.repository.entity.User()).email)")
    public void delete(@PathVariable Long id) {

        /* Find the existing user by user id */
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new APIException(User.class, " User not found with id ", id.toString());
        }
    }

    /**
     * This method implements the endpoint to change password for any user. The @PreAuthorize annotation was
     * added to make sure only ADMIN can change password for any user or user can change password for him/her self.
     * In case the role is USER and the id passed does not match with the one from OAuth server, then the function
     * wont be executed and unauthorised error will be sent back to consumer.
     *
     * @param id
     * @param oldPassword
     * @param newPassword
     */
    @PutMapping("/{id}/changePassword")
    @PreAuthorize("!hasAuthority('USER') || (#oldPassword != null && !#oldPassword.isEmpty() && authentication.principal == @userRepository.findById(#id).orElse(new com.tracking.users.repository.entity.User()).email)")
    public void changePassword(@PathVariable Long id, @RequestParam(required = false) String oldPassword, @Valid @Size(min = 3) @RequestParam String newPassword) {
        /* Find the existing user by user id */
        User user = repository.findById(id).orElseThrow(() -> new APIException(User.class, "id", id.toString()));
        /* Validate whether the current password matches with the one provided  */
        if (oldPassword == null || oldPassword.isEmpty() || passwordEncoder.matches(oldPassword, user.getPassword())) {

            /* Update the password of the user */
            user.setPassword(passwordEncoder.encode(newPassword));

            /* Save the user with updated password */
            repository.save(user);
        } else {
            /* Throw error if the existing password does not match */
            throw new ConstraintViolationException("old password doesn't match", new HashSet<>());
        }
    }
}
