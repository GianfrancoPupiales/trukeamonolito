package com.apirip.trukeamonolito.offer.service;

import com.apirip.trukeamonolito.offer.domain.*;
import com.apirip.trukeamonolito.offer.repo.OfferRepository;
import com.apirip.trukeamonolito.product.domain.Product;
import com.apirip.trukeamonolito.product.service.ProductService;
import com.apirip.trukeamonolito.reputation.service.ReputationService;
import com.apirip.trukeamonolito.student.domain.Student;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OfferService {

    private final OfferRepository offers;
    private final ProductService productService;
    private final ReputationService reputationService;
    private final StudentRepository students;

    public OfferService(OfferRepository offers, ProductService productService,
                        ReputationService reputationService, StudentRepository students) {
        this.offers = offers;
        this.productService = productService;
        this.reputationService = reputationService;
        this.students = students;
    }

    @Transactional
    public Offer proposeOffer(int offeringStudentId, int targetProductId, List<Integer> offeredProductIds) {
        Student offering = students.findById(offeringStudentId)
                .orElseThrow(() -> new EntityNotFoundException("Estudiante no encontrado"));
        Product target = productService.findProductById(targetProductId);
        if (target == null) throw new EntityNotFoundException("Producto objetivo no encontrado");

        List<Product> offered = offeredProductIds == null ? List.of() :
                offeredProductIds.stream()
                        .map(productService::findProductById)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        if (offered.isEmpty()) throw new IllegalArgumentException("Selecciona al menos un producto para ofrecer");

        // Duplicidad de oferta pendiente
        long dup = offers.existsPendingByStudentAndProduct(offering.getIdStudent(), target.getIdProduct(),
                target.getStudent().getIdStudent());
        if (dup > 0) throw new IllegalStateException("Ya existe una oferta pendiente para este producto");

        Offer o = Offer.builder()
                .productToOffer(target)
                .studentWhoOffered(offering)
                .offeredProducts(offered)
                .status(OfferStatus.PENDING)
                .isDelivered(false)
                .build();

        return offers.save(o);
    }

    @Transactional(readOnly = true)
    public List<Offer> findPendingReceivedByOwner(int ownerId) {
        return offers.findPendingReceivedByOwner(ownerId).stream()
                .filter(this::allProductsAvailable) // igual que tu filtro
                .toList();
    }

    private boolean allProductsAvailable(Offer offer) {
        if (offer.getProductToOffer() != null && !Boolean.TRUE.equals(offer.getProductToOffer().getIsAvailable())) return false;
        if (offer.getOfferedProducts() != null) {
            for (Product p : offer.getOfferedProducts()) {
                if (!Boolean.TRUE.equals(p.getIsAvailable())) return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<Offer> findPendingSentByStudent(int studentId) {
        return offers.findByStudentWhoOffered_IdStudentAndStatus(studentId, OfferStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Offer findById(int offerId) {
        return offers.findById(offerId).orElse(null);
    }

    @Transactional
    public boolean updateStatus(int offerId, OfferStatus newStatus) {
        Offer offer = offers.findById(offerId).orElse(null);
        if (offer == null || offer.getStatus() != OfferStatus.PENDING) return false;

        switch (newStatus) {
            case REJECTED, CANCELLED -> {
                offer.setStatus(newStatus);
                offers.save(offer);
                return true;
            }
            case ACCEPTED -> {
                offer.setStatus(OfferStatus.ACCEPTED);
                offers.save(offer);
                // desactivar productos involucrados
                deactivateProducts(offer);
                // cancelar otras ofertas pendientes que involucren los mismos productos ofrecidos
                cancelConflictingOffers(offer);
                return true;
            }
            default -> { return false; }
        }
    }

    private void deactivateProducts(Offer offer) {
        List<Integer> allIds = new ArrayList<>();
        if (offer.getProductToOffer() != null) allIds.add(offer.getProductToOffer().getIdProduct());
        if (offer.getOfferedProducts() != null) {
            allIds.addAll(offer.getOfferedProducts().stream().map(Product::getIdProduct).toList());
        }
        productService.updateAvailability(allIds, false);
    }

    private void reactivateProducts(Offer offer) {
        List<Integer> allIds = new ArrayList<>();
        if (offer.getProductToOffer() != null) allIds.add(offer.getProductToOffer().getIdProduct());
        if (offer.getOfferedProducts() != null) {
            allIds.addAll(offer.getOfferedProducts().stream().map(Product::getIdProduct).toList());
        }
        productService.updateAvailability(allIds, true);
    }

    private void cancelConflictingOffers(Offer acceptedOffer) {
        List<Integer> ids = new ArrayList<>();
        if (acceptedOffer.getProductToOffer() != null) ids.add(acceptedOffer.getProductToOffer().getIdProduct());
        if (acceptedOffer.getOfferedProducts() != null) {
            ids.addAll(acceptedOffer.getOfferedProducts().stream().map(Product::getIdProduct).toList());
        }
        var conflicting = offers.findPendingByOfferedProductIds(ids);
        for (Offer o : conflicting) {
            if (!Objects.equals(o.getIdOffer(), acceptedOffer.getIdOffer())) {
                o.setStatus(OfferStatus.CANCELLED);
                offers.save(o);
            }
        }
    }

    @Transactional
    public boolean confirmDelivery(int offerId, int studentId, boolean wasDelivered, Integer rating) {
        Offer offer = offers.findById(offerId).orElse(null);
        if (offer == null || offer.getStatus() != OfferStatus.ACCEPTED) return false;

        // Determinar si el estudiante es el que ofreció o el dueño
        boolean isOfferer = offer.getStudentWhoOffered().getIdStudent() == studentId;
        boolean isOwner = offer.getProductToOffer().getStudent().getIdStudent() == studentId;

        if (!isOfferer && !isOwner) return false; // No está involucrado en esta oferta

        if (wasDelivered) {
            // Validar rating
            if (rating == null || rating < 1 || rating > 5) {
                throw new IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas");
            }

            // Confirmar entrega y calificar según quién sea
            if (isOfferer) {
                offer.confirmByOfferer(rating);
                // El oferente califica al dueño del producto
                int ownerStudentId = offer.getProductToOffer().getStudent().getIdStudent();
                reputationService.rateStudent(ownerStudentId, rating);
            } else {
                offer.confirmByOwner(rating);
                // El dueño califica al oferente
                int offererStudentId = offer.getStudentWhoOffered().getIdStudent();
                reputationService.rateStudent(offererStudentId, rating);
            }

            // Si ambos confirmaron Y calificaron, marcar como completada
            if (offer.isBothConfirmedAndRated()) {
                offer.setStatus(OfferStatus.COMPLETED);
                offer.markAsDelivered();
            }

            offers.save(offer);
            return true;
        } else {
            // Cancelar la entrega → reactivar productos
            reactivateProducts(offer);
            // Penalización al que cancela: calificación de 1 estrella
            reputationService.rateStudent(studentId, 1);
            offer.setStatus(OfferStatus.CANCELLED);
            offers.save(offer);
            return true;
        }
    }

    @Transactional
    public boolean rateOfferAndKeepDelivered(int offerId, int score, int raterStudentId) {
        Offer offer = offers.findById(offerId).orElse(null);
        if (offer == null || !offer.isDelivered()) return false;

        Student owner = offer.getProductToOffer().getStudent();
        if (owner.getIdStudent() == raterStudentId) {
            // Evita que el dueño califique al oferente si la regla lo impide (ajusta a tu política)
            return false;
        }
        reputationService.rateStudent(offer.getStudentWhoOffered().getIdStudent(), score);
        offers.save(offer); // nada que cambiar, pero mantenemos consistencia
        return true;
    }

    @Transactional(readOnly = true)
    public List<Offer> findAcceptedPendingDeliveryByOwner(int ownerId) {
        return offers.findAcceptedUndeliveredByOwner(ownerId);
    }

    @Transactional(readOnly = true)
    public List<Offer> findAcceptedPendingDeliveryForStudent(int studentId) {
        // Obtener ofertas donde el estudiante es oferente o dueño y están ACCEPTED pero no COMPLETED
        List<Offer> asOfferer = offers.findAcceptedUndeliveredByOfferer(studentId);
        List<Offer> asOwner = offers.findAcceptedUndeliveredByOwner(studentId);

        // Combinar ambas listas
        java.util.Set<Offer> all = new java.util.HashSet<>(asOfferer);
        all.addAll(asOwner);
        return new java.util.ArrayList<>(all);
    }

    @Transactional(readOnly = true)
    public List<Offer> findCompletedTradesByStudent(int studentId) {
        List<Offer> sent = offers.findCompletedAsSender(studentId);
        List<Offer> received = offers.findCompletedAsOwner(studentId);
        ArrayList<Offer> all = new ArrayList<>(sent);
        all.addAll(received);
        return all;
    }
}