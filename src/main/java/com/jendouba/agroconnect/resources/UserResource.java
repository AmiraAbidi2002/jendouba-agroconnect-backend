package com.jendouba.agroconnect.resources;

import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.UserDAO;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private final UserDAO userDAO;

    public UserResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @GET
    @UnitOfWork
    public List<Map<String, Object>> getUsersByType(@QueryParam("type") String type) {
        List<User> users = userDAO.findByType(type);
        return users.stream().map(u -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", u.getUser_id());
            userMap.put("user_name", u.getUser_name());
            userMap.put("role", u.getUser_type());
            return userMap;
        }).collect(Collectors.toList());
    }

}

