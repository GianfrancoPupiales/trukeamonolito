package com.apirip.trukeamonolito.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket con STOMP para mensajería en tiempo real
 *
 * MODOS DE OPERACIÓN:
 * - DESARROLLO (websocket.mode=simple): Usa SimpleBroker en memoria (no clustering)
 * - PRODUCCIÓN (websocket.mode=relay): Usa RabbitMQ como broker externo (soporta clustering)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.mode:simple}")
    private String websocketMode;

    @Value("${rabbitmq.host:localhost}")
    private String rabbitHost;

    @Value("${rabbitmq.stomp.port:61613}")
    private int rabbitStompPort;

    @Value("${rabbitmq.username:guest}")
    private String rabbitUsername;

    @Value("${rabbitmq.password:guest}")
    private String rabbitPassword;

    /**
     * Configura el broker de mensajes
     * - SimpleBroker: Para desarrollo local (una sola instancia)
     * - StompBrokerRelay: Para producción con clustering (múltiples instancias compartiendo RabbitMQ)
     *
     * Destinos:
     * - /topic: para broadcasting (notificaciones generales)
     * - /queue: para mensajes punto a punto (chat privado)
     * - /user: para mensajes dirigidos a usuarios específicos
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        if ("relay".equalsIgnoreCase(websocketMode)) {
            // Modo PRODUCCIÓN: RabbitMQ como broker externo (soporta clustering)
            System.out.println("=== WEBSOCKET CONFIG: Modo RELAY (RabbitMQ) - Clustering HABILITADO ===");
            config.enableStompBrokerRelay("/topic", "/queue", "/user")
                    .setRelayHost(rabbitHost)
                    .setRelayPort(rabbitStompPort)
                    .setClientLogin(rabbitUsername)
                    .setClientPasscode(rabbitPassword)
                    .setSystemLogin(rabbitUsername)
                    .setSystemPasscode(rabbitPassword);
        } else {
            // Modo DESARROLLO: SimpleBroker en memoria (no soporta clustering)
            System.out.println("=== WEBSOCKET CONFIG: Modo SIMPLE (In-Memory) - Clustering DESHABILITADO ===");
            System.out.println("=== ADVERTENCIA: El chat solo funcionará dentro de la misma instancia ===");
            config.enableSimpleBroker("/topic", "/queue", "/user");
        }

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
