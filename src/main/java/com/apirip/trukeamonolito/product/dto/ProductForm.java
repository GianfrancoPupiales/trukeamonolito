package com.apirip.trukeamonolito.product.dto;

import com.apirip.trukeamonolito.product.domain.ProductCategory;
import com.apirip.trukeamonolito.product.domain.ProductState;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ProductForm(
        Integer idProduct,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull ProductState state,
        @NotNull ProductCategory category,
        @Size(min = 1, max = 3) List<ProductCategory> preferences,
        MultipartFile photo,              // opcional (crear/editar)
        String existingPhoto              // para conservar si no sube nueva
) {}
