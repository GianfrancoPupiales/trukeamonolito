package com.apirip.trukeamonolito.chat.web;

import com.apirip.trukeamonolito.chat.domain.ChatMessage;
import com.apirip.trukeamonolito.chat.domain.Conversation;
import com.apirip.trukeamonolito.chat.dto.ConversationDTO;
import com.apirip.trukeamonolito.chat.service.ChatService;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatWebController {

    private final ChatService chatService;
    private final StudentRepository studentRepository;

    /**
     * Obtiene el estudiante actual desde la autenticación
     */
    private Student getCurrentStudent(Authentication auth) {
        return studentRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Estudiante no encontrado"));
    }

    /**
     * Muestra la interfaz principal de chat
     */
    @GetMapping
    public String chatPage(Authentication auth, Model model) {
        Student currentStudent = getCurrentStudent(auth);

        List<ConversationDTO> conversations = chatService.getConversationsForStudent(currentStudent);
        long unreadCount = chatService.countUnreadConversations(currentStudent);

        model.addAttribute("conversations", conversations);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentStudent", currentStudent);

        return "chat/chat";
    }

    /**
     * Abre una conversación específica
     */
    @GetMapping("/conversation/{conversationId}")
    public String openConversation(
            @PathVariable Integer conversationId,
            Authentication auth,
            Model model) {

        Student currentStudent = getCurrentStudent(auth);

        Conversation conversation = chatService.getConversationById(conversationId, currentStudent);
        List<ChatMessage> messages = chatService.getMessages(conversation);
        Student otherStudent = conversation.getOtherParticipant(currentStudent);

        // Marca mensajes como leídos
        chatService.markMessagesAsRead(conversation, currentStudent);

        // Obtiene todas las conversaciones para la lista lateral
        List<ConversationDTO> conversations = chatService.getConversationsForStudent(currentStudent);

        model.addAttribute("conversation", conversation);
        model.addAttribute("messages", messages);
        model.addAttribute("otherStudent", otherStudent);
        model.addAttribute("conversations", conversations);
        model.addAttribute("currentStudent", currentStudent);

        return "chat/chat";
    }

}
