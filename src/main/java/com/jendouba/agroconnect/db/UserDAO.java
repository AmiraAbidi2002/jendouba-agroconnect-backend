package com.jendouba.agroconnect.db;

import com.jendouba.agroconnect.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.ws.rs.core.Response;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractDAO<User> {
    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
    public User create(User user){
        if(user.getUser_name() == null || user.getEmail() == null ||
                user.getPassword() == null || user.getUser_type() == null ||
                user.getLocation() == null) {
            throw new IllegalArgumentException("All fields are required");
        }
        return persist(user);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable((currentSession().get(User.class,id)));
    }
    public Optional<User> findByEmail(String email){
        return currentSession().
                createQuery("FROM User WHERE email= :email", User.class)
                .setParameter("email",email)
                .uniqueResultOptional();
    }

    public List<User> findByType(String type) {
        return currentSession()
                .createQuery("SELECT u FROM User u WHERE u.user_type = :type", User.class)
                .setParameter("type",type)
                .list();

    }

    public List<User> findAll() {
        return currentSession()
                .createQuery("FROM User", User.class)
                .list();
    }


}
