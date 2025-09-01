package com.jendouba.agroconnect.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jendouba.agroconnect.core.User;

import java.util.Date;
/**
 * Utility class for JWT operations.
 * Handles JWT creation and signing.
 */
public class JwtUtil {
    // Secret key used for signing JWTs (should be kept safe)
    private static final String SECRET ="12373291";
    //Generates a JWT token for the given user.
    public static String generateToken(User user){
        return JWT.create()
                .withSubject(String.valueOf(user.getUser_id())) // user ID as subject
                .withClaim("user_name",user.getUser_name()) // add username claim
                .withClaim("email",user.getEmail())// add email claim
                .withClaim("user_type",user.getUser_type())// add user_type claim
                .withIssuedAt(new Date())// set issued at timestamp
                .sign(Algorithm.HMAC256(SECRET));// sign token with HMAC256
    }





}
