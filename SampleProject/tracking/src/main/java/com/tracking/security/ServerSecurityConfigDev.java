package com.tracking.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
/**
 * Default implementation class for ServerSecurityConfigDev.
 *
 * @version 1.0
 * @dete 05-18-2020
 **/

@Configuration
@Profile("dev")
@Order(1)
public class ServerSecurityConfigDev extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/api/console/**").headers().frameOptions().sameOrigin().and()
                .cors().and()
                .csrf().disable();


    }
}
