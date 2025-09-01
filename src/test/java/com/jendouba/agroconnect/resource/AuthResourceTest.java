package com.jendouba.agroconnect.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.UserDAO;
import com.jendouba.agroconnect.dto.RegisterRequest;
import com.jendouba.agroconnect.resources.AuthResource;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;
import java.util.Optional;

public class AuthResourceTest {

    @Test
    void register_success() {
        UserDAO userDAO = mock(UserDAO.class);
        AuthResource auth = new AuthResource(userDAO);

        RegisterRequest req = new RegisterRequest();
        req.user_name = "Amira";
        req.email = "amira@test.com";
        req.password = "password123";
        req.user_type = "FARMER";
        req.location = "Jendouba";

        User saved = new User("Amira", "amira@test.com",
                BCrypt.hashpw("password123", BCrypt.gensalt()),
                "FARMER", "Jendouba");
        saved.setUser_id(1L);

        when(userDAO.create(any(User.class))).thenReturn(saved);

        Response result = auth.register(req);

        assertEquals(201, result.getStatus());
        Map<?, ?> entity = (Map<?, ?>) result.getEntity();
        assertEquals(1L, entity.get("id"));
    }

    @Test
    void register_missing_field() {
        UserDAO userDAO = mock(UserDAO.class);
        AuthResource auth = new AuthResource(userDAO);

        RegisterRequest req = new RegisterRequest(); // empty → missing email, password, etc.
        Response result = auth.register(req);

        assertEquals(400, result.getStatus());
        assertTrue(result.getEntity().toString().contains("Missing required fields"));
    }

    @Test
    void login_success() {
        UserDAO userDAO = mock(UserDAO.class);
        AuthResource auth = new AuthResource(userDAO);

        String hashed = BCrypt.hashpw("password123", BCrypt.gensalt());
        User u = new User("Amira", "amira@test.com", hashed, "FARMER", "Jendouba");

        when(userDAO.findByEmail("amira@test.com")).thenReturn(Optional.of(u));

        User creds = new User();
        creds.setEmail("amira@test.com");
        creds.setPassword("password123");

        Response result = auth.login(creds);

        assertEquals(200, result.getStatus());
        assertTrue(result.getEntity().toString().contains("token"));
    }

    @Test
    void login_invalid_password() {
        UserDAO userDAO = mock(UserDAO.class);
        AuthResource auth = new AuthResource(userDAO);

        String hashed = BCrypt.hashpw("correctPass", BCrypt.gensalt());
        User u = new User("X", "x@test.com", hashed, "BUYER", "Ghardimaou");

        when(userDAO.findByEmail("x@test.com")).thenReturn(Optional.of(u));

        User creds = new User();
        creds.setEmail("x@test.com");
        creds.setPassword("wrongPass");

        Response result = auth.login(creds);

        assertEquals(401, result.getStatus());
        assertTrue(result.getEntity().toString().contains("Invalid email or password"));
    }

    @Test
    void login_user_not_found() {
        UserDAO userDAO = mock(UserDAO.class);
        AuthResource auth = new AuthResource(userDAO);

        when(userDAO.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        User creds = new User();
        creds.setEmail("unknown@test.com");
        creds.setPassword("pass");

        Response result = auth.login(creds);

        assertEquals(401, result.getStatus());
        assertTrue(result.getEntity().toString().contains("Invalid email or password"));
    }
}
