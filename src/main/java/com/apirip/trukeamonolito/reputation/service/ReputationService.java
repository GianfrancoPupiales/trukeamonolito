package com.apirip.trukeamonolito.reputation.service;

import com.apirip.trukeamonolito.reputation.domain.Reputation;
import com.apirip.trukeamonolito.reputation.repo.ReputationRepository;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReputationService {

    private final ReputationRepository reputations;
    private final StudentRepository students;

    public ReputationService(ReputationRepository reputations, StudentRepository students) {
        this.reputations = reputations;
        this.students = students;
    }

    /** Agrega una calificación 1..5 al estudiante indicado. */
    @Transactional
    public Reputation rateStudent(int studentId, int score) {
        Student student = students.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Estudiante no encontrado: " + studentId));

        Reputation rep = reputations.findByStudent_IdStudent(studentId)
                .orElseGet(() -> Reputation.builder()
                        .student(student)
                        .totalScore(0).totalVotes(0).averageScore(0.0)
                        .build());

        rep.addRating(score);

        try {
            return reputations.save(rep);
        } catch (OptimisticLockingFailureException e) {
            // Si dos usuarios califican al mismo tiempo, puedes reintentar o propagar
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Reputation findByStudentId(int studentId) {
        return reputations.findByStudent_IdStudent(studentId).orElse(null);
    }
}