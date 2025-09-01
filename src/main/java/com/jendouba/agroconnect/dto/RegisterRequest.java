package com.jendouba.agroconnect.dto;
/**
        * DTO (Data Transfer Object) for user registration.
        * Contains the data sent by the client during registration.
        */
public class RegisterRequest {

    public String user_name;// Username of the new user
    public String email;
    public String password;// Password (hashed in backend)
    public String user_type;// Type of user (e.g., farmer, buyer)
    public String location;

}
