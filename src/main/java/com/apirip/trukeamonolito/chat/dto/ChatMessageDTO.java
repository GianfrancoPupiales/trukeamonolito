package com.apirip.trukeamonolito.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para transferir mensajes de chat vía WebSocket
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Integer messageId;
    private Integer conversationId;
    private Integer senderId;
    private String senderName;
    private String senderPhoto;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;
    private MessageType type;

    public enum MessageType {
        CHAT,       // Mensaje de chat normal
        JOIN,       // Usuario se une al chat
        LEAVE,      // Usuario sale del chat
        TYPING      // Usuario está escribiendo
    }
}
