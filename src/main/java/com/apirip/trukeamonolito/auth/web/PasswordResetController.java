package com.apirip.trukeamonolito.auth.web;

import com.apirip.trukeamonolito.auth.service.PasswordResetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            String userEmail = passwordResetService.initiatePasswordReset(email);
            // Store email in session for verification step
            session.setAttribute("resetEmail", userEmail);
            redirectAttributes.addFlashAttribute("message",
                "Se ha enviado un código de 6 dígitos a tu correo. Por favor, revisa tu bandeja de entrada.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
            return "redirect:/reset-password";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            return "redirect:/forgot-password";
        } catch (Exception e) {
            log.error("Error processing password reset request", e);
            redirectAttributes.addFlashAttribute("message",
                "Ocurrió un error al procesar tu solicitud. Por favor, intenta de nuevo.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("message",
                "Sesión expirada. Por favor, solicita un nuevo código.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-warning");
            return "redirect:/forgot-password";
        }
        model.addAttribute("email", email);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("code") String code,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("message",
                "Sesión expirada. Por favor, solicita un nuevo código.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-warning");
            return "redirect:/forgot-password";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/reset-password";
        }

        // Validate password format
        if (!isValidPassword(password)) {
            redirectAttributes.addFlashAttribute("error",
                "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.");
            return "redirect:/reset-password";
        }

        try {
            passwordResetService.resetPassword(code, password);
            session.removeAttribute("resetEmail");
            redirectAttributes.addFlashAttribute("message",
                "Tu contraseña ha sido restablecida exitosamente. Ya puedes iniciar sesión.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
            return "redirect:/signin";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password";
        } catch (Exception e) {
            log.error("Error resetting password", e);
            redirectAttributes.addFlashAttribute("error",
                "Ocurrió un error al restablecer tu contraseña. Por favor, intenta de nuevo.");
            return "redirect:/reset-password";
        }
    }

    private boolean isValidPassword(String password) {
        // At least 8 characters, one uppercase, one digit, one special character
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!_#%*?&])[A-Za-z\\d@$!_#%*?&]{8,}$";
        return password != null && password.matches(regex);
    }
}
