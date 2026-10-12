package com.campusfind.service;

import com.campusfind.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRegistryService {

    private final Map<String, User> tokenMap = new ConcurrentHashMap<>();

    public String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenMap.put(token, user);
        return token;
    }

    public User getUserByToken(String token) {
        if (token == null) return null;
        return tokenMap.get(token);
    }

    public Authentication getAuthentication(String token) {
        User user = getUserByToken(token);
        if (user == null) return null;

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singletonList(authority)
        );
    }

    public void removeToken(String token) {
        if (token != null) {
            tokenMap.remove(token);
        }
    }
}
