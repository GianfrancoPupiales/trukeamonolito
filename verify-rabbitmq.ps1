# Script para verificar que RabbitMQ está listo para clustering
# Ejecutar con: .\verify-rabbitmq.ps1

Write-Host "`n=== VERIFICACIÓN DE RABBITMQ PARA CLUSTERING ===" -ForegroundColor Green

# 1. Verificar contenedor
Write-Host "`n1. Estado del contenedor Docker:" -ForegroundColor Cyan
$container = docker ps --filter "name=rabbit-stomp" --format "{{.Status}}"
if ($container -match "Up") {
    Write-Host "   ✅ Contenedor 'rabbit-stomp' está corriendo" -ForegroundColor Green
    Write-Host "   Estado: $container" -ForegroundColor Gray
} else {
    Write-Host "   ❌ Contenedor no está corriendo" -ForegroundColor Red
    Write-Host "   Ejecuta: docker start rabbit-stomp" -ForegroundColor Yellow
    exit 1
}

# 2. Verificar plugin STOMP
Write-Host "`n2. Plugin STOMP:" -ForegroundColor Cyan
$stompEnabled = docker exec rabbit-stomp rabbitmq-plugins list | Select-String "\[E\*\] rabbitmq_stomp"
if ($stompEnabled) {
    Write-Host "   ✅ Plugin STOMP está habilitado" -ForegroundColor Green
} else {
    Write-Host "   ❌ Plugin STOMP no está habilitado" -ForegroundColor Red
    Write-Host "   Ejecuta: docker exec rabbit-stomp rabbitmq-plugins enable rabbitmq_stomp" -ForegroundColor Yellow
    exit 1
}

# 3. Verificar puerto STOMP
Write-Host "`n3. Puerto STOMP (61613):" -ForegroundColor Cyan
$portMapping = docker port rabbit-stomp 61613
if ($portMapping) {
    Write-Host "   ✅ Puerto 61613 está mapeado: $portMapping" -ForegroundColor Green
} else {
    Write-Host "   ❌ Puerto 61613 no está mapeado" -ForegroundColor Red
    Write-Host "   Necesitas recrear el contenedor con: -p 61613:61613" -ForegroundColor Yellow
    exit 1
}

# 4. Verificar puerto Management
Write-Host "`n4. Puerto Management (15672):" -ForegroundColor Cyan
$mgmtPort = docker port rabbit-stomp 15672
if ($mgmtPort) {
    Write-Host "   ✅ Puerto 15672 está mapeado: $mgmtPort" -ForegroundColor Green
    Write-Host "   Accede a: http://localhost:15672 (guest/guest)" -ForegroundColor Gray
} else {
    Write-Host "   ⚠️  Puerto Management no está mapeado" -ForegroundColor Yellow
}

# 5. Verificar archivo .env
Write-Host "`n5. Configuración .env:" -ForegroundColor Cyan
if (Test-Path ".env") {
    $envContent = Get-Content ".env" -Raw
    if ($envContent -match "WEBSOCKET_MODE=relay") {
        Write-Host "   ✅ WEBSOCKET_MODE=relay configurado" -ForegroundColor Green
    } else {
        Write-Host "   ❌ WEBSOCKET_MODE no está en 'relay'" -ForegroundColor Red
        Write-Host "   Cambia WEBSOCKET_MODE=relay en .env" -ForegroundColor Yellow
    }

    if ($envContent -match "RABBITMQ_HOST=localhost") {
        Write-Host "   ✅ RABBITMQ_HOST=localhost configurado" -ForegroundColor Green
    }

    if ($envContent -match "RABBITMQ_STOMP_PORT=61613") {
        Write-Host "   ✅ RABBITMQ_STOMP_PORT=61613 configurado" -ForegroundColor Green
    }
} else {
    Write-Host "   ❌ Archivo .env no encontrado" -ForegroundColor Red
}

# 6. Test de conexión
Write-Host "`n6. Test de conexión:" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:15672" -TimeoutSec 5 -UseBasicParsing
    Write-Host "   ✅ RabbitMQ Management responde correctamente" -ForegroundColor Green
} catch {
    Write-Host "   ❌ No se puede conectar a RabbitMQ Management" -ForegroundColor Red
}

Write-Host "`n=== RESUMEN ===" -ForegroundColor Green
Write-Host "Si todos los checks estan en OK, puedes:" -ForegroundColor White
Write-Host "1. Detener instancias actuales de la aplicacion" -ForegroundColor Yellow
Write-Host "2. Ejecutar:" -ForegroundColor Yellow
Write-Host "   Terminal 1: .\mvnw.cmd spring-boot:run" -ForegroundColor Cyan
Write-Host "   Terminal 2: .\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8081" -ForegroundColor Cyan
Write-Host "3. Buscar en logs: Modo RELAY RabbitMQ" -ForegroundColor Yellow
Write-Host "4. Chatear entre puertos diferentes en tiempo real" -ForegroundColor Magenta
Write-Host ""
