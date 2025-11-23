# Script para habilitar el plugin STOMP en RabbitMQ Docker
# Ejecutar con: .\enable-rabbitmq-stomp.ps1

Write-Host "=== Configurando RabbitMQ STOMP ===" -ForegroundColor Green

# Verificar que Docker está corriendo
Write-Host "`n1. Verificando Docker..." -ForegroundColor Cyan
docker ps | Select-String "rabbit-stomp"

# Habilitar plugin STOMP
Write-Host "`n2. Habilitando plugin STOMP..." -ForegroundColor Cyan
docker exec rabbit-stomp rabbitmq-plugins enable rabbitmq_stomp

# Verificar que el plugin está habilitado
Write-Host "`n3. Verificando plugins habilitados..." -ForegroundColor Cyan
docker exec rabbit-stomp rabbitmq-plugins list | Select-String "stomp"

# Mostrar información de conexión
Write-Host "`n=== Configuración Completada ===" -ForegroundColor Green
Write-Host "RabbitMQ Management: http://localhost:15672" -ForegroundColor Yellow
Write-Host "Usuario: guest" -ForegroundColor Yellow
Write-Host "Password: guest" -ForegroundColor Yellow
Write-Host "`nPuerto STOMP: 61613" -ForegroundColor Yellow
Write-Host "`nAhora cambia WEBSOCKET_MODE=relay en tu archivo .env" -ForegroundColor Magenta
