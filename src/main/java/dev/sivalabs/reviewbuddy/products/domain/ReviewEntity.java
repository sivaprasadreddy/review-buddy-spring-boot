package dev.sivalabs.reviewbuddy.products.domain;

import dev.sivalabs.reviewbuddy.shared.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "reviewer_id"}))
class ReviewEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_id_generator")
    @SequenceGenerator(name = "review_id_generator", sequenceName = "review_id_seq")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(nullable = false, length = 400)
    private String liked;

    @Column(nullable = false, length = 400)
    private String improvements;

    protected ReviewEntity() {}

    ReviewEntity(Long productId, Long reviewerId, String liked, String improvements) {
        this.productId = productId;
        this.reviewerId = reviewerId;
        this.liked = liked;
        this.improvements = improvements;
    }
}
