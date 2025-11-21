package com.apirip.trukeamonolito.offer.repo;

import com.apirip.trukeamonolito.offer.domain.Offer;
import com.apirip.trukeamonolito.offer.domain.OfferStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Integer> {

    @Query("""
     SELECT COUNT(o) FROM Offer o
     WHERE o.studentWhoOffered.idStudent = :studentId
       AND o.productToOffer.idProduct = :productId
       AND o.productToOffer.student.idStudent = :productOwnerId
       AND o.status = 'PENDING'
  """)
    long existsPendingByStudentAndProduct(@Param("studentId") int studentId,
                                          @Param("productId") int productId,
                                          @Param("productOwnerId") int productOwnerId);

    /** Recibidas (pendientes) por el dueño del producto objetivo */
    @EntityGraph(attributePaths = {"studentWhoOffered", "productToOffer", "offeredProducts"})
    @Query("""
     SELECT o FROM Offer o
     WHERE o.status = 'PENDING'
       AND o.productToOffer.student.idStudent = :ownerId
  """)
    List<Offer> findPendingReceivedByOwner(@Param("ownerId") int ownerId);

    /** Ofertas pendientes que involucren cualquiera de estos productos ofrecidos (para cancelar conflictivas) */
    @Query("""
     SELECT DISTINCT o FROM Offer o JOIN o.offeredProducts op
     WHERE o.status = 'PENDING' AND op.idProduct IN :ids
  """)
    List<Offer> findPendingByOfferedProductIds(@Param("ids") List<Integer> productIds);

    /** Aceptadas y no entregadas por dueño */
    @Query("""
     SELECT o FROM Offer o
     WHERE o.status = 'ACCEPTED'
       AND o.isDelivered = false
       AND o.productToOffer.student.idStudent = :ownerId
  """)
    List<Offer> findAcceptedUndeliveredByOwner(@Param("ownerId") int ownerId);

    @EntityGraph(attributePaths = {"studentWhoOffered", "productToOffer", "productToOffer.student", "offeredProducts"})
    List<Offer> findByStudentWhoOffered_IdStudentAndStatus(int studentId, OfferStatus status);

    /** Completadas como emisor y como receptor */
    @Query("SELECT o FROM Offer o WHERE o.studentWhoOffered.idStudent = :studentId AND o.status = 'COMPLETED' AND o.isDelivered = true")
    List<Offer> findCompletedAsSender(@Param("studentId") int studentId);

    @Query("SELECT o FROM Offer o WHERE o.productToOffer.student.idStudent = :studentId AND o.status = 'COMPLETED' AND o.isDelivered = true")
    List<Offer> findCompletedAsOwner(@Param("studentId") int studentId);

    Optional<Offer> findById(Integer id);
}
