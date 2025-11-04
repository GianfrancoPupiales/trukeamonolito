package com.apirip.trukeamonolito.profile.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public record ProfileUpdateForm(
        @NotBlank @Email(regexp = "^[a-zA-Z]+(\\.[a-zA-Z]+[0-9]*)?@epn\\.edu\\.ec$") String email,
        String password,                      // opcional; si viene vacío se conserva
        @NotBlank String name,
        @NotBlank String surname,
        @Pattern(regexp="\\d{10}") String phone,
        String existingPhoto,                 // para conservar la foto actual
        MultipartFile newPhoto                // opcional
) {}
