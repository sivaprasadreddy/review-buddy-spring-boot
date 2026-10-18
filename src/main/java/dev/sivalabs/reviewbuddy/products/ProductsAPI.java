package dev.sivalabs.reviewbuddy.products;

import dev.sivalabs.reviewbuddy.products.domain.ProductService;
import dev.sivalabs.reviewbuddy.products.domain.models.CategoryDto;
import dev.sivalabs.reviewbuddy.products.domain.models.FindProductsQuery;
import dev.sivalabs.reviewbuddy.products.domain.models.ProductDto;
import dev.sivalabs.reviewbuddy.shared.PagedResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductsAPI {
    private final ProductService productService;

    ProductsAPI(ProductService productService) {
        this.productService = productService;
    }

    public PagedResult<ProductDto> findProducts(FindProductsQuery query) {
        return productService.findProducts(query);
    }

    public List<CategoryDto> findAllCategories() {
        return productService.findAllCategories();
    }
}
