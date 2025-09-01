package com.jendouba.agroconnect.auth;

import com.jendouba.agroconnect.core.User;
import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;
/**
 * Security context implementation for authenticated users.
 * Stores the authenticated user and security info of the request.
 */
public class UserSecurityContext implements SecurityContext {

    private final User user;
    private final boolean secure;

    public UserSecurityContext(User user, boolean secure) {
        this.user = user;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return user;
    }// Return the authenticated user as Principal

    @Override
    public boolean isUserInRole(String role) {
        return role != null && role.equalsIgnoreCase(user.getUser_type());// Check if user role matches the required role
    }

    @Override
    public boolean isSecure() {
        return secure; } // Return whether the connection is secure (HTTPS)

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }// JWT Bearer authentication scheme

}
