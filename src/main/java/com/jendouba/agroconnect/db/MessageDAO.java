package com.jendouba.agroconnect.db;

import com.jendouba.agroconnect.core.Message;
import com.jendouba.agroconnect.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * Data Access Object for Message entity.
 * Provides methods to save, retrieve, and query messages in the database.
 */
public class MessageDAO extends AbstractDAO<Message> {

    public MessageDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    //Saves a message to the database.
    public Message save(Message message) {
        return persist(message);
    }


    //Retrieves all messages involving a specific user (sender or receiver),
    //   ordered by timestamp descending (most recent first).
    public List<Message> findByUserId(Long userId) {
        return currentSession()
                .createQuery(
                        "FROM Message m " +
                                "WHERE m.sender.user_id = :userId OR m.receiver.user_id = :userId " +
                                "ORDER BY m.timestamp DESC",
                        Message.class
                )
                .setParameter("userId", userId)
                .list();
    }

    //Retrieves all messages exchanged between two specific users,
    //     * ordered by timestamp ascending (chronological order).
    public List<Message> findByUsers(Long user1, Long user2) {
        return currentSession()
                .createQuery(
                        "SELECT m FROM Message m " +
                                "JOIN FETCH m.sender s " +
                                "JOIN FETCH m.receiver r " +
                                "WHERE (s.user_id = :u1 AND r.user_id = :u2) " +
                                "   OR (s.user_id = :u2 AND r.user_id = :u1) " +
                                "ORDER BY m.timestamp ASC",
                        Message.class
                )
                .setParameter("u1", user1)
                .setParameter("u2", user2)
                .list();
    }

    /**
     Returns the list of users a specific user has exchanged messages with,
     * along with the latest message for each conversation.     */
    public List<Object[]> findConversationsSafe(Long userId) {
        List<Message> messages = currentSession()
                .createQuery(
                        "FROM Message m " +
                                "WHERE m.sender.user_id = :uid OR m.receiver.user_id = :uid " +
                                "ORDER BY m.timestamp DESC",
                        Message.class
                )
                .setParameter("uid", userId)
                .list();

        Map<Long, Object[]> conversationMap = new LinkedHashMap<>();
        for (Message m : messages) {
            // Determine the other participant in the conversation
            User other = m.getSender().getUser_id().equals(userId) ? m.getReceiver() : m.getSender();
            if (!conversationMap.containsKey(other.getUser_id())) {
                conversationMap.put(other.getUser_id(), new Object[]{other, m});
            }
        }

        return new ArrayList<>(conversationMap.values());
    }

    /**
     * 🔹 Retrieves the last message exchanged between two users.
     */
    public Message findLastMessage(Long userId, Long otherId) {
        return currentSession()
                .createQuery(
                        "FROM Message m " +
                                "WHERE (m.sender.user_id = :u1 AND m.receiver.user_id = :u2) " +
                                "   OR (m.sender.user_id = :u2 AND m.receiver.user_id = :u1) " +
                                "ORDER BY m.timestamp DESC",
                        Message.class
                )
                .setParameter("u1", userId)
                .setParameter("u2", otherId)
                .setMaxResults(1)
                .uniqueResult();
    }
}
