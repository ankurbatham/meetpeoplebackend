package com.meetThePeople.config;

import com.meetThePeople.entity.User;
import com.meetThePeople.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Custom UserDetailsService implementation for Spring Security
 * This service loads user details for authentication
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {
        try {
            log.debug("Loading user details for mobile: {}", mobile);
            
            User user = userService.findByMobile(mobile)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with mobile: " + mobile));
            
            log.debug("User found: {} with ID: {}", user.getName(), user.getId());
            
            // Create UserDetails object
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getMobile())
                    .password("") // No password for JWT-based auth
                    .authorities(new ArrayList<>()) // No specific authorities for now
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
                    
        } catch (UsernameNotFoundException e) {
            log.warn("User not found for mobile: {}", mobile);
            throw e;
        } catch (Exception e) {
            log.error("Error loading user details for mobile: {}", mobile, e);
            throw new UsernameNotFoundException("Error loading user details", e);
        }
    }
} 