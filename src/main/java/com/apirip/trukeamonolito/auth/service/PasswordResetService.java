package com.apirip.trukeamonolito.auth.service;

import com.apirip.trukeamonolito.auth.domain.PasswordResetToken;
import com.apirip.trukeamonolito.auth.repo.PasswordResetTokenRepository;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final StudentRepository studentRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int EXPIRY_MINUTES = 15;

    @Transactional
    public String initiatePasswordReset(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con ese correo"));

        // Delete any existing tokens for this student
        tokenRepository.deleteByStudentId((long) student.getIdStudent());

        // Generate 6-digit code
        String token = generateSixDigitCode();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .student(student)
                .expiryDate(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Send email with code
        emailService.sendPasswordResetEmail(email, token);

        log.info("Password reset code generated for email: {}", email);
        return email; // Return email for session storage
    }

    private String generateSixDigitCode() {
        java.util.Random random = new java.util.Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional(readOnly = true)
    public PasswordResetToken validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("El token ha expirado o ya fue utilizado");
        }

        return resetToken;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = validateToken(token);

        Student student = resetToken.getStudent();
        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for student: {}", student.getEmail());
    }

    // Clean up expired tokens daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanUpExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Expired password reset tokens cleaned up");
    }
}
