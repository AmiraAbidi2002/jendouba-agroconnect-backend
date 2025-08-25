package com.jendouba.agroconnect.auth;

import com.jendouba.agroconnect.core.User;
import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;

public class UserSecurityContext implements SecurityContext {

    private final User user;

    public UserSecurityContext(User user) {
        this.user = user;
    }

    @Override
    public Principal getUserPrincipal() {
        return user::getUser_name;
    }

    @Override
    public boolean isUserInRole(String role) {
        return user.getUser_type() != null && user.getUser_type().equalsIgnoreCase(role);
    }

    @Override
    public boolean isSecure() {
        return false; }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }

}
