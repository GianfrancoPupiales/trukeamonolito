package com.apirip.trukeamonolito.chat.domain;

import com.apirip.trukeamonolito.student.domain.Student;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message",
        indexes = {
                @Index(name = "idx_msg_conversation", columnList = "conversation_id"),
                @Index(name = "idx_msg_sender", columnList = "sender_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMessage")
    private Integer idMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", foreignKey = @ForeignKey(name = "fk_msg_conversation"))
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", foreignKey = @ForeignKey(name = "fk_msg_sender"))
    private Student sender;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean isRead = false;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    /**
     * Marca el mensaje como leído
     */
    public void markAsRead() {
        this.isRead = true;
    }
}
