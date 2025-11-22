package com.apirip.trukeamonolito.chat.domain;

import com.apirip.trukeamonolito.offer.domain.Offer;
import com.apirip.trukeamonolito.student.domain.Student;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversation",
        indexes = {
                @Index(name = "idx_conv_student1", columnList = "student1_id"),
                @Index(name = "idx_conv_student2", columnList = "student2_id"),
                @Index(name = "idx_conv_offer", columnList = "offer_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConversation")
    private Integer idConversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student1_id", foreignKey = @ForeignKey(name = "fk_conv_student1"))
    private Student student1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student2_id", foreignKey = @ForeignKey(name = "fk_conv_student2"))
    private Student student2;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "conversation_offer",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "offer_id")
    )
    private List<Offer> offers = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastMessageAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sentAt ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastMessageAt = LocalDateTime.now();
    }

    /**
     * Verifica si un estudiante participa en esta conversación
     */
    public boolean hasParticipant(Student student) {
        return student1.getIdStudent() == student.getIdStudent()
            || student2.getIdStudent() == student.getIdStudent();
    }

    /**
     * Obtiene el otro participante de la conversación
     */
    public Student getOtherParticipant(Student currentStudent) {
        if (student1.getIdStudent() == currentStudent.getIdStudent()) {
            return student2;
        }
        return student1;
    }

    /**
     * Actualiza el timestamp del último mensaje
     */
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    /**
     * Añade una oferta a la conversación si no existe ya
     */
    public void addOffer(Offer offer) {
        if (this.offers == null) {
            this.offers = new ArrayList<>();
        }
        if (!this.offers.contains(offer)) {
            this.offers.add(offer);
        }
    }
}
