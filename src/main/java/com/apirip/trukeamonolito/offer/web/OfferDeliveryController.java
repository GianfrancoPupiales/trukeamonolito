package com.apirip.trukeamonolito.offer.web;

import com.apirip.trukeamonolito.offer.service.OfferService;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/offers")
public class OfferDeliveryController {

    private final OfferService offers;
    private final StudentRepository students;

    public OfferDeliveryController(OfferService offers, StudentRepository students) {
        this.offers = offers; this.students = students;
    }

    private int me(Authentication auth){ return students.findByEmail(auth.getName()).orElseThrow().getIdStudent(); }

    @GetMapping("/deliveries")
    public String listDeliveries(Authentication auth, Model model){
        int currentStudentId = me(auth);
        model.addAttribute("pendingDeliveries", offers.findAcceptedPendingDeliveryForStudent(currentStudentId));
        model.addAttribute("currentStudentId", currentStudentId);
        return "deliveries";
    }

    @PostMapping("/deliveries/confirm")
    public String confirm(@RequestParam int offerId,
                         @RequestParam boolean wasDelivered,
                         Authentication auth,
                         RedirectAttributes ra){
        try {
            boolean success = offers.confirmDelivery(offerId, me(auth), wasDelivered);
            if (success) {
                if (wasDelivered) {
                    ra.addFlashAttribute("messageType", "success");
                    ra.addFlashAttribute("message", "Entrega confirmada. El trueque se completará cuando ambas partes confirmen.");
                } else {
                    ra.addFlashAttribute("messageType", "warning");
                    ra.addFlashAttribute("message", "Entrega cancelada. Los productos han sido reactivados.");
                }
            } else {
                ra.addFlashAttribute("messageType", "danger");
                ra.addFlashAttribute("message", "No se pudo confirmar la entrega.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("message", "Error al confirmar la entrega: " + e.getMessage());
        }
        return "redirect:/offers/deliveries";
    }
}