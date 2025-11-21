package com.apirip.trukeamonolito.offer.web;

import com.apirip.trukeamonolito.offer.domain.OfferStatus;
import com.apirip.trukeamonolito.offer.service.OfferService;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/offers")
public class OfferInboxController {

    private final OfferService offers;
    private final StudentRepository students;

    public OfferInboxController(OfferService offers, StudentRepository students) {
        this.offers = offers; this.students = students;
    }

    private int me(Authentication auth){ return students.findByEmail(auth.getName()).orElseThrow().getIdStudent(); }

    /** Mostrar ofertas recibidas (bandeja de entrada) */
    @GetMapping("/received")
    public String listReceived(Authentication auth, Model model){
        var receivedOffers = offers.findPendingReceivedByOwner(me(auth));
        model.addAttribute("receivedOffers", receivedOffers);
        return "mailbox";
    }

    /** Mostrar ofertas enviadas */
    @GetMapping("/sent")
    public String listSent(Authentication auth, Model model){
        var sentOffers = offers.findPendingSentByStudent(me(auth));
        model.addAttribute("sentOffers", sentOffers);
        return "proposals";
    }

    /** Responder a ofertas: aceptar, rechazar o cancelar */
    @PostMapping("/respond")
    public String respond(@RequestParam int offerId,
                         @RequestParam String action,
                         RedirectAttributes ra) {
        try {
            boolean success = false;
            String successMessage = "";
            String redirectUrl = "";

            switch (action) {
                case "accept" -> {
                    success = offers.updateStatus(offerId, OfferStatus.ACCEPTED);
                    successMessage = "¡Oferta aceptada correctamente! Los productos han sido desactivados.";
                    redirectUrl = "redirect:/offers/received";
                }
                case "reject" -> {
                    success = offers.updateStatus(offerId, OfferStatus.REJECTED);
                    successMessage = "Oferta rechazada.";
                    redirectUrl = "redirect:/offers/received";
                }
                case "cancel" -> {
                    success = offers.updateStatus(offerId, OfferStatus.CANCELLED);
                    successMessage = "Oferta cancelada correctamente.";
                    redirectUrl = "redirect:/offers/sent";
                }
                default -> {
                    ra.addFlashAttribute("messageType", "danger");
                    ra.addFlashAttribute("message", "Acción no válida.");
                    return "redirect:/offers/received";
                }
            }

            if (success) {
                ra.addFlashAttribute("messageType", "success");
                ra.addFlashAttribute("message", successMessage);
            } else {
                ra.addFlashAttribute("messageType", "warning");
                ra.addFlashAttribute("message", "No se pudo procesar la oferta. Es posible que ya haya sido procesada.");
            }
            return redirectUrl;

        } catch (Exception e) {
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("message", "Error al procesar la oferta: " + e.getMessage());
            return "redirect:/offers/received";
        }
    }
}
