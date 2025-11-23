# Trukea Monolito

Plataforma web de intercambio y trueque de productos para estudiantes de la Escuela Politécnica Nacional (EPN). Los estudiantes pueden publicar productos, hacer ofertas de intercambio y construir reputación a través de intercambios exitosos.

## Tabla de Contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Clustering Multi-Instancia](#clustering-multi-instancia)
- [Funcionalidades](#funcionalidades)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Base de Datos](#base-de-datos)
- [Solución de Problemas](#solución-de-problemas)

---

## Características

### Funcionalidades Principales

- **Gestión de Usuarios**
  - Registro con validación de correo institucional EPN
  - Autenticación segura con Spring Security y BCrypt
  - Perfiles de estudiante con foto y reputación
  - Sistema de recuperación de contraseña con código de 6 dígitos vía email

- **Catálogo de Productos**
  - Publicación de productos con foto, descripción y categoría
  - Búsqueda y filtrado por categorías
  - Estados de producto (disponible/no disponible)
  - Carga de imágenes con almacenamiento local

- **Sistema de Ofertas**
  - Propuestas de intercambio (uno o varios productos propios por un producto ajeno)
  - Estados de oferta: PENDING, ACCEPTED, REJECTED, CANCELLED, COMPLETED
  - Confirmación de entrega física
  - Cancelación automática de ofertas conflictivas al aceptar una oferta
  - Historial completo de ofertas enviadas y recibidas

- **Chat en Tiempo Real**
  - Mensajería instantánea entre usuarios con ofertas aceptadas
  - Interfaz estilo WhatsApp Web
  - WebSocket con STOMP para comunicación bidireccional
  - Soporte para clustering multi-instancia con RabbitMQ
  - Contador de mensajes no leídos
  - Conversaciones persistentes en base de datos

- **Sistema de Reputación**
  - Calificación mutua después de completar intercambios
  - Escala de 1 a 5 estrellas
  - Promedio de calificaciones visible en perfil público
  - Penalización por entrega fallida

### Características Técnicas

- **Arquitectura Monolítica Modular**: Organizada por dominios (auth, student, product, offer, chat, reputation)
- **Persistencia con JPA/Hibernate**: Gestión automática de esquema
- **Seguridad robusta**: Autenticación basada en formularios, protección CSRF, sesiones HTTP
- **Transacciones ACID**: Operaciones complejas con garantía de consistencia
- **WebSocket bidireccional**: Chat en tiempo real con reconexión automática
- **Modo dual de clustering**: Simple (desarrollo) y Relay (producción con RabbitMQ)
- **Logging detallado**: Trazabilidad completa de operaciones

---

## Tecnologías

### Backend
- **Java 21**: Lenguaje principal
- **Spring Boot 3.5.7**: Framework principal
  - Spring Web: REST y MVC
  - Spring Data JPA: Persistencia
  - Spring Security: Autenticación y autorización
  - Spring WebSocket: Chat en tiempo real
  - Spring Mail: Envío de correos
- **PostgreSQL**: Base de datos relacional
- **RabbitMQ**: Message broker para clustering
- **Lombok**: Reducción de código boilerplate
- **Maven**: Gestión de dependencias y build

### Frontend
- **Thymeleaf**: Motor de plantillas
- **Bootstrap 5**: Framework CSS
- **Font Awesome**: Iconografía
- **SockJS + STOMP.js**: Cliente WebSocket
- **JavaScript vanilla**: Interactividad

### Infraestructura
- **Docker**: Contenedores para PostgreSQL y RabbitMQ
- **Mailtrap**: Servidor SMTP de prueba para desarrollo

---

## Arquitectura

### Patrón de Capas

```
┌─────────────────────────────────────────────────────────┐
│                    Capa de Presentación                 │
│         (Thymeleaf Templates + Bootstrap 5)             │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                   Capa de Controladores                 │
│        (Spring MVC Controllers + WebSocket)             │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                    Capa de Servicios                    │
│            (Lógica de Negocio + Transacciones)          │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                  Capa de Repositorios                   │
│              (Spring Data JPA Repositories)             │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                   Capa de Persistencia                  │
│                  (PostgreSQL Database)                  │
└─────────────────────────────────────────────────────────┘
```

### Arquitectura de Chat (Modo Clustering)

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│  Navegador   │         │  Navegador   │         │  Navegador   │
│  Usuario A   │         │  Usuario B   │         │  Usuario C   │
└──────┬───────┘         └──────┬───────┘         └──────┬───────┘
       │ WebSocket              │ WebSocket              │ WebSocket
       │ (SockJS/STOMP)         │ (SockJS/STOMP)         │ (SockJS/STOMP)
       │                        │                        │
┌──────▼────────┐        ┌──────▼────────┐        ┌──────▼────────┐
│  Instancia 1  │        │  Instancia 2  │        │  Instancia 3  │
│  Puerto 8080  │        │  Puerto 8081  │        │  Puerto 8082  │
└──────┬────────┘        └──────┬────────┘        └──────┬────────┘
       │                        │                        │
       └────────────────────────┼────────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │     RabbitMQ STOMP    │
                    │   (Message Broker)    │
                    │    Puerto 61613       │
                    └───────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │     PostgreSQL        │
                    │   (Base de Datos)     │
                    │    Puerto 5432        │
                    └───────────────────────┘
```

---

## Requisitos Previos

### Software Requerido

1. **Java Development Kit (JDK) 21**
   - Descargar desde: https://adoptium.net/
   - Verificar instalación: `java -version`

2. **Maven 3.8+** (incluido en el proyecto como Maven Wrapper)
   - No requiere instalación separada
   - El proyecto incluye `mvnw` (Linux/Mac) y `mvnw.cmd` (Windows)

3. **Docker Desktop**
   - Descargar desde: https://www.docker.com/products/docker-desktop
   - Requerido para PostgreSQL y RabbitMQ

4. **Git** (para clonar el repositorio)
   - Descargar desde: https://git-scm.com/

### Herramientas Opcionales

- **IntelliJ IDEA** o **Eclipse**: IDEs recomendados
- **Postman**: Para probar APIs REST
- **DBeaver** o **pgAdmin**: Clientes GUI para PostgreSQL

---

## Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/GianfrancoPupiales/trukeamonolito.git
cd trukea-monolito
```

### 2. Instalar PostgreSQL con Docker

Ejecuta el siguiente comando para crear un contenedor de PostgreSQL:

```bash
docker run --name trukea-postgres \
  -e POSTGRES_USER=trukea \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=trukeadb \
  -p 5432:5432 \
  -v trukea_pg_data:/var/lib/postgresql/data \
  -d postgres:latest
```

**En Windows PowerShell:**

```powershell
docker run --name trukea-postgres -e POSTGRES_USER=trukea -e POSTGRES_PASSWORD=password -e POSTGRES_DB=trukeadb -p 5432:5432 -v trukea_pg_data:/var/lib/postgresql/data -d postgres:latest
```

**Explicación de los parámetros:**
- `--name trukea-postgres`: Nombre del contenedor
- `-e POSTGRES_USER=trukea`: Usuario de PostgreSQL
- `-e POSTGRES_PASSWORD=password`: Contraseña del usuario
- `-e POSTGRES_DB=trukeadb`: Nombre de la base de datos
- `-p 5432:5432`: Mapeo del puerto (host:contenedor)
- `-v trukea_pg_data:/var/lib/postgresql/data`: Volumen persistente para datos
- `-d`: Ejecutar en segundo plano
- `postgres:latest`: Imagen de PostgreSQL

**Verificar que el contenedor está corriendo:**

```bash
docker ps | grep trukea-postgres
```

### 3. Instalar RabbitMQ con Docker

Ejecuta el siguiente comando para crear un contenedor de RabbitMQ:

```bash
docker run -d \
  --hostname rabbit-server \
  --name rabbit-stomp \
  -p 61613:61613 \
  -p 15672:15672 \
  rabbitmq:3-management
```

**En Windows PowerShell:**

```powershell
docker run -d --hostname rabbit-server --name rabbit-stomp -p 61613:61613 -p 15672:15672 rabbitmq:3-management
```

**Explicación de los parámetros:**
- `--hostname rabbit-server`: Nombre del host de RabbitMQ
- `--name rabbit-stomp`: Nombre del contenedor
- `-p 61613:61613`: Puerto STOMP para WebSocket
- `-p 15672:15672`: Puerto de la interfaz de administración web
- `rabbitmq:3-management`: Imagen con consola de administración

**Habilitar el plugin STOMP:**

El proyecto incluye un script PowerShell para habilitar el plugin STOMP automáticamente:

```powershell
.\enable-rabbitmq-stomp.ps1
```

**O manualmente:**

```bash
docker exec rabbit-stomp rabbitmq-plugins enable rabbitmq_stomp
```

**Verificar que el plugin está habilitado:**

```bash
docker exec rabbit-stomp rabbitmq-plugins list | grep stomp
```

Deberías ver: `[E*] rabbitmq_stomp`

**Acceder a la consola de administración:**
- URL: http://localhost:15672
- Usuario: `guest`
- Contraseña: `guest`

### 4. Configurar Variables de Entorno

El proyecto utiliza un archivo `.env` para configuración sensible.

**Copiar el archivo de ejemplo:**

```bash
cp .env.example .env
```

**Editar el archivo `.env`:**

```properties
# Database Configuration (PostgreSQL)
DB_URL=jdbc:postgresql://localhost:5432/trukeadb
DB_USERNAME=trukea
DB_PASSWORD=password

# WebSocket Mode
# simple = In-memory broker (desarrollo, sin clustering)
# relay = RabbitMQ broker (producción, con clustering)
WEBSOCKET_MODE=relay

# Mailtrap Configuration
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=tu_username_mailtrap
MAIL_PASSWORD=tu_password_mailtrap

# RabbitMQ Configuration (solo necesario si WEBSOCKET_MODE=relay)
RABBITMQ_HOST=localhost
RABBITMQ_STOMP_PORT=61613
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

**Obtener credenciales de Mailtrap:**

1. Crear cuenta gratuita en: https://mailtrap.io/
2. Ir a "Email Testing" > "Inboxes" > "My Inbox"
3. Copiar credenciales SMTP (host, port, username, password)
4. Actualizar el archivo `.env` con tus credenciales

---

## Configuración

### Modos de Operación

La aplicación soporta dos modos de operación para WebSocket:

#### Modo SIMPLE (Desarrollo)

**Configuración:**
```properties
WEBSOCKET_MODE=simple
```

**Características:**
- Broker de mensajes en memoria (SimpleBroker)
- No requiere RabbitMQ
- Solo una instancia de la aplicación
- Chat funciona solo dentro de la misma instancia
- Ideal para desarrollo local y pruebas

**Cuándo usar:**
- Desarrollo local en una sola computadora
- Pruebas de funcionalidad sin clustering
- Demos rápidas

#### Modo RELAY (Producción con Clustering)

**Configuración:**
```properties
WEBSOCKET_MODE=relay
```

**Características:**
- Broker externo RabbitMQ (STOMP Relay)
- Requiere RabbitMQ corriendo
- Múltiples instancias de la aplicación
- Chat funciona entre diferentes instancias
- Ideal para producción y alta disponibilidad

**Cuándo usar:**
- Producción con múltiples servidores
- Alta disponibilidad y escalabilidad horizontal
- Load balancing entre instancias

### Verificar Configuración

El proyecto incluye un script de verificación:

```powershell
.\verify-rabbitmq.ps1
```

Este script verifica:
- Estado del contenedor Docker de RabbitMQ
- Plugin STOMP habilitado
- Puertos correctamente mapeados
- Configuración del archivo `.env`
- Conectividad a RabbitMQ Management

---

## Ejecución

### Ejecución Básica (Una Instancia)

#### En Linux/Mac:

```bash
./mvnw spring-boot:run
```

#### En Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

#### En Windows (CMD):

```cmd
mvnw.cmd spring-boot:run
```

**Acceder a la aplicación:**
- URL: http://localhost:8080

### Compilar el Proyecto

```bash
# Linux/Mac
./mvnw clean package

# Windows
.\mvnw.cmd clean package
```

El archivo JAR se generará en: `target/trukea-monolito-0.0.1-SNAPSHOT.jar`

### Ejecutar el JAR

```bash
java -jar target/trukea-monolito-0.0.1-SNAPSHOT.jar
```

### Ejecutar con Puerto Personalizado

```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# Windows PowerShell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"

# JAR
java -jar target/trukea-monolito-0.0.1-SNAPSHOT.jar --server.port=8081
```

---

## Clustering Multi-Instancia

El modo clustering permite ejecutar múltiples instancias de la aplicación simultáneamente, compartiendo la misma base de datos y broker de mensajes.

### Requisitos

1. PostgreSQL corriendo (compartido entre instancias)
2. RabbitMQ corriendo con plugin STOMP habilitado
3. Configuración `WEBSOCKET_MODE=relay` en `.env`

### Ejecutar Múltiples Instancias

**Terminal 1 - Instancia en puerto 8080:**

```powershell
.\mvnw.cmd spring-boot:run
```

**Terminal 2 - Instancia en puerto 8081:**

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

**Terminal 3 - Instancia en puerto 8082:**

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8082"
```

### Verificar Clustering

**En los logs de cada instancia, deberías ver:**

```
=== WEBSOCKET CONFIG: Modo RELAY (RabbitMQ) - Clustering HABILITADO ===
```

**Probar el chat entre instancias:**

1. Abre Navegador A en http://localhost:8080
2. Abre Navegador B en http://localhost:8081
3. Inicia sesión con diferentes usuarios en cada navegador
4. Usuario A crea una oferta a Usuario B
5. Usuario B acepta la oferta (esto crea una conversación de chat)
6. Usuario A envía mensaje desde puerto 8080
7. Usuario B recibe el mensaje INSTANTÁNEAMENTE en puerto 8081

**Consola del navegador (F12):**

```
Conectado al WebSocket exitosamente
Destino STOMP: /topic/conversation.1
Suscripción exitosa a /topic/conversation.1
Mensaje recibido vía WebSocket: {...}
Mensaje agregado a la UI y scroll actualizado
```

### Monitorear RabbitMQ

Accede a la consola de administración:
- URL: http://localhost:15672
- Usuario: guest
- Contraseña: guest

**En la consola verás:**
- **Connections**: Número de instancias conectadas
- **Channels**: Canales STOMP activos
- **Exchanges**: `amq.topic` procesando mensajes
- **Queues**: Colas temporales de mensajes
- **Message rates**: Mensajes por segundo


---

## Funcionalidades

### 1. Registro de Usuarios

**Ruta:** `/signup`

**Proceso:**
1. Usuario ingresa datos personales y correo institucional (@epn.edu.ec)
2. Validación de formato de correo y fortaleza de contraseña
3. Contraseña cifrada con BCrypt
4. Creación de registro de reputación asociado
5. Redirección automática al inicio de sesión

**Validaciones:**
- Correo único en el sistema
- Contraseña: mínimo 8 caracteres, una mayúscula, un número, un carácter especial (@$!_#%*?&)
- Correo debe terminar en @epn.edu.ec

### 2. Inicio de Sesión

**Ruta:** `/signin`

**Proceso:**
1. Usuario ingresa correo y contraseña
2. Spring Security valida credenciales
3. Creación de sesión HTTP (30 minutos de timeout)
4. Redirección al catálogo de productos

**Seguridad:**
- Protección contra fuerza bruta
- Sesiones con JSESSIONID
- Protección CSRF habilitada
- Cookies HttpOnly

### 3. Recuperación de Contraseña

**Rutas:** `/forgot-password`, `/reset-password`

**Flujo:**

1. Usuario solicita reseteo en `/forgot-password`
2. Sistema genera código de 6 dígitos aleatorio
3. Código enviado al correo del usuario vía Mailtrap
4. Token válido por 15 minutos
5. Usuario ingresa código en `/reset-password`
6. Sistema valida código y permite cambio de contraseña
7. Nueva contraseña con mismas validaciones que registro
8. Token marcado como usado (un solo uso)

**Características:**
- Limpieza automática de tokens expirados (diaria a las 2 AM)
- Un código por usuario (códigos anteriores son invalidados)
- Validación de formato de contraseña en cliente y servidor

### 4. Publicación de Productos

**Ruta:** `/products/new`

**Proceso:**
1. Usuario completa formulario con título, descripción, categoría y foto
2. Foto guardada en `C:/trukeamonolito/uploads/products/`
3. Producto creado con estado `isAvailable=true`
4. Solo el nombre del archivo se guarda en BD

**Categorías disponibles:**
- Cualquiera (filtro, no categoría real)
- Libros
- Electrónica
- Ropa
- Deportes
- Hogar
- Otros

**Acceso a imágenes:**
- URL pública: `/product-images/{filename}`
- Configuración en `StaticResourceConfig`

### 5. Catálogo de Productos

**Ruta:** `/` o `/products/catalog`

**Características:**
- Vista de todos los productos disponibles
- Filtrado por categoría
- Visualización de foto, título y dueño
- Acceso público (no requiere autenticación)
- Botón "Hacer Oferta" solo para usuarios autenticados

### 6. Sistema de Ofertas

**Ruta para crear:** `/offers/new`

**Flujo de una Oferta:**

**6.1. Creación (Estado: PENDING)**
- Usuario selecciona producto objetivo
- Selecciona uno o más productos propios para ofrecer
- Sistema valida:
  - No existe oferta pendiente duplicada
  - Productos están disponibles
  - No se puede ofertar por producto propio

**6.2. Revisión por el Dueño**
- Dueño ve ofertas en `/offers/inbox`
- Opciones: Aceptar o Rechazar

**6.3. Aceptación (Estado: ACCEPTED)**
- Sistema marca todos los productos involucrados como `isAvailable=false`
- Cancela automáticamente otras ofertas pendientes que involucren esos productos
- Crea conversación de chat entre ambos estudiantes
- Redirección a confirmación de entrega

**6.4. Confirmación de Entrega (Estado: COMPLETED o revertido)**

**Ruta:** `/offers/{id}/delivery`

**Proceso:**
- Dueño confirma si el intercambio físico se realizó exitosamente
- **Si éxito:**
  - Estado cambia a COMPLETED
  - Productos permanecen no disponibles
  - Ambos usuarios pueden calificarse mutuamente
- **Si falla:**
  - Productos se reactivan (`isAvailable=true`)
  - Reputación del ofertante penalizada (score = 1)
  - Oferta no se completa

**6.5. Calificación Mutua**
- Cada usuario califica al otro (1-5 estrellas)
- Calificaciones almacenadas en tabla `Reputation`
- Promedio visible en perfil público

### 7. Chat en Tiempo Real

**Ruta:** `/chat`

**Arquitectura:**
- Protocolo: WebSocket con STOMP sobre SockJS
- Cliente: JavaScript vanilla con SockJS y STOMP.js
- Servidor: Spring WebSocket + RabbitMQ STOMP Relay

**Creación de Conversaciones:**
- Automática al aceptar una oferta
- Asociada a participantes y oferta
- Una conversación puede tener múltiples ofertas relacionadas

**Características del Chat:**

**Interfaz (Estilo WhatsApp Web):**
- Panel izquierdo: Lista de conversaciones
  - Avatar del otro usuario
  - Último mensaje
  - Timestamp
  - Badge de mensajes no leídos
- Panel derecho: Chat activo
  - Header con info del contacto
  - Área de mensajes con scroll automático
  - Input con botón de envío
  - Panel colapsable de ofertas relacionadas

**Mensajería:**
- Envío: Usuario escribe y presiona Enter o botón Send
- Almacenamiento: Mensaje guardado en BD (tabla `chat_messages`)
- Distribución: Enviado vía RabbitMQ a topic `/topic/conversation.{id}`
- Recepción: Otros usuarios suscritos reciben vía WebSocket
- Display: Mensaje agregado al DOM con animación fadeIn
- Scroll: Automático al último mensaje

**Estados de Mensajes:**
- `isRead`: Boolean para tracking de lectura
- Marcado como leído al abrir conversación

**Reconexión Automática:**
- Detección de desconexión
- Reintento cada 3-5 segundos
- Logging detallado en consola del navegador

**Destinos STOMP (Formato RabbitMQ):**
- Mensajes: `/topic/conversation.{conversationId}`
- Typing indicator: Deshabilitado (requiere formato `/exchange`)

**Logging en Consola del Navegador:**
```
Conectado al WebSocket exitosamente
Destino STOMP: /topic/conversation.1
Suscripción exitosa a /topic/conversation.1
Enviando mensaje: {conversationId: 1, content: "Hola"...}
Mensaje enviado al servidor
Mensaje recibido vía WebSocket: {...}
Mensaje parseado: {...}
Mostrando mensaje en UI...
Mensaje agregado a la UI y scroll actualizado
```

### 8. Sistema de Reputación

**Tabla:** `reputation`

**Estructura:**
- Relación 1:1 con Student
- `totalRatings`: Número total de calificaciones recibidas
- `averageScore`: Promedio de calificaciones (1-5)
- Actualización automática al recibir nueva calificación

**Visualización:**
- Perfil público: `/profile/{id}`
- Badge en lista de productos
- Estrellas gráficas en UI

**Penalizaciones:**
- Entrega fallida: Score fijado en 1 para esa transacción
- Impacta el promedio general

---

## Estructura del Proyecto

```
trukea-monolito/
│
├── src/
│   ├── main/
│   │   ├── java/com/apirip/trukeamonolito/
│   │   │   ├── auth/                    # Autenticación y autorización
│   │   │   │   ├── domain/              # PasswordResetToken
│   │   │   │   ├── dto/                 # SignupForm
│   │   │   │   ├── repo/                # PasswordResetTokenRepository
│   │   │   │   ├── service/             # AuthUserDetailsService, PasswordResetService, EmailService
│   │   │   │   ├── session/             # AuthUser (datos de sesión)
│   │   │   │   └── web/                 # AuthWebController, LoginSuccessHandler, PasswordResetController
│   │   │   │
│   │   │   ├── student/                 # Estudiantes
│   │   │   │   ├── domain/              # Student (entidad principal)
│   │   │   │   ├── repo/                # StudentRepository
│   │   │   │   └── service/             # StudentService
│   │   │   │
│   │   │   ├── product/                 # Productos
│   │   │   │   ├── domain/              # Product, ProductCategory, ProductState
│   │   │   │   ├── dto/                 # ProductForm
│   │   │   │   ├── repo/                # ProductRepository
│   │   │   │   ├── service/             # ProductService
│   │   │   │   └── web/                 # ProductControllers
│   │   │   │
│   │   │   ├── offer/                   # Ofertas de intercambio
│   │   │   │   ├── domain/              # Offer, OfferStatus
│   │   │   │   ├── repo/                # OfferRepository (queries JPQL custom)
│   │   │   │   ├── service/             # OfferService (lógica compleja)
│   │   │   │   └── web/                 # OfferWebController, OfferInboxController, OfferDeliveryController
│   │   │   │
│   │   │   ├── chat/                    # Chat en tiempo real
│   │   │   │   ├── domain/              # Conversation, ChatMessage
│   │   │   │   ├── dto/                 # ChatMessageDTO, ConversationDTO
│   │   │   │   ├── repo/                # ConversationRepository, ChatMessageRepository
│   │   │   │   ├── service/             # ChatService
│   │   │   │   └── web/                 # ChatWebController, ChatMessageController
│   │   │   │
│   │   │   ├── reputation/              # Sistema de reputación
│   │   │   │   ├── domain/              # Reputation
│   │   │   │   ├── repo/                # ReputationRepository
│   │   │   │   └── service/             # ReputationService
│   │   │   │
│   │   │   ├── storage/                 # Almacenamiento de archivos
│   │   │   │   └── FileStorageService   # Guardar fotos localmente
│   │   │   │
│   │   │   ├── config/                  # Configuraciones Spring
│   │   │   │   ├── SecurityConfig       # Spring Security
│   │   │   │   ├── WebConfig            # MVC
│   │   │   │   ├── WebSocketConfig      # WebSocket + STOMP (modo dual)
│   │   │   │   └── StaticResourceConfig # Recursos estáticos
│   │   │   │
│   │   │   ├── common/                  # Utilidades compartidas
│   │   │   │   ├── exception/           # Excepciones custom
│   │   │   │   ├── util/                # Helpers
│   │   │   │   └── web/                 # HomeController
│   │   │   │
│   │   │   └── TrukeaMonolitoApplication.java  # Main class
│   │   │
│   │   └── resources/
│   │       ├── templates/               # Plantillas Thymeleaf
│   │       │   ├── chat/                # Chat UI
│   │       │   ├── offers/              # Vistas de ofertas
│   │       │   ├── profile/             # Perfiles
│   │       │   ├── navbar.html          # Navbar reutilizable
│   │       │   ├── home.html            # Catálogo
│   │       │   ├── signin.html          # Login
│   │       │   ├── signup.html          # Registro
│   │       │   ├── forgot_password.html # Recuperar contraseña
│   │       │   └── reset_password.html  # Resetear contraseña
│   │       │
│   │       ├── static/                  # Recursos estáticos (CSS, JS, imágenes)
│   │       ├── application.yml          # Configuración principal
│   │       └── data.sql                 # Datos iniciales (opcional)
│   │
│   └── test/                            # Tests unitarios e integración
│
├── .env                                 # Variables de entorno (NO versionar)
├── .env.example                         # Plantilla de .env
├── .gitignore                           # Archivos ignorados por Git
├── pom.xml                              # Configuración Maven
├── mvnw                                 # Maven Wrapper (Linux/Mac)
├── mvnw.cmd                             # Maven Wrapper (Windows)
├── enable-rabbitmq-stomp.ps1            # Script para habilitar STOMP
├── verify-rabbitmq.ps1                  # Script de verificación
├── CLUSTERING.md                        # Guía de clustering
├── RABBITMQ_SETUP.md                    # Guía de instalación RabbitMQ
└── README.md                            # Este archivo
```

---

## Base de Datos

### Diagrama de Relaciones

```
┌─────────────────┐         ┌─────────────────┐
│    Student      │──1:1────│   Reputation    │
│─────────────────│         │─────────────────│
│ idStudent (PK)  │         │ idReputation(PK)│
│ email (UK)      │         │ student_id (FK) │
│ name            │         │ totalRatings    │
│ password        │         │ averageScore    │
│ photo           │         └─────────────────┘
└────────┬────────┘
         │
         │ 1:N
         │
┌────────▼────────┐         ┌─────────────────┐
│    Product      │         │PasswordResetToken│
│─────────────────│         │─────────────────│
│ idProduct (PK)  │         │ id (PK)         │
│ title           │         │ token (UK)      │
│ description     │         │ student_id (FK) │
│ category        │         │ expiryDate      │
│ photo           │         │ used            │
│ isAvailable     │         └─────────────────┘
│ student_id (FK) │
└────────┬────────┘
         │
         │ N:1
         │
┌────────▼────────────────────────────────────┐
│              Offer                          │
│─────────────────────────────────────────────│
│ idOffer (PK)                                │
│ productToOffer_id (FK) -> Product           │
│ studentWhoOffered_id (FK) -> Student        │
│ status (PENDING, ACCEPTED, REJECTED, etc.)  │
│ createdAt, acceptedAt, completedAt          │
│ deliveryConfirmed, deliverySuccess          │
└────────┬────────────────────────────────────┘
         │
         │ N:N (tabla intermedia: offer_offered_products)
         │
┌────────▼────────┐
│    Product      │  (productos ofrecidos en la oferta)
└─────────────────┘


┌─────────────────────────────────────────────┐
│            Conversation                     │
│─────────────────────────────────────────────│
│ idConversation (PK)                         │
│ student1_id (FK) -> Student                 │
│ student2_id (FK) -> Student                 │
│ createdAt, lastMessageAt                    │
└────────┬────────────────────────────────────┘
         │
         │ 1:N
         │
┌────────▼────────────────────────────────────┐
│            ChatMessage                      │
│─────────────────────────────────────────────│
│ idMessage (PK)                              │
│ conversation_id (FK) -> Conversation        │
│ sender_id (FK) -> Student                   │
│ receiver_id (FK) -> Student                 │
│ content                                     │
│ sentAt                                      │
│ isRead                                      │
└─────────────────────────────────────────────┘


┌─────────────────────────────────────────────┐
│   conversation_offers (N:N)                 │
│─────────────────────────────────────────────│
│ conversation_id (FK) -> Conversation        │
│ offer_id (FK) -> Offer                      │
└─────────────────────────────────────────────┘
```

### Tablas Principales

**students**
- PK: `id_student`
- UK: `email`
- Columnas: email, name, password (BCrypt), ci, phone, address, photo

**reputation**
- PK: `id_reputation`
- FK: `student_id` (1:1 con Student)
- Columnas: total_ratings, average_score

**products**
- PK: `id_product`
- FK: `student_id` (N:1 con Student)
- Columnas: title, description, category, state, photo, is_available, created_at

**offers**
- PK: `id_offer`
- FK: `product_to_offer_id` (producto objetivo)
- FK: `student_who_offered_id` (quien hace la oferta)
- Columnas: status, created_at, accepted_at, completed_at, delivery_confirmed, delivery_success
- Relación N:N con Product (productos ofrecidos) vía tabla `offer_offered_products`

**conversations**
- PK: `id_conversation`
- FK: `student1_id`, `student2_id`
- Columnas: created_at, last_message_at
- Relación N:N con Offer vía tabla `conversation_offers`

**chat_messages**
- PK: `id_message`
- FK: `conversation_id`, `sender_id`, `receiver_id`
- Columnas: content, sent_at, is_read

**password_reset_tokens**
- PK: `id`
- UK: `token`
- FK: `student_id`
- Columnas: token (6 dígitos), expiry_date, used

### Gestión de Esquema

Hibernate gestiona el esquema automáticamente con la configuración:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

**Opciones:**
- `update`: Actualiza esquema sin borrar datos (recomendado para desarrollo)
- `create`: Borra y recrea el esquema al iniciar
- `create-drop`: Borra esquema al cerrar aplicación
- `validate`: Solo valida que el esquema coincida con entidades
- `none`: Sin gestión automática

---

## Solución de Problemas

### PostgreSQL

**Problema: No puedo conectar a PostgreSQL**

```
org.postgresql.util.PSQLException: Connection refused
```

**Soluciones:**

1. Verificar que el contenedor está corriendo:
   ```bash
   docker ps | grep trukea-postgres
   ```

2. Si no está corriendo, iniciarlo:
   ```bash
   docker start trukea-postgres
   ```

3. Verificar logs del contenedor:
   ```bash
   docker logs trukea-postgres
   ```

4. Verificar credenciales en `.env`:
   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/trukeadb
   DB_USERNAME=trukea
   DB_PASSWORD=password
   ```

5. Verificar puerto no esté en uso:
   ```bash
   # Windows
   netstat -ano | findstr :5432

   # Linux/Mac
   lsof -i :5432
   ```

### RabbitMQ

**Problema: Error "Invalid destination" en logs**

```
Received ERROR {message=[Invalid destination]...
```

**Causa:** El formato de destinos STOMP no es correcto para RabbitMQ.

**Solución:** Asegúrate de usar puntos en lugar de barras:
- Correcto: `/topic/conversation.1`
- Incorrecto: `/topic/conversation/1`

**Problema: Plugin STOMP no habilitado**

**Solución:**
```bash
docker exec rabbit-stomp rabbitmq-plugins enable rabbitmq_stomp
```

O ejecutar el script:
```powershell
.\enable-rabbitmq-stomp.ps1
```

**Problema: RabbitMQ no inicia**

**Solución:**
```bash
# Verificar logs
docker logs rabbit-stomp

# Reiniciar contenedor
docker restart rabbit-stomp
```

### WebSocket

**Problema: Mensajes no llegan en tiempo real**

**Verificar en consola del navegador (F12):**

```javascript
// Deberías ver:
Conectado al WebSocket exitosamente
Suscripción exitosa a /topic/conversation.1

// NO deberías ver:
Error de conexión WebSocket
WebSocket no conectado
```

**Soluciones:**

1. Verificar modo en `.env`:
   ```properties
   WEBSOCKET_MODE=relay  # Para clustering
   ```

2. Verificar que RabbitMQ está corriendo
3. Revisar logs del servidor:
   ```
   === WEBSOCKET CONFIG: Modo RELAY (RabbitMQ) - Clustering HABILITADO ===
   ```

4. Probar con modo simple para debugging:
   ```properties
   WEBSOCKET_MODE=simple
   ```

**Problema: Chat funciona en puerto 8080 pero no en 8081**

**Causa:** Estás en modo `simple` que no soporta clustering.

**Solución:** Cambiar a modo `relay`:
```properties
WEBSOCKET_MODE=relay
```

### Mailtrap

**Problema: Correos de reseteo no llegan**

**Verificar:**

1. Credenciales correctas en `.env`
2. Logs de la aplicación:
   ```
   Password reset code sent to: correo@ejemplo.com
   ```
3. Bandeja de Mailtrap en https://mailtrap.io/inboxes

**Problema: Error al enviar email**

```
org.springframework.mail.MailSendException
```

**Soluciones:**

1. Verificar credenciales de Mailtrap
2. Verificar conectividad a internet
3. Revisar límite de emails de Mailtrap (plan gratuito: 500/mes)

### Compilación

**Problema: Error de compilación con Lombok**

```
cannot find symbol: method builder()
```

**Solución:**

1. Asegurar que Lombok está en el classpath
2. Configurar annotation processor en el IDE:
   - IntelliJ: Settings > Build > Compiler > Annotation Processors > Enable
   - Eclipse: Instalar Lombok plugin

3. Limpiar y recompilar:
   ```bash
   ./mvnw clean compile
   ```

**Problema: OutOfMemoryError al compilar**

**Solución:**

```bash
# Aumentar memoria para Maven
export MAVEN_OPTS="-Xmx1024m"
./mvnw clean compile

# Windows
set MAVEN_OPTS=-Xmx1024m
mvnw.cmd clean compile
```

### Docker

**Problema: Docker no está corriendo**

**Solución:**

1. Iniciar Docker Desktop
2. Verificar que el daemon está activo:
   ```bash
   docker ps
   ```

**Problema: Puerto ya en uso**

```
Error starting userland proxy: listen tcp 0.0.0.0:5432: bind: address already in use
```

**Soluciones:**

1. Cambiar el puerto del contenedor:
   ```bash
   docker run -p 5433:5432 ...
   ```

2. Detener proceso que usa el puerto:
   ```bash
   # Windows
   netstat -ano | findstr :5432
   taskkill /PID <PID> /F

   # Linux/Mac
   lsof -i :5432
   kill -9 <PID>
   ```

### Sesiones

**Problema: Sesión expira muy rápido**

**Solución:** Aumentar timeout en `application.yml`:

```yaml
server:
  servlet:
    session:
      timeout: 60m  # 60 minutos
```

**Problema: Error CSRF en formularios**

**Verificar:** Que todos los formularios incluyan el token CSRF:

```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

---

## Scripts Incluidos

### enable-rabbitmq-stomp.ps1

**Propósito:** Habilitar el plugin STOMP en RabbitMQ Docker.

**Uso:**
```powershell
.\enable-rabbitmq-stomp.ps1
```

**Qué hace:**
1. Verifica que Docker está corriendo
2. Habilita el plugin `rabbitmq_stomp`
3. Verifica que el plugin está activo
4. Muestra información de conexión

### verify-rabbitmq.ps1

**Propósito:** Verificar que RabbitMQ está configurado correctamente.

**Uso:**
```powershell
.\verify-rabbitmq.ps1
```

**Qué verifica:**
1. Estado del contenedor Docker
2. Plugin STOMP habilitado
3. Puertos 61613 y 15672 mapeados
4. Configuración del archivo `.env`
5. Conectividad a RabbitMQ Management

---

## Comandos Útiles

### Docker

```bash
# Listar contenedores
docker ps

# Ver logs
docker logs trukea-postgres
docker logs rabbit-stomp

# Detener contenedor
docker stop trukea-postgres
docker stop rabbit-stomp

# Iniciar contenedor
docker start trukea-postgres
docker start rabbit-stomp

# Eliminar contenedor (¡cuidado con los datos!)
docker rm trukea-postgres
docker rm rabbit-stomp

# Eliminar volumen (¡borra todos los datos!)
docker volume rm trukea_pg_data

# Ejecutar comando en contenedor
docker exec -it trukea-postgres psql -U trukea -d trukeadb
docker exec rabbit-stomp rabbitmq-plugins list
```

### PostgreSQL

```bash
# Conectar a psql
docker exec -it trukea-postgres psql -U trukea -d trukeadb

# Comandos psql útiles
\dt              # Listar tablas
\d students      # Describir tabla students
\l               # Listar bases de datos
\q               # Salir
```

### Maven

```bash
# Compilar sin tests
./mvnw clean compile -DskipTests

# Compilar con tests
./mvnw clean test

# Empaquetar JAR
./mvnw clean package

# Limpiar target
./mvnw clean

# Ver dependencias
./mvnw dependency:tree
```

### Git

```bash
# Ignorar .env (ya está en .gitignore)
git status  # .env no debe aparecer

# Commit de cambios
git add .
git commit -m "Descripción del cambio"
git push
```

---

## Contacto y Contribución

Este proyecto fue desarrollado como parte del curso de Aplicaciones Web Avanzadas en la Escuela Politécnica Nacional (EPN).

---
