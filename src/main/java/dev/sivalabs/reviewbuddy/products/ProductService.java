package dev.sivalabs.reviewbuddy.products;

import dev.sivalabs.reviewbuddy.shared.PagedResult;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ProductService {
    static final int PAGE_SIZE = 6;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PagedResult<ProductDto> findProducts(ProductSearchCriteria criteria) {
        var pageable = PageRequest.of(criteria.pageNo() - 1, PAGE_SIZE, toSort(criteria.sort()));
        var query = criteria.query().isEmpty() ? "" : "%" + criteria.query().toLowerCase() + "%";
        var page = productRepository.search(query, criteria.categorySlug(), pageable);
        return PagedResult.from(page.map(this::toProductDto));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> findAllCategories() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(this::toCategoryDto)
                .toList();
    }

    private Sort toSort(String sort) {
        // Ratings and reviews are not stored yet, so every sort option currently falls back to newest first.
        return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    }

    private ProductDto toProductDto(ProductEntity product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getTagline(),
                product.getDescription(),
                toCategoryDto(product.getCategory()),
                product.getMaker(),
                product.getUrl(),
                product.getImageUrl(),
                product.getCreatedAt());
    }

    private CategoryDto toCategoryDto(CategoryEntity category) {
        return new CategoryDto(category.getId(), category.getName(), category.getSlug());
    }
}
