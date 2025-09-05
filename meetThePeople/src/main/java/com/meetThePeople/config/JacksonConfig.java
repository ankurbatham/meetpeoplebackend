package com.meetThePeople.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson configuration to handle JSON serialization issues
 * This configuration helps prevent circular reference errors and provides better serialization control
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JavaTimeModule for proper LocalDateTime handling
        mapper.registerModule(new JavaTimeModule());
        
        // Disable features that can cause issues
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Enable features for better serialization
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
} 