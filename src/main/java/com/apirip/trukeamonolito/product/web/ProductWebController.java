package com.apirip.trukeamonolito.product.web;

import com.apirip.trukeamonolito.product.domain.ProductCategory;
import com.apirip.trukeamonolito.product.dto.ProductForm;
import com.apirip.trukeamonolito.product.service.ProductService;
import com.apirip.trukeamonolito.student.repo.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductWebController {

    private final ProductService service;
    private final StudentRepository students;

    public ProductWebController(ProductService service, StudentRepository students) {
        this.service = service;
        this.students = students;
    }

    private int currentStudentId(Authentication auth) {
        return students.findByEmail(auth.getName()).orElseThrow().getIdStudent();
    }

    @GetMapping
    public String myProducts(Authentication auth, Model model) {
        var list = service.findAvailableProductsByOwner(currentStudentId(auth));
        model.addAttribute("products", list);
        return "products/my_products";
    }

    @GetMapping("/new")
    public String newForm() {
        return "redirect:/products?open=new";
    }

    @PostMapping
    public String create(@Valid ProductForm form, Authentication auth) {
        service.registerProduct(form, currentStudentId(auth));
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable int id, Model model) {
        var p = service.findProductById(id);
        model.addAttribute("product", p);
        model.addAttribute("allCategories", ProductCategory.values());
        return "products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable int id, @Valid ProductForm form, Authentication auth) {
        form = new ProductForm(
                id,
                form.title(),
                form.description(),
                form.state(),
                form.category(),
                form.preferences(),
                form.photo(),
                form.existingPhoto()
        );
        service.updateProduct(form, currentStudentId(auth));
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        try {
            service.removeProduct(id);
            ra.addFlashAttribute("messageType", "success");
            ra.addFlashAttribute("message", "Producto eliminado correctamente.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("messageType", "error");
            ra.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) String category, Authentication auth, Model model) {
        ProductCategory cat = parse(category);
        Integer exclude = (auth == null) ? null : currentStudentId(auth);
        var items = service.searchByCategory(cat, exclude);
        model.addAttribute("products", items);
        model.addAttribute("selectedCategory", category);
        return "/products/catalog";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        var product = service.findProductByIdWithStudent(id);
        if (product == null) {
            return "redirect:/products/catalog";
        }
        model.addAttribute("productDetail", product);
        return "product_detail";
    }

    @GetMapping("/search")
    public String searchByTitle(@RequestParam(required = false) String title,
                                Authentication auth,
                                Model model) {
        if (title == null || title.trim().isEmpty()) {
            return "redirect:/products/catalog";
        }

        Integer exclude = (auth == null) ? null : currentStudentId(auth);
        var found = service.searchByTitle(title.trim(), exclude);
        model.addAttribute("products", found);
        model.addAttribute("searchTerm", title.trim());
        return "/products/catalog";
    }

    private ProductCategory parse(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return ProductCategory.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}