package dev.sivalabs.reviewbuddy.products.domain;

import dev.sivalabs.reviewbuddy.products.domain.models.ReviewDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    @Query("""
            select new dev.sivalabs.reviewbuddy.products.domain.models.ReviewDto(r.liked, r.improvements)
            from ReviewEntity r where r.productId = :productId
            order by r.createdAt desc, r.id desc
            """)
    List<ReviewDto> findReviewsByProductId(Long productId);

    boolean existsByProductIdAndReviewerId(Long productId, Long reviewerId);
}
