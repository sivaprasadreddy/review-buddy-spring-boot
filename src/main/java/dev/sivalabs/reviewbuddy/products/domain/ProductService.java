package dev.sivalabs.reviewbuddy.products.domain;

import dev.sivalabs.reviewbuddy.products.domain.models.CategoryDto;
import dev.sivalabs.reviewbuddy.products.domain.models.CreateProductCmd;
import dev.sivalabs.reviewbuddy.products.domain.models.FindProductsQuery;
import dev.sivalabs.reviewbuddy.products.domain.models.ProductDto;
import dev.sivalabs.reviewbuddy.shared.PagedResult;
import dev.sivalabs.reviewbuddy.shared.ResourceNotFoundException;
import dev.sivalabs.reviewbuddy.users.UsersAPI;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    public static final int PAGE_SIZE = 6;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UsersAPI usersAPI;

    ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, UsersAPI usersAPI) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.usersAPI = usersAPI;
    }

    @Transactional(readOnly = true)
    public PagedResult<ProductDto> findProducts(FindProductsQuery criteria) {
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

    @Transactional
    public Long createProduct(CreateProductCmd cmd) {
        var maker = usersAPI.getCurrentUserName().orElseThrow(() -> new AccessDeniedException("Login required"));
        var category = categoryRepository
                .findBySlug(cmd.categorySlug())
                .orElseThrow(() -> new IllegalArgumentException("Select an existing category"));
        var product = new ProductEntity();
        product.setName(cmd.name());
        product.setTagline(cmd.tagline());
        product.setDescription(cmd.description());
        product.setCategory(category);
        product.setMaker(maker);
        product.setUrl(cmd.url());
        product.setImageUrl(cmd.imageUrl().isBlank() ? null : cmd.imageUrl());
        return productRepository.save(product).getId();
    }

    @Transactional(readOnly = true)
    public ProductDto findProductById(Long id) {
        return toProductDto(
                productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found")));
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
