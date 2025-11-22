package com.apirip.trukeamonolito.chat.repo;

import com.apirip.trukeamonolito.chat.domain.ChatMessage;
import com.apirip.trukeamonolito.chat.domain.Conversation;
import com.apirip.trukeamonolito.student.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    /**
     * Encuentra todos los mensajes de una conversación ordenados por fecha
     */
    List<ChatMessage> findByConversationOrderBySentAtAsc(Conversation conversation);

    /**
     * Cuenta mensajes no leídos en una conversación para un receptor específico
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.conversation = :conversation " +
            "AND m.sender != :receiver " +
            "AND m.isRead = false")
    long countUnreadMessages(
            @Param("conversation") Conversation conversation,
            @Param("receiver") Student receiver
    );

    /**
     * Marca todos los mensajes de una conversación como leídos para un receptor
     */
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
            "WHERE m.conversation = :conversation " +
            "AND m.sender != :receiver " +
            "AND m.isRead = false")
    int markAllAsRead(
            @Param("conversation") Conversation conversation,
            @Param("receiver") Student receiver
    );

    /**
     * Obtiene el último mensaje de una conversación
     */
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.conversation = :conversation " +
            "ORDER BY m.sentAt DESC " +
            "LIMIT 1")
    ChatMessage findLastMessageByConversation(@Param("conversation") Conversation conversation);
}
