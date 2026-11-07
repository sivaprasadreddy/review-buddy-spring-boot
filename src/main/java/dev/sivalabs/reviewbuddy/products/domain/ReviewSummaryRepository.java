package dev.sivalabs.reviewbuddy.products.domain;

import dev.sivalabs.reviewbuddy.products.domain.models.ReviewSummaryDto;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class ReviewSummaryRepository {
    private final JdbcClient jdbc;

    ReviewSummaryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ReviewSummaryDto> findByProductId(Long productId) {
        return jdbc.sql("select liked, improvements from review_summaries where product_id = :productId")
                .param("productId", productId)
                .query((rs, rowNum) -> new ReviewSummaryDto(rs.getString("liked"), rs.getString("improvements")))
                .optional();
    }

    @Transactional
    public void save(Long productId, int reviewCount, ReviewSummaryDto summary) {
        // Reviews are append-only. A slower request must not overwrite a newer snapshot.
        jdbc.sql("""
                insert into review_summaries(product_id, review_count, liked, improvements)
                values (:productId, :reviewCount, :liked, :improvements)
                on conflict (product_id) do update
                set review_count = excluded.review_count, liked = excluded.liked,
                    improvements = excluded.improvements, generated_at = CURRENT_TIMESTAMP
                where review_summaries.review_count <= excluded.review_count
                """)
                .param("productId", productId)
                .param("reviewCount", reviewCount)
                .param("liked", summary.liked())
                .param("improvements", summary.improvements())
                .update();
    }
}
