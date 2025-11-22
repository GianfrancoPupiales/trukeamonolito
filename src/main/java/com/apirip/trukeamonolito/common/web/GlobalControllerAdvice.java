package com.apirip.trukeamonolito.common.web;

import com.apirip.trukeamonolito.chat.service.ChatService;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * ControllerAdvice global para agregar atributos comunes a todas las vistas
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ChatService chatService;
    private final StudentRepository studentRepository;

    /**
     * Agrega el contador de conversaciones no leídas a todas las vistas
     * Este atributo estará disponible en todas las plantillas Thymeleaf
     */
    @ModelAttribute("unreadConversationsCount")
    public Long addUnreadConversationsCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Solo calcula si el usuario está autenticado
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return studentRepository.findByEmail(auth.getName())
                    .map(chatService::countUnreadConversations)
                    .orElse(0L);
        }

        return 0L;
    }
}
