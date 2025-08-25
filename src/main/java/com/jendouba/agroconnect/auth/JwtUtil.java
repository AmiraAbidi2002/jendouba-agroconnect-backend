package com.jendouba.agroconnect.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jendouba.agroconnect.core.User;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET ="12373291";
    public static String generateToken(User user){
        return JWT.create()
                .withSubject(String.valueOf(user.getUser_id()))
                .withClaim("user_name",user.getUser_name())
                .withClaim("email",user.getEmail())
                .withClaim("user_type",user.getUser_type())
                .withIssuedAt(new Date())
                .sign(Algorithm.HMAC256(SECRET));
    }





}
