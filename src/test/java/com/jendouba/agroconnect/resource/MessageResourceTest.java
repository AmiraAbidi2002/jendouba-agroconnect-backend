package com.jendouba.agroconnect.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jendouba.agroconnect.core.Message;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.MessageDAO;
import com.jendouba.agroconnect.db.UserDAO;
import com.jendouba.agroconnect.resources.MessageResource;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MessageResourceTest {

    private UserDAO userDAO;
    private MessageDAO messageDAO;
    private MessageResource messageResource;

    private User sender;
    private User receiver;
    private Message message;

    @BeforeEach
    void setup() {
        userDAO = mock(UserDAO.class);
        messageDAO = mock(MessageDAO.class);
        messageResource = new MessageResource(messageDAO, userDAO);

        sender = new User("Alice", "alice@test.com", "pass", "FARMER", "Location1");
        sender.setUser_id(1L);
        receiver = new User("Bob", "bob@test.com", "pass", "BUYER", "Location2");
        receiver.setUser_id(2L);

        message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent("Hello Bob!");
    }

    @Test
    void testSendMessage_success() {
        // Mock user lookup
        when(userDAO.findById(1L)).thenReturn(Optional.of(sender));
        when(userDAO.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageDAO.save(any(Message.class))).thenReturn(message);

        MessageResource.MessageInput input = new MessageResource.MessageInput();
        input.senderId = 1L;
        input.receiverId = 2L;
        input.content = "Hello Bob!";

        Response resp = messageResource.sendMessage(input);
        assertEquals(200, resp.getStatus());
        assertEquals("Hello Bob!", ((com.jendouba.agroconnect.dto.MessageDTO) resp.getEntity()).getContent());
    }

    @Test
    void testSendMessage_senderNotFound() {
        when(userDAO.findById(1L)).thenReturn(Optional.empty());
        MessageResource.MessageInput input = new MessageResource.MessageInput();
        input.senderId = 1L;
        input.receiverId = 2L;
        input.content = "Hello";

        assertThrows(WebApplicationException.class, () -> messageResource.sendMessage(input));
    }

    @Test
    void testGetMessages_success() {
        when(messageDAO.findByUsers(1L, 2L)).thenReturn(Arrays.asList(message));

        Response resp = messageResource.getMessages(1L, 2L);
        assertEquals(200, resp.getStatus());

        List<?> messages = (List<?>) resp.getEntity();
        assertEquals(1, messages.size());
    }

    @Test
    void testGetConversations_success() {
        Object[] conversation = new Object[]{receiver, message};
        List<Object[]> conversations = Arrays.asList(new Object[][]{conversation});
        when(messageDAO.findConversationsSafe(1L)).thenReturn(conversations);

        Response resp = messageResource.getConversations(1L);
        assertEquals(200, resp.getStatus());

        List<?> convList = (List<?>) resp.getEntity();
        assertEquals(1, convList.size());
        MessageResource.ConversationDTO dto = (MessageResource.ConversationDTO) convList.get(0);
        assertEquals("Bob", dto.user_name);
        assertEquals("Hello Bob!", dto.lastMessage);
    }

    @Test
    void testGetConversations_empty() {
        when(messageDAO.findConversationsSafe(1L)).thenReturn(List.of());

        Response resp = messageResource.getConversations(1L);
        assertEquals(200, resp.getStatus());

        List<?> convList = (List<?>) resp.getEntity();
        assertEquals(0, convList.size());
    }
}
