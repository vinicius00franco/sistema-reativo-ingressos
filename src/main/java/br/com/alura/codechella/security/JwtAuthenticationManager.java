package br.com.alura.codechella.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationManager(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        try {
            String email = jwtUtil.validateAndGetEmail(token);
            // Single role USER for simplicity
            return Mono.just(new UsernamePasswordAuthenticationToken(email, token,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}