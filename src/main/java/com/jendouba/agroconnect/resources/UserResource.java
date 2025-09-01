package com.jendouba.agroconnect.resources;

import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.UserDAO;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resource to manage users.
 * Supports fetching users by type, fetching single user, updating users, and searching users.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    private final UserDAO userDAO;

    public UserResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    /**
     * GET /users?type={type}
     * Fetch all users filtered by type (e.g., FARMER, BUYER).
     * Returns a simplified user map with id, username, role, and location.
     */
    @GET
    @UnitOfWork
    public List<Map<String, Object>> getUsersByType(@QueryParam("type") String type) {
        List<User> users = userDAO.findByType(type);
        return users.stream().map(u -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", u.getUser_id());
            userMap.put("user_name", u.getUser_name());
            userMap.put("role", u.getUser_type());
            userMap.put("location",u.getLocation());
            return userMap;
        }).collect(Collectors.toList());
    }

    /**
     * GET /users/{id}
     * Fetch a single user by id.
     */
    @GET
    @Path("/{id}")
    @UnitOfWork
    public Response getUser(@PathParam("id") Long id) {
        Optional<User> user = userDAO.findById(id);
        if (user.isPresent()) return Response.ok(user.get()).build();
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message","User not found")).build();
    }


    /* PUT /users/{id}
     * Update a user by id. Only fields provided in the request are updated.
            */
    @PUT
    @Path("/{id}")
    @UnitOfWork
    public Response updateUser(@PathParam("id") Long id, User updated) {
        Optional<User> userOpt = userDAO.findById(id);
        if (userOpt.isEmpty())
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message","User not found")).build();

        User user = userOpt.get();
        if (updated.getUser_name() != null) user.setUser_name(updated.getUser_name());
        if (updated.getEmail() != null) user.setEmail(updated.getEmail());
        if (updated.getLocation() != null) user.setLocation(updated.getLocation());
        if (updated.getPassword() != null) user.setPassword(updated.getPassword());
        User saved = userDAO.update(user); // persist/update
        return Response.ok(saved).build();
    }

    /**
     * 🔍* GET /users/search?query={query}
     *      * Search users by id or name. Example: /users/search?query=ali or /users/search?query=12
     */

    @GET
    @Path("/search")
    @UnitOfWork
    public Response search(@QueryParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new WebApplicationException("Missing search query", 400);
        }

        List<User> results = userDAO.searchUsers(query);
        return Response.ok(results).build();
    }
}


