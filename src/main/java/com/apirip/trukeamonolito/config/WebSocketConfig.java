package com.apirip.trukeamonolito.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket con STOMP para mensajería en tiempo real
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura el broker de mensajes
     * - /topic: para broadcasting (notificaciones generales)
     * - /queue: para mensajes punto a punto (chat privado)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker de mensajes simple en memoria
        config.enableSimpleBroker("/topic", "/queue");

        // Prefijo para mensajes que van desde el cliente al servidor
        config.setApplicationDestinationPrefixes("/app");

        // Prefijo para mensajes de usuario específico
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Registra los endpoints STOMP
     * Los clientes se conectarán a /ws para establecer la conexión WebSocket
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .withSockJS(); // Habilita SockJS como fallback para navegadores sin WebSocket
    }
}
