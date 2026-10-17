package dev.sivalabs.reviewbuddy.products;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class ProductController {
    static final double STATIC_RATING = 4.0;

    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    String listProducts(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "category", required = false) String categorySlug,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
        var criteria = new ProductSearchCriteria(query, categorySlug, sort, page);
        model.addAttribute("criteria", criteria);
        model.addAttribute("categories", productService.findAllCategories());
        model.addAttribute("productsPage", productService.findProducts(criteria));
        model.addAttribute("rating", STATIC_RATING);
        return "products";
    }
}
