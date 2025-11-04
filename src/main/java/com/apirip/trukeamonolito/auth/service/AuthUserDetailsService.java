package com.apirip.trukeamonolito.auth.service;

import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthUserDetailsService implements UserDetailsService {
    private final StudentRepository repo;
    public AuthUserDetailsService(StudentRepository repo){ this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Student s = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + email));
        return new org.springframework.security.core.userdetails.User(
                s.getEmail(), s.getPassword(), Set.of(new SimpleGrantedAuthority("USER"))
        );
    }
}