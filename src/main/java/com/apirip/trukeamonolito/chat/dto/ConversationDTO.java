package com.apirip.trukeamonolito.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para mostrar conversaciones en la lista
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {
    private Integer conversationId;
    private Integer otherStudentId;
    private String otherStudentName;
    private String otherStudentPhoto;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
    private boolean isOnline;
}
