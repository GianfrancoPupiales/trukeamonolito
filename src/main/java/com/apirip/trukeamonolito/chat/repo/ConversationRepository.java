package com.apirip.trukeamonolito.chat.repo;

import com.apirip.trukeamonolito.chat.domain.Conversation;
import com.apirip.trukeamonolito.offer.domain.Offer;
import com.apirip.trukeamonolito.student.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    /**
     * Encuentra una conversación entre dos estudiantes (en cualquier orden)
     */
    @Query("SELECT c FROM Conversation c " +
            "WHERE (c.student1 = :student1 AND c.student2 = :student2) " +
            "OR (c.student1 = :student2 AND c.student2 = :student1)")
    Optional<Conversation> findByParticipants(
            @Param("student1") Student student1,
            @Param("student2") Student student2
    );

    /**
     * Encuentra todas las conversaciones de un estudiante, ordenadas por último mensaje
     */
    @Query("SELECT c FROM Conversation c " +
            "WHERE c.student1 = :student OR c.student2 = :student " +
            "ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByStudentOrderByLastMessageDesc(@Param("student") Student student);

    /**
     * Cuenta conversaciones con mensajes no leídos para un estudiante
     */
    @Query("SELECT COUNT(DISTINCT c) FROM Conversation c " +
            "JOIN c.messages m " +
            "WHERE (c.student1 = :student OR c.student2 = :student) " +
            "AND m.sender != :student " +
            "AND m.isRead = false")
    long countUnreadConversations(@Param("student") Student student);
}
