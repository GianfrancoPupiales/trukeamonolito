package com.apirip.trukeamonolito.offer.web;

import com.apirip.trukeamonolito.offer.service.OfferService;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("pendingDeliveries", offers.findAcceptedPendingDeliveryByOwner(me(auth)));
        return "offers/deliveries"; // vista luego
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable int id, @RequestParam boolean wasDelivered){
        offers.confirmDelivery(id, wasDelivered);
        return "redirect:/offers/deliveries";
    }

    @PostMapping("/{id}/rate")
    public String rate(@PathVariable int id, @RequestParam int score, Authentication auth){
        offers.rateOfferAndKeepDelivered(id, score, me(auth));
        return "redirect:/offers/deliveries";
    }

    @GetMapping("/history")
    public String history(Authentication auth, Model model){
        model.addAttribute("tradeHistory", offers.findCompletedTradesByStudent(me(auth)));
        return "offers/history"; // vista luego
    }
}