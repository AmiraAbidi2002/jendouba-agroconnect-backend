package com.jendouba.agroconnect.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.UserDAO;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.hibernate.UnitOfWork;

import java.util.Optional;
/**
 * JWT Authenticator class.
 * This class verifies the JWT token and returns the corresponding User object if valid.
 */
public class JwtAuthenticator implements Authenticator<String, User> {

    private final UserDAO userDAO;// DAO to fetch users from the database
    private static final String SECRET = "12373291";// Secret key used for verifying JWT

    public JwtAuthenticator(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    @UnitOfWork
    public Optional<User> authenticate(String token) throws AuthenticationException {
        try {
            // Create a JWT verifier with HMAC256 algorithm and the secret key
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();

            // Decode and verify the token
            DecodedJWT decoded = verifier.verify(token);
            System.out.println("decoding JWT: " + decoded.getSubject());

            // Extract user ID from the token subject
            Long userId = Long.valueOf(decoded.getSubject());
            System.out.println("user found: " + userDAO.findById(userId));

            // Return the user from the database
            return userDAO.findById(userId);

        } catch (JWTVerificationException | NumberFormatException e) {
            e.printStackTrace();
            return Optional.empty();// Return empty Optional if token is invalid
        }
    }
}
