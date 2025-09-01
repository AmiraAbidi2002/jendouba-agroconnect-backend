package com.jendouba.agroconnect.dto;

import com.jendouba.agroconnect.core.Message;
import java.time.LocalDateTime;
/**
 * DTO for Message entity.
 * Used to transfer message data between backend and frontend.
 */
public class MessageDTO {
    private Long msg_id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private LocalDateTime timestamp;

    public MessageDTO() {}

    public MessageDTO(Long msg_id, Long senderId, String senderName,
                      Long receiverId, String content, LocalDateTime timestamp) {
        this.msg_id = msg_id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
    }
/**Converts a Message entity to a MessageDTO.
 * Handles null sender/receiver safely.
 */
    public static MessageDTO fromEntity(Message m) {
        return new MessageDTO(
                m.getMsg_id(),
                m.getSender() != null ? m.getSender().getUser_id() : null,
                m.getSender() != null ? m.getSender().getUser_name() : null,
                m.getReceiver() != null ? m.getReceiver().getUser_id() : null,
                m.getContent(),
                m.getTimestamp()
        );
    }

    // Getters and Setters
    public Long getMsg_id() { return msg_id; }
    public void setMsg_id(Long msg_id) { this.msg_id = msg_id; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
