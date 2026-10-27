package com.splitmate.security;

import com.splitmate.repo.UserRepository;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {
    private JwtService jwt;
    private UserRepository users;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        jwt = mock(JwtService.class);
        users = mock(UserRepository.class);
        filter = new JwtAuthFilter(jwt, users);
    }

    @Test
    void invalidTokenContinuesAsUnauthenticatedWithoutLookingUpAUser() throws Exception {
        when(jwt.subject("broken-token")).thenThrow(new JwtException("invalid token"));
        MockHttpServletRequest request = request("Bearer broken-token");

        assertDoesNotThrow(() -> filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain()));
        verifyNoInteractions(users);
    }

    @Test
    void databaseFailureIsNotMisreportedAsAnInvalidToken() {
        when(jwt.subject("valid-token")).thenReturn("person@example.com");
        when(users.findByEmailIgnoreCase("person@example.com"))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> filter.doFilter(request("Bearer valid-token"), new MockHttpServletResponse(), new MockFilterChain()));
    }

    private MockHttpServletRequest request(String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", authorization);
        return request;
    }
}
