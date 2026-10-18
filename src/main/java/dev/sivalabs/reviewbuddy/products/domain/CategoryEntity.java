package dev.sivalabs.reviewbuddy.products.domain;

import dev.sivalabs.reviewbuddy.shared.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
class CategoryEntity extends BaseEntity {

    protected CategoryEntity() {}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_id_generator")
    @SequenceGenerator(name = "category_id_generator", sequenceName = "category_id_seq")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
