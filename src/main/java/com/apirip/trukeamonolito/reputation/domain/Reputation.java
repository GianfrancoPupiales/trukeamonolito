package com.apirip.trukeamonolito.reputation.domain;

import com.apirip.trukeamonolito.student.domain.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "reputation",
        uniqueConstraints = @UniqueConstraint(name = "uk_reputation_student", columnNames = "idStudent")
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reputation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReputation;

    private int totalScore;     // suma de puntajes
    private int totalVotes;     // cantidad de votos
    private double averageScore;

    @Version
    private Long version;       // para concurrencia optimista

    public Reputation(Student student) {
        this.student = student;
        this.totalScore = 0;
        this.totalVotes = 0;
        this.averageScore = 0.0;
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idStudent", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reputation_student"))
    private Student student;

    /** Lógica de dominio con validación de rango 1..5 */
    public void addRating(int score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("El puntaje debe estar entre 1 y 5.");
        }
        this.totalScore += score;
        this.totalVotes += 1;
        this.averageScore = (double) totalScore / totalVotes;
    }
}
