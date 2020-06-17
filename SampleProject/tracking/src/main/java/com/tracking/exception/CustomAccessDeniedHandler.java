package com.tracking.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Used for processing AccessDeniedException as @ControllerAdvice annotation is not sufficient for AccessDeniedException.
 *
 * @version 1.0
 * @dete 05-18-2020
 */

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {


    private final HttpMessageConverter<String> messageConverter;

    private final ObjectMapper mapper;

    public CustomAccessDeniedHandler() {
        this.messageConverter = new StringHttpMessageConverter();
        this.mapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) {

        /* Create an instance of ApiError */
        ApiError apiError = new ApiError(UNAUTHORIZED);
        apiError.setMessage(e.getMessage());
        apiError.setDebugMessage(e.getMessage());

        /* Create new ServerHttpResponse */
        ServerHttpResponse outputMessage = new ServletServerHttpResponse(httpServletResponse);

        /* Set the HTTP Status */
        outputMessage.setStatusCode(HttpStatus.UNAUTHORIZED);

        try {
            /* Write the json error message */
            messageConverter.write(mapper.writeValueAsString(apiError), MediaType.APPLICATION_JSON, outputMessage);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            throw e;
        }
    }
}