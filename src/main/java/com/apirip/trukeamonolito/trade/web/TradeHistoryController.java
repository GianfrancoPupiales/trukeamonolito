package com.apirip.trukeamonolito.trade.web;

import com.apirip.trukeamonolito.offer.service.OfferService;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trades")
public class TradeHistoryController {

    private final OfferService offers;
    private final StudentRepository students;

    public TradeHistoryController(OfferService offers, StudentRepository students) {
        this.offers = offers;
        this.students = students;
    }

    private int me(Authentication auth){
        return students.findByEmail(auth.getName()).orElseThrow().getIdStudent();
    }

    @GetMapping("/history")
    public String history(Authentication auth, Model model){
        int currentStudentId = me(auth);
        model.addAttribute("tradeHistory", offers.findCompletedTradesByStudent(currentStudentId));
        model.addAttribute("currentStudentId", currentStudentId);
        return "trade_history";
    }
}
