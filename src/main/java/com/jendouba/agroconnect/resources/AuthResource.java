package com.jendouba.agroconnect.resources;

import com.jendouba.agroconnect.auth.JwtUtil;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.UserDAO;
import com.jendouba.agroconnect.dto.RegisterRequest;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;
import java.util.Optional;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UserDAO userDAO;

    public AuthResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @POST
    @Path("/register")
    @UnitOfWork
    public Response register(RegisterRequest req) {
        if (req.email == null || req.password == null || req.user_name == null || req.user_type == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Missing required fields"))
                    .build();
        }

        User u = new User();
        u.setUser_name(req.user_name);
        u.setEmail(req.email);

        String hashedPassword = BCrypt.hashpw(req.password, BCrypt.gensalt());
        u.setPassword(hashedPassword);

        u.setUser_type(req.user_type.toUpperCase());
        u.setLocation(req.location);

        System.out.println("Register user received: " + u);

        User saved = userDAO.create(u);

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("id", saved.getUser_id()))
                .build();
    }

    @POST
    @Path("/login")
    @UnitOfWork
    public Response login(User credentials) {
        Optional<User> found = userDAO.findByEmail(credentials.getEmail());

        if (found.isPresent() && BCrypt.checkpw(credentials.getPassword(), found.get().getPassword())) {
            String token = JwtUtil.generateToken(found.get());
            return Response.ok()
                    .entity(Map.of("token",token))
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Invalid email or password"))
                    .build();
        }
    }



    }
