package com.apirip.trukeamonolito.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class FileStorageService {

    private static final Path ROOT = Paths.get("C:/trukeamonolito/uploads");
    private static final Path STUDENTS = ROOT.resolve("students");
    private static final Path PRODUCTS = ROOT.resolve("products");

    public FileStorageService() {
        try {
            Files.createDirectories(STUDENTS);
            Files.createDirectories(PRODUCTS);
        } catch (IOException e) {
            throw new RuntimeException("No se pudieron crear carpetas de subida", e);
        }
    }

    /** Guarda una foto de ESTUDIANTE y retorna solo el NOMBRE del archivo. */
    public String saveStudentPhoto(MultipartFile file) {
        return store(file, STUDENTS);
    }

    /** Guarda una foto de PRODUCTO y retorna solo el NOMBRE del archivo. */
    public String saveProductPhoto(MultipartFile file) {
        return store(file, PRODUCTS);
    }

    private String store(MultipartFile file, Path folder) {
        if (file == null || file.isEmpty()) return null;
        String original = Objects.requireNonNull(file.getOriginalFilename(), "filename");
        String safeName = Path.of(original).getFileName().toString();
        String filename = System.currentTimeMillis() + "_" + safeName;

        try {
            Files.copy(file.getInputStream(), folder.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename; // <-- MUY IMPORTANTE: guardamos SOLO el nombre en BD
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo " + filename, e);
        }
    }
}