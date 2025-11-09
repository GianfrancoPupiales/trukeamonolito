package com.apirip.trukeamonolito.auth.web;

import com.apirip.trukeamonolito.auth.session.AuthUser;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final StudentRepository students;

    public LoginSuccessHandler(StudentRepository students) {
        this.students = students;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String email = authentication.getName();
        Student s = students.findByEmail(email).orElse(null);

        if (s != null) {
            HttpSession session = request.getSession(true);
            // Guarda solo lo necesario (NUNCA password)
            session.setAttribute("AUTH_USER", new AuthUser(
                    s.getIdStudent(), s.getName(), s.getEmail(), s.getPhoto()
            ));
        }

        response.sendRedirect("/products/catalog");
    }
}