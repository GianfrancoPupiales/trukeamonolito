package com.apirip.trukeamonolito.student.service;

import com.apirip.trukeamonolito.reputation.domain.Reputation;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository repo;
    private final PasswordEncoder encoder;

    public StudentService(StudentRepository repo, PasswordEncoder encoder) {
        this.repo = repo; this.encoder = encoder;
    }

    public Student authenticate(String email, String rawPassword) {
        return repo.findByEmail(email)
                .filter(s -> encoder.matches(rawPassword, s.getPassword()))
                .orElse(null);
    }

    public boolean createStudent(Student s) {
        if (repo.existsByEmail(s.getEmail())) return false;
        s.setPassword(encoder.encode(s.getPassword()));
        if (s.getReputation()==null) s.setReputation(new Reputation(s));
        repo.save(s);
        return true;
    }

    public Student findStudentById(int id) { return repo.findById(id).orElse(null); }

    public boolean updateStudent(Student s) {
        Student existing = repo.findById(s.getIdStudent()).orElse(null);
        if (existing == null) return false;

        if (s.getPassword()==null || s.getPassword().isBlank())
            s.setPassword(existing.getPassword());
        else
            s.setPassword(encoder.encode(s.getPassword()));

        if (s.getPhoto()==null || s.getPhoto().isBlank())
            s.setPhoto(existing.getPhoto());

        if (existing.getReputation()==null) s.setReputation(new Reputation(s));
        else s.setReputation(existing.getReputation());

        repo.save(s);
        return true;
    }

    public List<Student> findAll(){ return repo.findAll(); }

    @Transactional(readOnly = true)
    public Student getPublicProfileWithProducts(int studentId) {
        Student student = repo.findById(studentId).orElse(null);
        if (student == null) {
            throw new IllegalArgumentException("Estudiante no encontrado: " + studentId);
        }
        // Clonar perfil sin exponer contraseñas
        Student publicProfile = Student.builder()
                .idStudent(student.getIdStudent())
                .name(student.getName())
                .email(student.getEmail())
                .photo(student.getPhoto())
                .uniqueCode(student.getUniqueCode())
                .reputation(student.getReputation())
                .build();
        // Solo productos disponibles para trueque
        var availableProducts = student.getProducts().stream()
                .filter(p -> p.getIsAvailable() != null && p.getIsAvailable())
                .toList();
        publicProfile.setProducts(availableProducts);
        return publicProfile;
    }
}