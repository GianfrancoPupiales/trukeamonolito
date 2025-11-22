# Sistema de Chat - Resumen de Cambios

## ✅ Cambios Implementados

### 1. **Chat vinculado a ofertas aceptadas**
Las conversaciones de chat ahora se crean **automáticamente** cuando una oferta es aceptada, no manualmente.

**Flujo:**
1. Usuario A envía oferta a Usuario B
2. Usuario B **acepta** la oferta
3. ✨ **Automáticamente** se crea una conversación de chat entre ambos
4. Ambos usuarios pueden acceder al chat desde el navbar

**Cambios técnicos:**
- `Conversation` ahora tiene referencia `OneToOne` con `Offer`
- `OfferService.updateStatus()` crea la conversación al aceptar (línea 115-120)
- Eliminada ruta `/chat/new/{id}` (ya no se crean manualmente)
- Eliminado botón "Enviar mensaje" del perfil público

### 2. **UI con fondo blanco y diseño limpio**
El diseño del chat ahora es consistente con el resto de la aplicación:

**Cambios visuales:**
- ✅ Fondo blanco (`#ffffff`) en lugar de oscuro
- ✅ Texto oscuro (`#212529`) bien legible
- ✅ Mensajes enviados: fondo azul `#0d6efd` (Bootstrap primary)
- ✅ Mensajes recibidos: fondo blanco con borde gris
- ✅ Sidebar con bordes sutiles
- ✅ Sombras suaves para profundidad

### 3. **Área de escritura mejorada**
El input de mensajes ahora es mucho más visible:

**Mejoras:**
- ✅ Input con fondo gris claro `#f8f9fa`
- ✅ Borde redondeado (25px) estilo moderno
- ✅ Botón de envío azul destacado
- ✅ Placeholder claro: "Escribe un mensaje..."
- ✅ Padding generoso (15px-20px)
- ✅ Hover effects en el botón

### 4. **Acceso desde navbar**
El enlace "Chat" en la barra de navegación muestra todas las conversaciones activas.

**Comportamiento:**
- `/chat` → Lista todas las conversaciones del usuario
- `/chat/conversation/{id}` → Abre conversación específica
- Solo aparecen conversaciones de ofertas aceptadas

## 📋 Flujo Completo de Uso

### Paso 1: Hacer una oferta
```
1. Usuario A navega al catálogo
2. Ve un producto de Usuario B
3. Hace clic en "Hacer oferta"
4. Selecciona sus productos para intercambiar
5. Envía la oferta
```

### Paso 2: Aceptar la oferta
```
1. Usuario B va a "Ofertas recibidas"
2. Ve la oferta de Usuario A
3. Hace clic en "Aceptar"
4. ✨ Se crea automáticamente la conversación de chat
```

### Paso 3: Chatear
```
1. Usuario A o Usuario B van a "Chat" en el navbar
2. Ven la conversación creada
3. Hacen clic para abrirla
4. Escriben mensajes en tiempo real
```

## 🗄️ Cambios en Base de Datos

### Tabla `conversation`
```sql
CREATE TABLE conversation (
  id_conversation INTEGER PRIMARY KEY,
  student1_id INTEGER NOT NULL,
  student2_id INTEGER NOT NULL,
  offer_id INTEGER NOT NULL UNIQUE,  -- ← NUEVO: referencia a oferta
  created_at TIMESTAMP,
  last_message_at TIMESTAMP,
  FOREIGN KEY (offer_id) REFERENCES offer(id_offer)
);
```

**Restricción:** Cada oferta solo puede tener UNA conversación.

## 📝 Archivos Modificados

### Dominio
- `Conversation.java:45` - Agregado campo `offer` (OneToOne)

### Repositorios
- `ConversationRepository.java:18` - Agregado `findByOffer(Offer)`

### Servicios
- `ChatService.java:28-58` - Nuevos métodos para crear conversaciones desde ofertas
- `OfferService.java:26,115-120` - Crea conversación al aceptar oferta

### Controladores
- `ChatWebController.java:80-93` - Eliminado método `startConversation()`

### Vistas
- `chat/chat.html` - Rediseño completo con fondo blanco
- `profile/public_profile.html:82-88` - Eliminado botón "Enviar mensaje"

## 🎨 Paleta de Colores

```css
/* Fondo general */
background: #f8f9fa

/* Contenedores */
background: #ffffff
border: #dee2e6

/* Mensajes enviados */
background: #0d6efd (azul Bootstrap)
color: #ffffff

/* Mensajes recibidos */
background: #ffffff
border: #e0e0e0
color: #212529

/* Input */
background: #f8f9fa
border: #dee2e6
placeholder: #6c757d

/* Botón enviar */
background: #0d6efd
hover: #0b5ed7
```

## ⚠️ Consideraciones Importantes

### 1. Conversaciones solo de ofertas aceptadas
- ✅ No se puede chatear sin una oferta aceptada
- ✅ Cada oferta aceptada genera exactamente UNA conversación
- ✅ Si se rechaza/cancela una oferta, la conversación persiste (histórico)

### 2. Datos existentes
**IMPORTANTE:** Si tienes conversaciones antiguas en la base de datos SIN referencia a `offer`, necesitarás:
- Eliminarlas manualmente, o
- Ejecutar un script de migración que las vincule a ofertas

### 3. Lazy @Lazy en OfferService
Se usa `@Lazy` en la inyección de `ChatService` para evitar dependencia circular:
```java
public OfferService(..., @Lazy ChatService chatService)
```

## 🚀 Cómo Probar

### Escenario completo
```bash
# 1. Ejecutar la aplicación
./mvnw.cmd spring-boot:run

# 2. Registrar dos usuarios (A y B)
http://localhost:8080/signup

# 3. Usuario A: Publicar un producto

# 4. Usuario B: Hacer oferta por el producto de A

# 5. Usuario A: Ir a "Ofertas recibidas" y aceptar

# 6. Ambos usuarios: Ir a "Chat" en navbar
#    → Verán la conversación creada automáticamente

# 7. Chatear en tiempo real
```

## 📊 Comparación Antes/Después

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Crear conversación** | Botón en perfil público | Automático al aceptar oferta |
| **Fondo** | Oscuro (#111b21) | Blanco (#ffffff) |
| **Texto** | Claro (#e9edef) | Oscuro (#212529) |
| **Input visibilidad** | Baja (mismo fondo) | Alta (gris claro destacado) |
| **Vinculación** | Independiente | Ligado a Offer (OneToOne) |
| **Acceso** | Desde perfil | Desde navbar (todas las conversaciones) |

## ✨ Próximas Mejoras Sugeridas

1. **Mensaje automático inicial:** Cuando se crea la conversación, enviar mensaje del sistema:
   ```
   "Conversación iniciada por oferta aceptada. ¡Coordinen la entrega!"
   ```

2. **Link a la oferta:** En el header del chat, mostrar la oferta asociada

3. **Notificaciones:** Badge con número de mensajes no leídos en navbar

4. **Búsqueda:** Filtrar conversaciones por nombre de usuario

5. **Timestamps más amigables:** "Hace 5 min", "Ayer", etc.

## 🐛 Testing Checklist

- [x] Compilación exitosa
- [ ] Crear oferta y aceptarla → conversación se crea
- [ ] Enviar mensajes en tiempo real → aparecen instantáneamente
- [ ] Mensajes enviados alineados a derecha (azul)
- [ ] Mensajes recibidos alineados a izquierda (blanco con borde)
- [ ] Input de mensaje visible y funcional
- [ ] Badge de no leídos funciona
- [ ] Indicador "escribiendo..." aparece
- [ ] Scroll automático al recibir mensajes
- [ ] Responsive en móvil

---

**Compilación:** ✅ Exitosa (0 errores, 8 warnings Lombok)
**Estado:** Listo para testing
