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

public class JwtAuthenticator implements Authenticator<String, User> {

    private final UserDAO userDAO;
    private static final String SECRET = "12373291";

    public JwtAuthenticator(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    @UnitOfWork
    public Optional<User> authenticate(String token) throws AuthenticationException {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            DecodedJWT decoded = verifier.verify(token);
            System.out.println("Décodage JWT: " + decoded.getSubject());

            // Conversion correcte vers Long
            Long userId = Long.valueOf(decoded.getSubject());

            System.out.println("Utilisateur trouvé: " + userDAO.findById(userId));

            // findById doit retourner Optional<User>
            return userDAO.findById(userId);

        } catch (JWTVerificationException | NumberFormatException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
