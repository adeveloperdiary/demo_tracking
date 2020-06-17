package com.tracking.events.repository.entity;


import com.tracking.users.repository.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Event Entity Class.
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;

    private Double distance;
    private Double duration;

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;

    private String weatherCondition;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @ToString.Exclude
    private User user;

}
