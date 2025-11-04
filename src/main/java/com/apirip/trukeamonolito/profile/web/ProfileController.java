package com.apirip.trukeamonolito.profile.web;

import com.apirip.trukeamonolito.profile.dto.ProfileUpdateForm;
import com.apirip.trukeamonolito.storage.FileStorageService;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import com.apirip.trukeamonolito.student.service.StudentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final StudentService students;
    private final StudentRepository studentRepo;
    private final FileStorageService storage;

    public ProfileController(StudentService students, StudentRepository studentRepo, FileStorageService storage) {
        this.students = students;
        this.studentRepo = studentRepo;
        this.storage = storage;
    }

    private int me(Authentication auth) {
        return studentRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"))
                .getIdStudent();
    }

    /** Mostrar mi perfil (equiv. route=show) */
    @GetMapping
    public String show(Authentication auth, Model model) {
        var profile = students.findStudentById(me(auth));
        if (profile == null) throw new EntityNotFoundException("No se pudo encontrar tu perfil");
        model.addAttribute("profile", profile);
        // La vista la haremos luego: "profile/my_profile"
        return "profile/my_profile";
    }

    /** Perfil público (equiv. route=public) */
    @GetMapping("/public/{id}")
    public String publicShow(@PathVariable int id,
                             @RequestParam(value = "from", required = false) String from,
                             Model model) {
        var profile = students.getPublicProfileWithProducts(id);
        model.addAttribute("profile", profile);
        model.addAttribute("from", from);
        // Vista luego: "profile/public_profile"
        return "profile/public_profile";
    }

    /** Editar perfil (equiv. route=edit) */
    @PostMapping
    public String edit(@Valid @ModelAttribute("form") ProfileUpdateForm form,
                       Authentication auth,
                       RedirectAttributes ra,
                       Model model) {

        int myId = me(auth);
        Student logged = students.findStudentById(myId);
        if (logged == null) {
            ra.addFlashAttribute("messageType", "error");
            ra.addFlashAttribute("message", "No se pudo cargar tu sesión.");
            return "redirect:/signin";
        }

        // Resolver foto nueva (opcional) en C:/trukeamonolito/uploads/students
        String photoPath = resolvePhoto(form.newPhoto(), form.existingPhoto());

        // Si password viene vacío, el servicio conservará la anterior (ya implementado)
        String password = (form.password() == null || form.password().isBlank())
                ? "" : form.password();

        // Construimos un Student con los cambios (el service se encarga de mantener reputación/foto si aplica)
        Student updated = Student.builder()
                .idStudent(logged.getIdStudent())
                .uniqueCode(logged.getUniqueCode())
                .email(form.email())
                .password(password)              // StudentService encripta si viene no vacío
                .name(form.name())
                .surname(form.surname())
                .phone(form.phone())
                .photo(photoPath)                // si es null, el service conserva la anterior
                .build();

        boolean ok = students.updateStudent(updated);
        var profile = students.findStudentById(myId);

        model.addAttribute("profile", profile);
        ra.addFlashAttribute("messageType", ok ? "success" : "error");
        ra.addFlashAttribute("message", ok ? "Perfil actualizado correctamente." : "Hubo un problema al actualizar tu perfil.");

        // Redirigimos al GET para evitar repost
        return "redirect:/profile";
    }

    private String resolvePhoto(MultipartFile newPhoto, String existingPhoto) {
        if (newPhoto != null && !newPhoto.isEmpty()) {
            // Guarda en C:/trukeamonolito/uploads/students y retorna solo el filename
            return storage.saveStudentPhoto(newPhoto);
        }
        // Si no se subió una nueva, conserva la existente (solo el nombre)
        return (existingPhoto == null || existingPhoto.isBlank())
                ? null
                : java.nio.file.Path.of(existingPhoto).getFileName().toString();
    }
}