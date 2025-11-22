package com.apirip.trukeamonolito.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String recipientEmail, String resetCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("TRUKEA - Código de Restablecimiento de Contraseña");
            message.setText("Hola,\n\n" +
                    "Has solicitado restablecer tu contraseña en TRUKEA.\n\n" +
                    "Tu código de verificación es:\n\n" +
                    "    " + resetCode + "\n\n" +
                    "Este código es válido por 15 minutos.\n\n" +
                    "Ingresa este código en la página de restablecimiento de contraseña para continuar.\n\n" +
                    "Si no solicitaste este cambio, puedes ignorar este correo.\n\n" +
                    "Saludos,\n" +
                    "El equipo de TRUKEA");
            message.setFrom("noreply@trukea.com");

            mailSender.send(message);
            log.info("Password reset code sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Error sending password reset code to: {}", recipientEmail, e);
            throw new RuntimeException("Error al enviar el correo de restablecimiento", e);
        }
    }
}
