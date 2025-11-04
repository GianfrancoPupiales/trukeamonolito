package com.apirip.trukeamonolito.reputation.repo;

import com.apirip.trukeamonolito.reputation.domain.Reputation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReputationRepository extends JpaRepository<Reputation, Integer> {
    // Derivada por propiedad anidada
    Optional<Reputation> findByStudent_IdStudent(int idStudent);
}
