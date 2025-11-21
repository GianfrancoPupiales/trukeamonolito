package com.apirip.trukeamonolito.offer.web;

import com.apirip.trukeamonolito.offer.dto.OfferForm;
import com.apirip.trukeamonolito.offer.service.OfferService;
import com.apirip.trukeamonolito.product.domain.ProductCategory;
import com.apirip.trukeamonolito.product.service.ProductService;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        var target = products.findProductByIdWithStudent(targetProductId);
        if (target == null) {
            throw new EntityNotFoundException("Producto no encontrado: " + targetProductId);
        }

        var currentId = me(auth);

        var prefs = target.getPreferences();
        var hasAny = prefs != null && prefs.stream().anyMatch(p -> p == ProductCategory.Cualquiera);

        var myProducts = hasAny
                ? products.findAvailableProductsByOwner(currentId)
                : products.findAvailableProductsByOwnerAndCategories(currentId, prefs);

        model.addAttribute("productDetail", target);
        model.addAttribute("productsOfUser", myProducts);
        model.addAttribute("form", new OfferForm(targetProductId, null));
        return "offers/prepare";
    }

    /** Equivalente a propose */
    @PostMapping("/propose")
    public String propose(@Valid @ModelAttribute("form") OfferForm form,
                         BindingResult br,
                         Authentication auth,
                         RedirectAttributes ra){
        if (br.hasErrors()) {
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("message", "Por favor selecciona al menos un producto para ofrecer.");
            return "redirect:/offers/prepare/" + form.targetProductId();
        }

        try {
            offers.proposeOffer(me(auth), form.targetProductId(), form.offeredProductIds());
            ra.addFlashAttribute("messageType", "success");
            ra.addFlashAttribute("message", "¡Oferta enviada correctamente!");
            return "redirect:/products/catalog";
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("message", "Producto no encontrado: " + e.getMessage());
            return "redirect:/products/catalog";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("message", e.getMessage());
            return "redirect:/offers/prepare/" + form.targetProductId();
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("messageType", "warning");
            ra.addFlashAttribute("message", e.getMessage());
            return "redirect:/products/catalog";
        } catch (Exception e) {
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("message", "Error al enviar la oferta. Por favor intenta de nuevo.");
            return "redirect:/offers/prepare/" + form.targetProductId();
        }
    }

}