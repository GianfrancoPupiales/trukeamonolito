package com.apirip.trukeamonolito.offer.web;

import com.apirip.trukeamonolito.offer.dto.OfferForm;
import com.apirip.trukeamonolito.offer.service.OfferService;
import com.apirip.trukeamonolito.product.domain.ProductCategory;
import com.apirip.trukeamonolito.product.service.ProductService;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/offers")
public class OfferWebController {

    private final OfferService offers;
    private final ProductService products;
    private final StudentRepository students;

    public OfferWebController(OfferService offers, ProductService products, StudentRepository students) {
        this.offers = offers; this.products = products; this.students = students;
    }

    private int me(Authentication auth){
        return students.findByEmail(auth.getName()).orElseThrow().getIdStudent();
    }

    /** Equivalente a prepare: muestra detalle del target y filtra mis productos según preferencias */
    @GetMapping("/prepare/{targetProductId}")
    public String prepare(@PathVariable int targetProductId, Authentication auth, Model model){
        var target = products.findProductById(targetProductId);
        var currentId = me(auth);

        var prefs = target.getPreferences();
        var hasAny = prefs != null && prefs.stream().anyMatch(p -> p == ProductCategory.Cualquiera);
        var myProducts = hasAny
                ? products.findAvailableProductsByOwner(currentId)
                : products.searchByCategory(target.getCategory(), currentId) // o implementar método específico por prefs
                ;

        model.addAttribute("productDetail", target);
        model.addAttribute("productsOfUser", myProducts);
        model.addAttribute("form", new OfferForm(targetProductId, null));
        return "offers/prepare"; // vista luego
    }

    /** Equivalente a propose */
    @PostMapping("/propose")
    public String propose(@Valid @ModelAttribute("form") OfferForm form, Authentication auth){
        offers.proposeOffer(me(auth), form.targetProductId(), form.offeredProductIds());
        return "redirect:/products/catalog";
    }

    /** Equivalente a cancel (del emisor) → lo tratamos como REJECTED/CANCELLED desde el servicio de estado */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable int id){
        offers.updateStatus(id, com.apirip.trukeamonolito.offer.domain.OfferStatus.CANCELLED);
        return "redirect:/offers/sent";
    }
}