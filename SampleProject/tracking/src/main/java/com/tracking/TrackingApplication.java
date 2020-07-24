package com.tracking;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot Application Entry Point
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@EnableEurekaClient
@SpringBootApplication
@ComponentScan(basePackages = "com.tracking.*")
public class TrackingApplication {

    Logger logger = LoggerFactory.getLogger(TrackingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TrackingApplication.class, args);
    }
}
