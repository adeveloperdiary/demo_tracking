package com.tracking.security;

import com.tracking.exception.CustomAccessDeniedHandler;
import com.tracking.exception.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
/**
 * Default implementation class for ResourceServerConfiguration. The rules are defined in configure() method.
 *
 * @version 1.0
 * @dete 05-18-2020
 **/
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public ResourceServerConfiguration(CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId("api");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .antMatcher("/api/**")
                .authorizeRequests()
                .antMatchers("/api/signin**").permitAll()
                .antMatchers("/api/console/**").permitAll()
                .antMatchers("/api/signin/**").permitAll()
                .antMatchers("/api/event**").hasAnyAuthority("ADMIN", "USER")
                .antMatchers("/api/users**").hasAnyAuthority("ADMIN", "USER_MANAGER", "USER")
                .antMatchers("/api/users/**").hasAnyAuthority("ADMIN", "USER_MANAGER", "USER")
                .antMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint).accessDeniedHandler(new CustomAccessDeniedHandler());
    }


}