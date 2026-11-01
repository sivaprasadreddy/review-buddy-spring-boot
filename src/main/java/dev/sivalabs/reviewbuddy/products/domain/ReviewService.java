package dev.sivalabs.reviewbuddy.products.domain;

import dev.sivalabs.reviewbuddy.shared.ResourceNotFoundException;
import dev.sivalabs.reviewbuddy.users.UsersAPI;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final ProductRepository products;
    private final ReviewRepository reviews;
    private final UsersAPI users;

    ReviewService(ProductRepository products, ReviewRepository reviews, UsersAPI users) {
        this.products = products;
        this.reviews = reviews;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public void checkEligibility(Long productId) {
        var reviewerId = currentReviewer();
        var product =
                products.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        checkEligibility(product, reviewerId);
    }

    @Transactional
    public void submitReview(Long productId, String liked, String improvements) {
        var reviewerId = currentReviewer();
        // Serialize eligibility checks and insertion for this product, including concurrent duplicate requests.
        var product =
                products.findForReview(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        checkEligibility(product, reviewerId);
        if (liked == null
                || liked.isBlank()
                || liked.length() > 400
                || improvements == null
                || improvements.isBlank()
                || improvements.length() > 400) {
            throw new IllegalArgumentException("Both feedback fields must contain 1 to 400 characters");
        }
        reviews.saveAndFlush(new ReviewEntity(productId, reviewerId, liked, improvements));
    }

    private Long currentReviewer() {
        return users.getCurrentUserId().orElseThrow(() -> new AccessDeniedException("Login required"));
    }

    private void checkEligibility(ProductEntity product, Long reviewerId) {
        if (reviewerId.equals(product.getMakerId())) {
            throw new AccessDeniedException("You cannot review your own product");
        }
        if (reviews.existsByProductIdAndReviewerId(product.getId(), reviewerId)) {
            throw new AccessDeniedException("You have already reviewed this product");
        }
    }
}
