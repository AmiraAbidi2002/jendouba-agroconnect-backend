package com.jendouba.agroconnect.db;

import com.jendouba.agroconnect.core.Message;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class MessageDAO extends AbstractDAO<Message> {

    public MessageDAO( SessionFactory sessionFactory1) {
        super(sessionFactory1);

    }
    //Save Mssg
    public Message save(Message message){
        return persist(message);
    }

    public List<Message> findByUsers(Long user1, Long user2){
        return currentSession()
                .createQuery("FROM Message m WHERE (m.sender.user_id = :u1 AND m.receiver.user_id = :u2)"
                        +"OR (m.sender.user_id = :u2 AND m.receiver.user_id = :u1) ORDER BY m.timestamp ASC",Message.class)
                .setParameter("u1",user1)
                .setParameter("u2",user2)
                .list();
    }

}
