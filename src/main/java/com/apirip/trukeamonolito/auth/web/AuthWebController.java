package com.apirip.trukeamonolito.auth.web;

import com.apirip.trukeamonolito.auth.dto.SignupForm;
import com.apirip.trukeamonolito.storage.FileStorageService;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthWebController {

    private final StudentService students;
    private final FileStorageService storage;

    public AuthWebController(StudentService students, FileStorageService storage) {
        this.students = students;
        this.storage = storage;
    }

    @GetMapping("/signin")
    public String signin() { return "signin"; }

    @GetMapping("/signup")
    public String signup(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new SignupForm(null,null,null,null,null));
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String doSignup(@Valid @ModelAttribute("form") SignupForm form,
                           BindingResult br,
                           @RequestParam(value="photo", required=false) MultipartFile photo,
                           RedirectAttributes ra) {

        if (br.hasErrors()) {
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("message", "Revisa los campos marcados.");
            return "redirect:/signup";
        }

        try {
            String photoFilename = null;
            if (photo != null && !photo.isEmpty()) {
                // guarda en C:/trukeamonolito/uploads/students y retorna SOLO el nombre del archivo
                photoFilename = storage.saveStudentPhoto(photo);
            }

            Student s = Student.builder()
                    .email(form.email())
                    .password(form.password())
                    .name(form.fullName())
                    .uniqueCode(form.uniqueCode())
                    .phone(form.phone())
                    .photo(photoFilename) // guarda solo el filename
                    .build();

            if (!students.createStudent(s)) {
                ra.addFlashAttribute("form", form);
                ra.addFlashAttribute("messageType", "danger");
                ra.addFlashAttribute("message", "El correo institucional ya está registrado.");
                return "redirect:/signup";
            }

            ra.addFlashAttribute("messageType", "success");
            ra.addFlashAttribute("message", "¡Usuario creado correctamente! Ahora inicia sesión.");
            return "redirect:/signin";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("message", e.getMessage());
            return "redirect:/signup";
        } catch (Exception e) {
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("message", "Hubo un error al crear el usuario. Inténtalo de nuevo.");
            return "redirect:/signup";
        }
    }
}