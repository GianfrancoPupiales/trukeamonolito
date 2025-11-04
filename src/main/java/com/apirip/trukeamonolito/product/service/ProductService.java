package com.apirip.trukeamonolito.product.service;

import com.apirip.trukeamonolito.product.domain.Product;
import com.apirip.trukeamonolito.product.domain.ProductCategory;
import com.apirip.trukeamonolito.product.dto.ProductForm;
import com.apirip.trukeamonolito.product.repo.ProductRepository;
import com.apirip.trukeamonolito.storage.FileStorageService;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository products;
    private final StudentRepository students;
    private final FileStorageService storage;

    public ProductService(ProductRepository products, StudentRepository students, FileStorageService storage) {
        this.products = products;
        this.students = students;
        this.storage = storage;
    }

    @Transactional
    public Product registerProduct(ProductForm f, int ownerId) {
        Student owner = students.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + ownerId));

        validatePreferences(f.preferences());

        // Foto: guardar en /uploads/products y devolver SOLO filename
        String filename = resolvePhotoFilename(f.photo(), f.existingPhoto());

        Product p = Product.builder()
                .title(f.title().trim())
                .description(f.description().trim())
                .state(f.state())
                .category(f.category())
                .photo(filename)            // <--- solo filename en BD
                .student(owner)
                .preferences(f.preferences())
                .isAvailable(true)
                .build();

        return products.save(p); // datePublication lo pone @PrePersist
    }

    @Transactional
    public Product updateProduct(ProductForm f, int ownerId) {
        Integer id = Optional.ofNullable(f.idProduct())
                .orElseThrow(() -> new IllegalArgumentException("idProduct requerido"));

        Product p = products.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        // (Opcional) validar ownership: p.getStudent().getIdStudent().equals(ownerId)

        validatePreferences(f.preferences());

        p.setTitle(f.title().trim());
        p.setDescription(f.description().trim());
        p.setState(f.state());
        p.setCategory(f.category());
        p.setPreferences(f.preferences());

        String filename = resolvePhotoFilename(f.photo(), f.existingPhoto());
        if (filename != null && !filename.isBlank()) {
            p.setPhoto(filename); // reemplaza solo si hay nueva o se mantiene la existente normalizada
        }

        return products.save(p);
    }

    @Transactional
    public void removeProduct(int idProduct) {
        products.deleteById(idProduct);
    }

    @Transactional(readOnly = true)
    public Product findProductById(int id) {
        return products.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Product> findAvailableProductsByOwner(int ownerId) {
        return products.findByStudent_IdStudentAndIsAvailableTrue(ownerId);
    }

    @Transactional(readOnly = true)
    public List<Product> listProductsForGuest() {
        return products.findByIsAvailableTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> listProductsForUser(int userId) {
        return products.findByIsAvailableTrueAndStudent_IdStudentNot(userId);
    }

    @Transactional(readOnly = true)
    public List<Product> searchByCategory(ProductCategory category, Integer excludeUserId) {
        if (category == null || category == ProductCategory.Cualquiera) {
            return (excludeUserId == null)
                    ? products.findByIsAvailableTrue()
                    : products.findByIsAvailableTrueAndStudent_IdStudentNot(excludeUserId);
        }
        return products.findByCategoryForCatalog(category, excludeUserId);
    }

    @Transactional(readOnly = true)
    public List<Product> searchByTitle(String title, Integer excludeUserId) {
        title = Optional.ofNullable(title).orElse("").trim();
        if (title.isEmpty() || title.length() > 50) return List.of();
        return products.searchByTitle(title, excludeUserId);
    }

    @Transactional
    public void updateAvailability(List<Integer> ids, boolean available) {
        ids.stream()
                .map(products::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(p -> p.setIsAvailable(available));
        // Si quieres flush inmediato: products.saveAll(...);
    }

    // ------------------ helpers ------------------

    private void validatePreferences(List<ProductCategory> prefs) {
        if (prefs == null || prefs.isEmpty() || prefs.size() > 3) {
            throw new IllegalArgumentException("Las preferencias deben ser 1..3 categorías.");
        }
    }

    /**
     * Si viene un archivo, lo guarda (en /uploads/products) y retorna el filename.
     * Si no viene, retorna la foto existente normalizada (solo filename).
     * Si no hay nada, retorna null.
     */
    private String resolvePhotoFilename(MultipartFile uploaded, String existing) {
        if (uploaded != null && !uploaded.isEmpty()) {
            // Requiere que FileStorageService GUARDÉ en .../products y devuelva SOLO filename
            String saved = storage.saveProductPhoto(uploaded); // <-- asegúrate de tener este método
            return normalizeToFilename(saved);
        }
        if (existing != null && !existing.isBlank()) {
            return normalizeToFilename(existing);
        }
        return null;
    }

    /** Asegura que guardemos solo 'foo.jpg' aunque venga una ruta completa. */
    private String normalizeToFilename(String pathOrName) {
        if (pathOrName == null) return null;
        return Path.of(pathOrName).getFileName().toString();
    }
}
