package com.splitmate.security;

import com.splitmate.domain.User;
import com.splitmate.repo.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwt;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String email;
            try {
                email = jwt.subject(authorization.substring(7));
            } catch (JwtException | IllegalArgumentException invalidToken) {
                log.warn("Rejected bearer token for {}: {}", request.getRequestURI(),
                        invalidToken.getClass().getSimpleName());
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            User user = users.findByEmailIgnoreCase(email).orElse(null);
            if (user == null) {
                log.warn("JWT subject has no matching account for {}", request.getRequestURI());
                SecurityContextHolder.clearContext();
            } else {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user.email, null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            }
        }
        chain.doFilter(request, response);
    }
}
