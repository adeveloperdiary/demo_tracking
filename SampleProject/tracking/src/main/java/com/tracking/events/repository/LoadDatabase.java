package com.tracking.events.repository;


import com.tracking.events.repository.entity.Event;
import com.tracking.events.service.EventService;
import com.tracking.users.repository.UserRepository;
import com.tracking.users.repository.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Demo class to load dummy data into the database during server startup.
 *
 * @version 1.0
 * @dete 05-18-2020
 */

@Configuration
@Slf4j
public class LoadDatabase {
    @Value("${data.users:admin,userman,user1,user2,user3,user4,user5}")
    private String[] users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventService business;

    int rnd(int size) {
        return (int) (Math.random() * size);
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public double randLocation(double min, double max) {
        Random rand = new Random();
        return round(rand.nextDouble() * (max - min) + min, 6);
    }

    public LocalDate getRandomDate() throws ParseException {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String start = "2020-01-01";

        long startMillis = format.parse(start).getTime();
        long endMillis = (new Date()).getTime();
        long randomMillisSinceEpoch = ThreadLocalRandom
                .current()
                .nextLong(startMillis, endMillis);

        return LocalDate.parse(format.format(new Date(randomMillisSinceEpoch)));
    }

    @Bean
    CommandLineRunner initUsers(UserRepository repo) {
        return args -> {
            for (int i = 0; i < users.length; i++) {
                String email = users[i] + "@" + users[i] + ".com";
                User.Role role = i > 1 ? User.Role.USER : i == 0 ? User.Role.ADMIN : User.Role.USER_MANAGER;
                String pwd = passwordEncoder.encode("pwd");
                log.info("save {}", repo.save(new User(null, email, pwd, role, null)));
            }
        };
    }

    @Bean
    CommandLineRunner initEvent(EventRepository repo, UserRepository userRepo) {
        return args -> {
            for (int i = 0; i < 200; i++) {
                LocalDate date = LocalDate.ofEpochDay((long) (10_00L + Math.random() * 10_00));
                LocalTime time = LocalTime.ofSecondOfDay((long) (Math.random() * 24 * 3600));
                int u = rnd(users.length);
                double distance = rnd(1000);
                double duration = rnd((int) (distance) * 4);
                User user = userRepo.findByEmail(users[u] + "@" + users[u] + ".com").orElse(userRepo.findAll().iterator().next());
                Event event = new Event(null, getRandomDate(), time, distance, duration, randLocation(-90.0, 90.0), randLocation(-180.0, 180.0), null, LocalDateTime.now(), LocalDateTime.now(), user);
                event.setWeatherCondition(business.fetchWeatherCondition(event));
                log.info("save {}", repo.save(event));
            }
        };
    }
}









