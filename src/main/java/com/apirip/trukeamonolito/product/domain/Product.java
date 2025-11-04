package com.apirip.trukeamonolito.product.domain;

import com.apirip.trukeamonolito.student.domain.Student;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "product")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProduct")
    private Integer idProduct;

    @Column(nullable = false) private String title;
    @Column(length = 2000)    private String description;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ProductState state;

    @Column(name = "date_publication", nullable = false)
    private LocalDate datePublication;

    @Column(name="isAvailable", nullable = false)
    private Boolean isAvailable = true;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ProductCategory category;

    private String photo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idStudent", foreignKey = @ForeignKey(name="fk_product_student"))
    private Student student;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_preferences", joinColumns = @JoinColumn(name = "idProduct"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private List<ProductCategory> preferences;

    @PrePersist
    void prePersist() {
        if (datePublication == null) {
            datePublication = LocalDate.now();
        }
        isAvailable = true;
    }
}
