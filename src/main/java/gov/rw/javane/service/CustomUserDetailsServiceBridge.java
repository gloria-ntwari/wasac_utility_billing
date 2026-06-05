package gov.rw.javane.service;

import gov.rw.javane.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsServiceBridge {

    private final CustomUserDetailsService customUserDetailsService;

    public UserDetails loadUserByUsername(String username) {
        return customUserDetailsService.loadUserByUsername(username);
    }
}
