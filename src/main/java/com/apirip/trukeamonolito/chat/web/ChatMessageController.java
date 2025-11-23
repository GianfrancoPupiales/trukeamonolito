package com.apirip.trukeamonolito.chat.web;

import com.apirip.trukeamonolito.chat.domain.ChatMessage;
import com.apirip.trukeamonolito.chat.domain.Conversation;
import com.apirip.trukeamonolito.chat.dto.ChatMessageDTO;
import com.apirip.trukeamonolito.chat.service.ChatService;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Controlador WebSocket para mensajería en tiempo real
 */
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;
    private final StudentRepository studentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Maneja el envío de mensajes de chat
     * Los clientes envían mensajes a: /app/chat/{conversationId}
     * Los mensajes se distribuyen a: /topic/conversation/{conversationId}
     */
    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(
            @DestinationVariable Integer conversationId,
            @Payload ChatMessageDTO messageDTO,
            Principal principal) {

        try {
            // Obtiene el estudiante actual desde el email autenticado
            Student sender = studentRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

            // Valida y obtiene la conversación
            Conversation conversation = chatService.getConversationById(conversationId, sender);

            // Guarda el mensaje en la base de datos
            ChatMessage savedMessage = chatService.sendMessage(conversation, sender, messageDTO.getContent());

            // Convierte a DTO para enviar por WebSocket
            ChatMessageDTO responseDTO = chatService.toDTO(savedMessage);

            // Envía el mensaje a todos los suscritos al topic de esta conversación
            // Formato para RabbitMQ STOMP: /topic/conversation.{id}
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + conversationId,
                    responseDTO
            );

            // Nota: Notificaciones de usuario deshabilitadas para simplificar con RabbitMQ
            // Los mensajes se reciben vía el topic compartido

        } catch (Exception e) {
            // Log error y envía mensaje de error al remitente
            ChatMessageDTO errorDTO = ChatMessageDTO.builder()
                    .content("Error al enviar mensaje: " + e.getMessage())
                    .type(ChatMessageDTO.MessageType.CHAT)
                    .build();

            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    errorDTO
            );
        }
    }

    /**
     * Maneja el indicador de "está escribiendo"
     * DESHABILITADO: RabbitMQ STOMP tiene formato diferente para /user destinations
     */
    @MessageMapping("/chat/{conversationId}/typing")
    public void typing(
            @DestinationVariable Integer conversationId,
            Principal principal) {
        // Typing indicator deshabilitado para simplificar integración con RabbitMQ
        // Puede habilitarse usando formato /exchange/amq.topic/typing.{conversationId}
    }
}
