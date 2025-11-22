# Sistema de Chat en Tiempo Real - Documentación

## 🎯 Descripción General

Se ha implementado un sistema de chat en tiempo real tipo WhatsApp Web utilizando WebSockets (STOMP) sobre Spring Boot. El sistema permite comunicación bidireccional instantánea entre estudiantes de la plataforma Trukea.

## ✅ Problema Solucionado

**Error Original:**
```
java.lang.NullPointerException: Cannot invoke "com.apirip.trukeamonolito.auth.session.AuthUser.id()" because "authUser" is null
```

**Causa:**
El proyecto usa `Authentication` de Spring Security con email como identificador, no `@AuthenticationPrincipal AuthUser`.

**Solución:**
Cambio de:
```java
@GetMapping
public String chatPage(@AuthenticationPrincipal AuthUser authUser, Model model) {
    Student currentStudent = studentRepository.findById(authUser.id())...
```

A:
```java
@GetMapping
public String chatPage(Authentication auth, Model model) {
    Student currentStudent = studentRepository.findByEmail(auth.getName())...
```

## 📁 Archivos Creados

### Dominio (`chat/domain/`)
- **Conversation.java** - Conversación entre dos estudiantes
  - Relaciones: ManyToOne con Student (student1, student2)
  - OneToMany con ChatMessage
  - Métodos: `hasParticipant()`, `getOtherParticipant()`, `updateLastMessageTime()`

- **ChatMessage.java** - Mensaje individual
  - ManyToOne con Conversation y Student (sender)
  - Campos: content, sentAt, isRead
  - Método: `markAsRead()`

### Repositorios (`chat/repo/`)
- **ConversationRepository.java**
  - `findByParticipants(student1, student2)` - Bidireccional
  - `findByStudentOrderByLastMessageDesc(student)` - Lista ordenada
  - `countUnreadConversations(student)` - Badge contador

- **ChatMessageRepository.java**
  - `findByConversationOrderBySentAtAsc(conversation)`
  - `countUnreadMessages(conversation, receiver)`
  - `markAllAsRead(conversation, receiver)` - Bulk update
  - `findLastMessageByConversation(conversation)` - Preview

### DTOs (`chat/dto/`)
- **ChatMessageDTO.java** - Para WebSocket
  - Incluye: messageId, senderId, senderName, senderPhoto, content, sentAt, isRead
  - Enum MessageType: CHAT, JOIN, LEAVE, TYPING

- **ConversationDTO.java** - Para lista UI
  - Incluye: conversationId, otherStudentId, otherStudentName, otherStudentPhoto
  - lastMessage, lastMessageAt, unreadCount, isOnline

### Servicios (`chat/service/`)
- **ChatService.java**
  - `getOrCreateConversation(student1, student2)`
  - `sendMessage(conversation, sender, content)`
  - `getMessages(conversation)`
  - `markMessagesAsRead(conversation, reader)`
  - `getConversationsForStudent(student)` - Con DTOs
  - `toDTO(message)` - Conversión a DTO
  - `countUnreadConversations(student)`

### Controladores (`chat/web/`)
- **ChatWebController.java** - Endpoints HTTP
  - `GET /chat` - Vista principal
  - `GET /chat/conversation/{id}` - Abrir conversación
  - `GET /chat/new/{studentId}` - Iniciar chat

- **ChatMessageController.java** - WebSocket
  - `@MessageMapping("/chat/{conversationId}")` - Enviar mensaje
  - `@MessageMapping("/chat/{conversationId}/typing")` - Typing indicator
  - Broadcast a `/topic/conversation/{id}`
  - Notificaciones a `/user/queue/notifications`

### Configuración (`config/`)
- **WebSocketConfig.java**
  - `@EnableWebSocketMessageBroker`
  - Endpoint: `/ws` con SockJS
  - Broker: `/topic`, `/queue`
  - App prefix: `/app`
  - User prefix: `/user`

### Vista (`templates/chat/`)
- **chat.html** - Interfaz completa WhatsApp-style
  - **Sidebar izquierdo:**
    - Lista de conversaciones
    - Avatares, nombres, último mensaje
    - Timestamps, badges de no leídos
    - Scroll vertical

  - **Panel derecho:**
    - Header con avatar y nombre del contacto
    - Typing indicator
    - Área de mensajes con scroll automático
    - Mensajes alineados (izq: recibidos, der: enviados)
    - Input con botón de envío

  - **WebSocket Client:**
    - Conexión con SockJS + STOMP.js
    - Auto-reconexión en caso de pérdida
    - Subscripción a `/topic/conversation/{id}`
    - Subscripción a `/user/queue/typing`
    - Enter-to-send, typing detection

## 🚀 Cómo Probar

### 1. Compilar y Ejecutar
```bash
# Compilar
./mvnw.cmd clean compile

# Ejecutar
./mvnw.cmd spring-boot:run
```

### 2. Acceder a la Aplicación
- URL: http://localhost:8080
- Inicia sesión con un usuario existente

### 3. Iniciar una Conversación
**Opción A - Desde Perfil Público:**
1. Navega al catálogo de productos
2. Haz clic en un producto de otro usuario
3. Ve al perfil público del estudiante
4. Clic en botón "Enviar mensaje" 💬
5. Se crea la conversación y redirige al chat

**Opción B - Desde el Menú:**
1. Clic en "Chat" en la barra de navegación
2. Se muestra la lista de conversaciones existentes
3. Selecciona una conversación de la lista

### 4. Enviar Mensajes
1. Escribe en el campo de texto inferior
2. Presiona Enter o clic en botón de envío ✈️
3. El mensaje aparece inmediatamente alineado a la derecha
4. El otro usuario lo recibe en tiempo real (si está conectado)

### 5. Probar Tiempo Real (Requiere 2 Navegadores)
1. Abre 2 ventanas de navegador diferentes (o modo incógnito)
2. Inicia sesión con 2 usuarios distintos
3. Usuario A inicia conversación con Usuario B
4. En ventana de Usuario B, navega a "/chat"
5. Envía mensajes desde ambos lados
6. Observa la actualización instantánea sin recargar

### 6. Verificar Características
- ✅ Mensajes en tiempo real
- ✅ Indicador "escribiendo..." (si escribes rápido)
- ✅ Scroll automático a nuevos mensajes
- ✅ Badges de mensajes no leídos
- ✅ Timestamps en formato HH:mm
- ✅ Avatares de usuarios
- ✅ Mensajes persistidos (se guardan en H2)

## 🔧 Arquitectura Técnica

### WebSocket Flow
```
Cliente                    Servidor                    Base de Datos
   |                          |                              |
   |------ Connect /ws ------>|                              |
   |<----- STOMP Ready -------|                              |
   |                          |                              |
   |-- Subscribe /topic/1 --->|                              |
   |                          |                              |
   |-- Send /app/chat/1 ----->|                              |
   |                          |--- Save Message ------------>|
   |                          |<-- Message Saved ------------|
   |<- Broadcast /topic/1 ----|                              |
   |                          |--- Notify other user ------->|
```

### Persistencia
```sql
-- Tablas creadas automáticamente por Hibernate
CREATE TABLE conversation (
  id_conversation INTEGER PRIMARY KEY AUTO_INCREMENT,
  student1_id INTEGER NOT NULL,
  student2_id INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL,
  last_message_at TIMESTAMP NOT NULL,
  FOREIGN KEY (student1_id) REFERENCES student(id_student),
  FOREIGN KEY (student2_id) REFERENCES student(id_student)
);

CREATE TABLE chat_message (
  id_message INTEGER PRIMARY KEY AUTO_INCREMENT,
  conversation_id INTEGER NOT NULL,
  sender_id INTEGER NOT NULL,
  content VARCHAR(2000) NOT NULL,
  sent_at TIMESTAMP NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (conversation_id) REFERENCES conversation(id_conversation),
  FOREIGN KEY (sender_id) REFERENCES student(id_student)
);
```

## 🎨 Personalización UI

### Colores WhatsApp (CSS Variables)
```css
--whatsapp-bg: #111b21;           /* Fondo general */
--whatsapp-sidebar: #202c33;       /* Sidebar conversaciones */
--whatsapp-chat: #0b141a;          /* Fondo área de chat */
--whatsapp-message-out: #005c4b;   /* Mensajes enviados */
--whatsapp-message-in: #202c33;    /* Mensajes recibidos */
--whatsapp-accent: #00a884;        /* Color de acento */
```

### Responsive Design
- Desktop: Sidebar 380px + Chat flexible
- Mobile: Toggle entre lista y chat activo

## 📊 Queries Personalizados

### Encontrar Conversación (Bidireccional)
```java
@Query("SELECT c FROM Conversation c " +
       "WHERE (c.student1 = :student1 AND c.student2 = :student2) " +
       "OR (c.student1 = :student2 AND c.student2 = :student1)")
Optional<Conversation> findByParticipants(Student student1, Student student2);
```

### Contar No Leídos
```java
@Query("SELECT COUNT(DISTINCT c) FROM Conversation c " +
       "JOIN c.messages m " +
       "WHERE (c.student1 = :student OR c.student2 = :student) " +
       "AND m.sender != :student AND m.isRead = false")
long countUnreadConversations(Student student);
```

### Marcar Como Leídos (Bulk Update)
```java
@Modifying
@Query("UPDATE ChatMessage m SET m.isRead = true " +
       "WHERE m.conversation = :conversation " +
       "AND m.sender != :receiver AND m.isRead = false")
int markAllAsRead(Conversation conversation, Student receiver);
```

## 🔐 Seguridad

### Configuración
- `/ws/**` - Público (handshake WebSocket)
- `/chat/**` - Requiere autenticación
- Message handlers validan `Principal` automáticamente

### Validaciones en ChatService
```java
public Conversation getConversationById(Integer id, Student currentStudent) {
    Conversation conv = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada"));

    if (!conv.hasParticipant(currentStudent)) {
        throw new SecurityException("No tienes acceso a esta conversación");
    }

    return conv;
}
```

## 📝 Notas de Desarrollo

1. **Autenticación**: El proyecto usa `Authentication.getName()` que retorna el email
2. **Session Management**: AuthUser se guarda en HttpSession, no en Principal
3. **Transacciones**: Todos los métodos de ChatService usan `@Transactional`
4. **Lazy Loading**: Relaciones configuradas como LAZY para optimizar consultas
5. **Índices**: Agregados en conversation_id, sender_id, student1_id, student2_id

## 🐛 Troubleshooting

### Error: "Cannot invoke id() because authUser is null"
**Solución:** Usar `Authentication auth` y `auth.getName()` en lugar de `@AuthenticationPrincipal`

### WebSocket no conecta
**Verificar:**
- SecurityConfig permite `/ws/**`
- Puerto 8080 disponible
- Console del navegador para errores JavaScript

### Mensajes no se guardan
**Verificar:**
- H2 database en `./data/trukea`
- `spring.jpa.hibernate.ddl-auto=update` en application.properties
- Logs de Hibernate para errores SQL

### Typing indicator no funciona
**Normal:** Requiere conexión rápida, puede que el timeout de 3 segundos sea muy corto

## 🔄 Próximas Mejoras Sugeridas

1. **Presencia Online**: Implementar estado "en línea/desconectado"
2. **Notificaciones Push**: Integrar con Service Workers
3. **Archivos**: Soporte para enviar imágenes/archivos
4. **Búsqueda**: Buscar en historial de mensajes
5. **Editar/Eliminar**: Permitir editar/borrar mensajes
6. **Grupos**: Conversaciones con más de 2 participantes
7. **Emojis**: Picker de emojis integrado
8. **Read Receipts**: Doble check azul cuando se lee

## 📚 Referencias

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [STOMP Protocol](https://stomp.github.io/)
- [SockJS Client](https://github.com/sockjs/sockjs-client)
