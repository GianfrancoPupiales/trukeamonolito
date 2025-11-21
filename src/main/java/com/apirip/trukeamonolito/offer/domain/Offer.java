package com.apirip.trukeamonolito.offer.domain;

import com.apirip.trukeamonolito.product.domain.Product;
import com.apirip.trukeamonolito.student.domain.Student;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "offer")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Offer implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOffer")
    private Integer idOffer;

    /** Productos que ofrece el postulante (pueden ser varios) */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "offeredProducts",
            joinColumns = @JoinColumn(name = "idOffer"),
            inverseJoinColumns = @JoinColumn(name = "idProduct"))
    private List<Product> offeredProducts;

    /** Producto objetivo (del dueño que recibirá la oferta) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "productToOffer", foreignKey = @ForeignKey(name="fk_offer_target_product"))
    private Product productToOffer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.PENDING;

    @Column(nullable = false)
    private boolean isDelivered = false;

    /** Confirmación de entrega por parte del estudiante que ofreció */
    @Column(nullable = false)
    private boolean confirmedByOfferer = false;

    /** Confirmación de entrega por parte del dueño del producto objetivo */
    @Column(nullable = false)
    private boolean confirmedByOwner = false;

    /** Quién propone la oferta */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studentWhoOffered", foreignKey = @ForeignKey(name="fk_offer_offered_by"))
    private Student studentWhoOffered;

    @Version
    private Long version;

    public void markAsDelivered() { this.isDelivered = true; }

    /** Confirma la entrega desde la perspectiva del estudiante que ofreció */
    public void confirmByOfferer() {
        this.confirmedByOfferer = true;
        if (this.confirmedByOwner) {
            this.markAsDelivered();
        }
    }

    /** Confirma la entrega desde la perspectiva del dueño del producto */
    public void confirmByOwner() {
        this.confirmedByOwner = true;
        if (this.confirmedByOfferer) {
            this.markAsDelivered();
        }
    }

    /** Verifica si ambas partes han confirmado */
    public boolean isBothConfirmed() {
        return confirmedByOfferer && confirmedByOwner;
    }
}