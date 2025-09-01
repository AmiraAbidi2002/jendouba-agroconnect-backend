package com.jendouba.agroconnect.resources;

import com.jendouba.agroconnect.core.Message;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.MessageDAO;
import com.jendouba.agroconnect.db.UserDAO;
import com.jendouba.agroconnect.dto.MessageDTO;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Resource for messaging functionality between users.
 * Supports sending messages, fetching messages, and listing conversations.
 */
@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageResource {

    private final MessageDAO messageDAO;
    private final UserDAO userDAO;

    public MessageResource(MessageDAO messageDAO, UserDAO userDAO) {
        this.messageDAO = messageDAO;
        this.userDAO = userDAO;
    }

    /**
     * Input DTO for sending a message.
     */
    public static class MessageInput {
        public Long senderId;
        public Long receiverId;
        public String content;
    }

    /**
     * Send a message from sender to receiver.
     */

    @POST
    @UnitOfWork
    public Response sendMessage(MessageInput input) {
        User sender = userDAO.findById(input.senderId)
                .orElseThrow(() -> new WebApplicationException("Sender not found", 404));
        User receiver = userDAO.findById(input.receiverId)
                .orElseThrow(() -> new WebApplicationException("Receiver not found", 404));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(input.content);

        Message saved = messageDAO.save(message);
        return Response.ok(MessageDTO.fromEntity(saved)).build();
    }


    /**
     * Fetch messages between two users, ordered by timestamp ascending.
     */
    @GET
    @UnitOfWork
    @Path("/{userId}")
    public Response getMessages(@PathParam("userId") Long userId,
                                @QueryParam("with") Long otherUserId) {
        List<Message> messages = messageDAO.findByUsers(userId, otherUserId);
        List<MessageDTO> dto = messages.stream()
                .map(MessageDTO::fromEntity)
                .toList();
        return Response.ok(dto).build();
    }

    /**
     * 🔹  List all existing conversations for a user.
     * Each conversation includes the other user's info and the last message.
     *
     */
    @GET
    @UnitOfWork
    @Path("/conversations/{userId}")
    public Response getConversations(@PathParam("userId") Long userId) {
        List<Object[]> results = messageDAO.findConversationsSafe(userId);
        if (results == null) results = List.of();

        List<ConversationDTO> conversations = results.stream()
                .map(r -> {
                    if (r == null || r.length == 0 || !(r[0] instanceof User)) return null;
            User other = (User) r[0];
            // retrieve the last correct message
                    Message last = (Message) r[1];
            String lastMsg = last != null ? last.getContent() : "";
            return new ConversationDTO(other.getUser_id(), other.getUser_name(), lastMsg);
        }).filter(c -> c != null)
                .collect(Collectors.toList());

        return Response.ok(conversations).build();
    }

    /**
     * DTO representing a conversation summary.
     */
    public static class ConversationDTO {
        public Long id;
        public String user_name;
        public String lastMessage;

        public ConversationDTO(Long id, String user_name, String lastMessage) {
            this.id = id;
            this.user_name = user_name;
            this.lastMessage = lastMessage;
        }
    }
}
