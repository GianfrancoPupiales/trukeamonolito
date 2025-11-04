package com.apirip.trukeamonolito.offer.web;

import com.apirip.trukeamonolito.offer.domain.OfferStatus;
import com.apirip.trukeamonolito.offer.service.OfferService;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/offers")
public class OfferInboxController {

    private final OfferService offers;
    private final StudentRepository students;

    public OfferInboxController(OfferService offers, StudentRepository students) {
        this.offers = offers; this.students = students;
    }

    private int me(Authentication auth){ return students.findByEmail(auth.getName()).orElseThrow().getIdStudent(); }

    @GetMapping("/inbox")
    public String listReceived(Authentication auth, Model model){
        model.addAttribute("receivedOffers", offers.findPendingReceivedByOwner(me(auth)));
        model.addAttribute("view", "received");
        return "offers/inbox"; // vista luego
    }

    @GetMapping("/sent")
    public String listSent(Authentication auth, Model model){
        model.addAttribute("sentOffers", offers.findPendingSentByStudent(me(auth)));
        model.addAttribute("view", "sent");
        return "offers/sent"; // vista luego
    }

    @PostMapping("/{id}/accept")
    public String accept(@PathVariable int id){
        offers.updateStatus(id, OfferStatus.ACCEPTED);
        return "redirect:/offers/inbox";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable int id){
        offers.updateStatus(id, OfferStatus.REJECTED);
        return "redirect:/offers/inbox";
    }
}
