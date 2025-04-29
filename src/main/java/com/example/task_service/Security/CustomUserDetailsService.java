package com.example.task_service.Security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("Username is null or empty");
        }

        // Return a dummy user with ROLE_USER (or assign dynamically if needed)
        return new User(
                username,
                "", // No password required with JWT
                Collections.singleton(new SimpleGrantedAuthority("ROLE_MENTOR"))
        );
    }
}
