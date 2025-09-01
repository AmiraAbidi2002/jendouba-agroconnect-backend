package com.jendouba.agroconnect.db;

import com.jendouba.agroconnect.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User entity.
 * Provides methods to create, update, search, and retrieve users from the database.
 */
public class UserDAO extends AbstractDAO<User> {
    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

   //Persist a new user in the database.
    public User create(User user){
        if(user.getUser_name() == null || user.getEmail() == null ||
                user.getPassword() == null || user.getUser_type() == null ||
                user.getLocation() == null) {
            throw new IllegalArgumentException("All fields are required");
        }
        return persist(user);
    }

    //Update an existing user
    public User update (User user){
        currentSession().merge(user);
        return user;
    }

    //Find user by ID
    public Optional<User> findById(Long id) {
        return Optional.ofNullable((currentSession().get(User.class,id)));
    }

    //Find user by email
    public Optional<User> findByEmail(String email){
        return currentSession().
                createQuery("FROM User WHERE email= :email", User.class)
                .setParameter("email",email)
                .uniqueResultOptional();
    }

    //Find all users of a specific type (e.g., farmer, buyer).
    public List<User> findByType(String type) {
        return currentSession()
                .createQuery("SELECT u FROM User u WHERE u.user_type = :type", User.class)
                .setParameter("type",type)
                .list();

    }

    //Retrieve all users from the database
    public List<User> findAll() {
        return currentSession()
                .createQuery("FROM User", User.class)
                .list();
    }
    /**
     * 🔍 Search users by exact user_id or approximate user_name.
     */
    public List<User> searchUsers(String query) {
        try {
            Long id = Long.parseLong(query); // // If query is a number, search by ID
            return currentSession()
                    .createQuery("FROM User u WHERE u.user_id = :id OR u.user_name LIKE :name", User.class)
                    .setParameter("id", id)
                    .setParameter("name", "%" + query + "%")
                    .list();
        } catch (NumberFormatException e) {
            // Otherwise, search by name only
            return currentSession()
                    .createQuery("FROM User u WHERE u.user_name LIKE :name", User.class)
                    .setParameter("name", "%" + query + "%")
                    .list();
        }
    }

}
