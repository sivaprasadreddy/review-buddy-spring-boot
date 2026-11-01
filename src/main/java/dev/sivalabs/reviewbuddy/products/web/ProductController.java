package dev.sivalabs.reviewbuddy.products.web;

import dev.sivalabs.reviewbuddy.products.domain.ProductService;
import dev.sivalabs.reviewbuddy.products.domain.models.CreateProductCmd;
import dev.sivalabs.reviewbuddy.products.domain.models.FindProductsQuery;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class ProductController {
    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products/submit")
    String submissionForm(Model model) {
        model.addAttribute("product", new SubmitProductRequest("", "", "", "", "", ""));
        model.addAttribute("categories", productService.findAllCategories());
        return "submit-product";
    }

    @PostMapping("/products/submit")
    String submitProduct(
            @Valid @ModelAttribute("product") SubmitProductRequest request, BindingResult bindingResult, Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                var id = productService.createProduct(new CreateProductCmd(
                        request.name(),
                        request.tagline(),
                        request.description(),
                        request.categorySlug(),
                        request.url(),
                        request.imageUrl()));
                return "redirect:/products/" + id;
            } catch (IllegalArgumentException e) {
                bindingResult.rejectValue("categorySlug", "invalid", e.getMessage());
            }
        }
        model.addAttribute("categories", productService.findAllCategories());
        return "submit-product";
    }

    @GetMapping("/products/{id:[0-9]+}")
    String productDetails(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findProductById(id));
        return "product";
    }

    @GetMapping("/products")
    String listProducts(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "category", required = false) String categorySlug,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
        var criteria = new FindProductsQuery(query, categorySlug, page);
        model.addAttribute("criteria", criteria);
        model.addAttribute("categories", productService.findAllCategories());
        model.addAttribute("productsPage", productService.findProducts(criteria));
        return "products";
    }
}
