package com.jendouba.agroconnect.resources;

import com.jendouba.agroconnect.core.Message;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.MessageDAO;
import com.jendouba.agroconnect.db.UserDAO;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

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

    // DTO to simplify frontend communication
    public static class MessageInput {
        public Long senderId;
        public Long receiverId;
        public String content;
    }

    @POST
    @UnitOfWork
    public Response sendMessage(MessageInput input) {
        User sender = userDAO.findById( input.senderId)
                .orElseThrow(() -> new WebApplicationException("Sender not found", 404));
        User receiver = userDAO.findById( input.receiverId)
                .orElseThrow(() -> new WebApplicationException("Receiver not found", 404));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(input.content);

        Message saved = messageDAO.save(message);
        return Response.ok(saved).build();
    }





    @GET
    @UnitOfWork
    @Path("/{userId}")
    public Response getMessages(@PathParam("userId") Long userId,
                                @QueryParam("with") Long otherUserId) {
        List<Message> messages = messageDAO.findByUsers(userId, otherUserId);
        return Response.ok(messages).build();
    }
}
