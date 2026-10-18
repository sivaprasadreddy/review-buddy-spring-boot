package dev.sivalabs.reviewbuddy.products.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    @Query(value = """
            select p from ProductEntity p join fetch p.category c
            where (:categorySlug = '' or c.slug = :categorySlug)
              and (:query = ''
                   or lower(p.name) like :query
                   or lower(p.tagline) like :query
                   or lower(p.description) like :query)
            """, countQuery = """
            select count(p) from ProductEntity p join p.category c
            where (:categorySlug = '' or c.slug = :categorySlug)
              and (:query = ''
                   or lower(p.name) like :query
                   or lower(p.tagline) like :query
                   or lower(p.description) like :query)
            """)
    Page<ProductEntity> search(String query, String categorySlug, Pageable pageable);
}
