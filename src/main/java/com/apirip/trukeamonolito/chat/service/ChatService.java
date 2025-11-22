package com.apirip.trukeamonolito.chat.service;

import com.apirip.trukeamonolito.chat.domain.ChatMessage;
import com.apirip.trukeamonolito.chat.domain.Conversation;
import com.apirip.trukeamonolito.chat.dto.ChatMessageDTO;
import com.apirip.trukeamonolito.chat.dto.ConversationDTO;
import com.apirip.trukeamonolito.chat.repo.ChatMessageRepository;
import com.apirip.trukeamonolito.chat.repo.ConversationRepository;
import com.apirip.trukeamonolito.offer.domain.Offer;
import com.apirip.trukeamonolito.offer.domain.OfferStatus;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final StudentRepository studentRepository;

    /**
     * Crea una conversación a partir de una oferta ACEPTADA
     */
    @Transactional
    public Conversation createConversationFromOffer(Offer offer) {
        if (offer.getStatus() != OfferStatus.ACCEPTED) {
            throw new IllegalStateException("Solo se pueden crear conversaciones de ofertas aceptadas");
        }

        // Verifica si ya existe
        return conversationRepository.findByOffer(offer)
                .orElseGet(() -> {
                    Student student1 = offer.getStudentWhoOffered();
                    Student student2 = offer.getProductToOffer().getStudent();

                    Conversation conversation = Conversation.builder()
                            .student1(student1)
                            .student2(student2)
                            .offer(offer)
                            .build();
                    return conversationRepository.save(conversation);
                });
    }

    /**
     * Obtiene conversación por oferta (si existe)
     */
    @Transactional(readOnly = true)
    public Conversation getConversationByOffer(Offer offer) {
        return conversationRepository.findByOffer(offer).orElse(null);
    }

    /**
     * Envía un mensaje en una conversación
     */
    @Transactional
    public ChatMessage sendMessage(Conversation conversation, Student sender, String content) {
        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build();

        ChatMessage savedMessage = messageRepository.save(message);

        // Actualiza el timestamp de la conversación
        conversation.updateLastMessageTime();
        conversationRepository.save(conversation);

        return savedMessage;
    }

    /**
     * Obtiene todos los mensajes de una conversación
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Conversation conversation) {
        List<ChatMessage> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);
        // Inicializa los proxies lazy de sender para evitar LazyInitializationException
        messages.forEach(msg -> {
            msg.getSender().getName();
            msg.getSender().getPhoto();
        });
        return messages;
    }

    /**
     * Marca todos los mensajes de una conversación como leídos
     */
    @Transactional
    public void markMessagesAsRead(Conversation conversation, Student reader) {
        messageRepository.markAllAsRead(conversation, reader);
    }

    /**
     * Obtiene todas las conversaciones de un estudiante con información resumida
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationsForStudent(Student student) {
        List<Conversation> conversations = conversationRepository.findByStudentOrderByLastMessageDesc(student);

        return conversations.stream()
                .map(conv -> {
                    Student other = conv.getOtherParticipant(student);
                    // Inicializa las propiedades lazy para evitar LazyInitializationException
                    String otherName = other.getName();
                    String otherPhoto = other.getPhoto();
                    Integer otherId = other.getIdStudent();

                    ChatMessage lastMsg = messageRepository.findLastMessageByConversation(conv);
                    long unreadCount = messageRepository.countUnreadMessages(conv, student);

                    return ConversationDTO.builder()
                            .conversationId(conv.getIdConversation())
                            .otherStudentId(otherId)
                            .otherStudentName(otherName)
                            .otherStudentPhoto(otherPhoto)
                            .lastMessage(lastMsg != null ? lastMsg.getContent() : "Sin mensajes")
                            .lastMessageAt(conv.getLastMessageAt())
                            .unreadCount(unreadCount)
                            .isOnline(false) // Implementar lógica de presencia si es necesario
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Convierte un ChatMessage a DTO
     */
    public ChatMessageDTO toDTO(ChatMessage message) {
        Student sender = message.getSender();
        return ChatMessageDTO.builder()
                .messageId(message.getIdMessage())
                .conversationId(message.getConversation().getIdConversation())
                .senderId(sender.getIdStudent())
                .senderName(sender.getName())
                .senderPhoto(sender.getPhoto())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .isRead(message.isRead())
                .type(ChatMessageDTO.MessageType.CHAT)
                .build();
    }

    /**
     * Cuenta conversaciones con mensajes no leídos
     */
    @Transactional(readOnly = true)
    public long countUnreadConversations(Student student) {
        return conversationRepository.countUnreadConversations(student);
    }

    /**
     * Obtiene una conversación por ID y valida que el estudiante participe
     */
    @Transactional(readOnly = true)
    public Conversation getConversationById(Integer conversationId, Student currentStudent) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada"));

        if (!conversation.hasParticipant(currentStudent)) {
            throw new SecurityException("No tienes acceso a esta conversación");
        }

        // Inicializa los proxies lazy para evitar LazyInitializationException
        Student other = conversation.getOtherParticipant(currentStudent);
        // Fuerza la carga de las propiedades del estudiante
        other.getName();
        other.getPhoto();
        other.getEmail();

        return conversation;
    }
}
