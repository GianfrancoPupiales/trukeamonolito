package com.apirip.trukeamonolito.product.repo;

import com.apirip.trukeamonolito.product.domain.Product;
import com.apirip.trukeamonolito.product.domain.ProductCategory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // ----- Métodos derivados: inicializa student y preferences -----
    @EntityGraph(attributePaths = {"student", "preferences"})
    List<Product> findByStudent_IdStudentAndIsAvailableTrue(int idStudent);

    @EntityGraph(attributePaths = {"student", "preferences"})
    List<Product> findByIsAvailableTrueAndStudent_IdStudentNot(int idStudent);

    @EntityGraph(attributePaths = {"student", "preferences"})
    List<Product> findByIsAvailableTrue();

    @EntityGraph(attributePaths = {"student", "preferences"})
    List<Product> findByStudent_IdStudentAndIsAvailableTrueAndCategoryIn(
            int idStudent, List<ProductCategory> categories
    );

    @EntityGraph(attributePaths = {"student", "preferences"})
    @Query("""
              SELECT p FROM Product p
              WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))
                AND p.isAvailable = true
                AND (:excludeId IS NULL OR p.student.idStudent <> :excludeId)
              ORDER BY p.datePublication DESC
            """)
    List<Product> searchByTitle(@Param("title") String title, @Param("excludeId") Integer excludeId);

    @EntityGraph(attributePaths = {"student", "preferences"})
    @Query("""
              SELECT p FROM Product p
              WHERE p.category = :category
                AND p.isAvailable = true
                AND (:excludeId IS NULL OR p.student.idStudent <> :excludeId)
              ORDER BY p.datePublication DESC
            """)
    List<Product> findByCategoryForCatalog(@Param("category") ProductCategory category,
                                           @Param("excludeId") Integer excludeId);

    @EntityGraph(attributePaths = {"student", "preferences"})
    @Query("SELECT p FROM Product p WHERE p.idProduct = :id")
    Product findByIdWithStudent(@Param("id") int id);

}