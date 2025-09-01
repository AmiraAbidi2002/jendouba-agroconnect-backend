package com.jendouba.agroconnect.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import javax.security.auth.Subject;
import java.security.Principal;
/**
 * User entity representing an application user.
 * Implements Principal to integrate with Java security frameworks.
 */
@Entity
@Table(name = "Users")
public class User implements Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long user_id;// Primary key: User ID

    @Column(nullable = false)
    private String user_name;
    @Column(nullable = false,unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @NotEmpty(message = "User type is required")
    private String  user_type;// Type of user (e.g., farmer, buyer)

    @NotEmpty(message = "location is required")
    @Column(nullable = false)
    private String location;

    public User() {
    }

    public User( String user_name, String email, String password, String user_type, String location) {
        this.user_name = user_name;
        this.email = email;
        this.password = password;
        this.user_type = user_type;
        this.location = location;
    }
    // Getters and setters
    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getName() {
        return user_name;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}
