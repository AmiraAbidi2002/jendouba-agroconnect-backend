package com.jendouba.agroconnect.auth;

import com.jendouba.agroconnect.core.User;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthenticationException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT Authentication Filter.
 * This class intercepts HTTP requests and validates the JWT token from the Authorization header.
 * If the token is valid, it sets the SecurityContext with the authenticated user.
 */
@Provider
public class JwtAuthFilter extends AuthFilter<String, User> {

    /**
     * Builder class to create an instance of JwtAuthFilter.
     * Follows Dropwizard's AuthFilterBuilder pattern.
     */
    public static class Builder<U> extends AuthFilterBuilder<String, User, JwtAuthFilter> {
        @Override
        protected JwtAuthFilter newInstance() {
            return new JwtAuthFilter();
        }
    }

    /**
     * Filters incoming HTTP requests to validate the JWT token.
     * @param requestContext HTTP request context
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Retrieve Authorization header
        String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // If the header is missing or does not start with "Bearer ", reject the request
        if (header == null || !header.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Bearer");
        }

        if (header != null && header.startsWith("Bearer ")) {
            // Extract JWT token from the header
            String token = header.substring("Bearer ".length());
            Optional<User> user;
            try {
                // Authenticate the user using the token
                user = authenticator.authenticate(token);
            } catch (AuthenticationException e) {
                // If authentication fails, respond with 401 Unauthorized
                throw new NotAuthorizedException("Bearer");
            }

            // If user is not found, respond with 401 Unauthorized
            if (user.isEmpty()) {
                throw new NotAuthorizedException("Bearer");
            }

            // Determine if the request is secure (HTTPS)
            boolean secure = requestContext.getSecurityContext() != null &&
                    requestContext.getSecurityContext().isSecure();

            // Set the SecurityContext with the authenticated user
            SecurityContext sc = new UserSecurityContext(user.get(), secure);
            requestContext.setSecurityContext(sc);
        }
    }
}
