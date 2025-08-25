package com.jendouba.agroconnect.auth;

import com.jendouba.agroconnect.core.User;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthenticationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Optional;

@Provider
public class JwtAuthFilter extends AuthFilter<String, User> {

    public static class Builder<U> extends AuthFilterBuilder<String, User, JwtAuthFilter> {
        @Override
        protected JwtAuthFilter newInstance() {
            return new JwtAuthFilter();
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            Optional<User> user ;
            try {
                user = authenticator.authenticate(token);
            } catch (AuthenticationException e) {
                throw new RuntimeException(e);
            }
            if (user.isPresent()) {
                requestContext.setSecurityContext(new UserSecurityContext(user.get()));
            } else {
                throw new jakarta.ws.rs.NotAuthorizedException("Bearer");
            }
        } else {
            throw new jakarta.ws.rs.NotAuthorizedException("Bearer");
        }
    }
}

