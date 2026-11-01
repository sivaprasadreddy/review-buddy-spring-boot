package dev.sivalabs.reviewbuddy.products.web;

import dev.sivalabs.reviewbuddy.products.domain.ProductService;
import dev.sivalabs.reviewbuddy.products.domain.ReviewService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
class ReviewController {
    private final ProductService products;
    private final ReviewService reviews;

    ReviewController(ProductService products, ReviewService reviews) {
        this.products = products;
        this.reviews = reviews;
    }

    @GetMapping("/products/{id:[0-9]+}/reviews/new")
    String form(@PathVariable Long id, Model model) {
        reviews.checkEligibility(id);
        model.addAttribute("review", new SubmitReviewRequest("", ""));
        model.addAttribute("product", products.findProductById(id));
        return "submit-review";
    }

    @PostMapping("/products/{id:[0-9]+}/reviews")
    String submit(
            @PathVariable Long id,
            @Valid @ModelAttribute("review") SubmitReviewRequest request,
            BindingResult errors,
            Model model) {
        reviews.checkEligibility(id);
        if (errors.hasErrors()) {
            model.addAttribute("product", products.findProductById(id));
            return "submit-review";
        }
        reviews.submitReview(id, request.liked(), request.improvements());
        return "redirect:/products/" + id;
    }
}
