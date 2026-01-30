package com.election.voterhierarchy.config;

import com.election.voterhierarchy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationEventListener {

    private final UserService userService;

    // Use @Lazy to break the circular dependency
    public AuthenticationEventListener(@Lazy UserService userService) {
        this.userService = userService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            try {
                userService.updateLastLogin(username);
                log.info("Updated last login for user: {}", username);
            } catch (Exception e) {
                log.error("Failed to update last login for user: {}", username, e);
            }
        }
    }
}