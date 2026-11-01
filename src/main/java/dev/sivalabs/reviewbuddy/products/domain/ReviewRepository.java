package dev.sivalabs.reviewbuddy.products.domain;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    boolean existsByProductIdAndReviewerId(Long productId, Long reviewerId);
}
