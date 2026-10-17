package dev.sivalabs.reviewbuddy.products;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findBySlug(String slug);
}
