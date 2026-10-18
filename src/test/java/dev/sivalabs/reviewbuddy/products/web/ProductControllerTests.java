package dev.sivalabs.reviewbuddy.products.web;

import static org.assertj.core.api.Assertions.assertThat;

import dev.sivalabs.reviewbuddy.BaseIT;
import dev.sivalabs.reviewbuddy.products.domain.ProductService;
import dev.sivalabs.reviewbuddy.products.domain.models.ProductDto;
import dev.sivalabs.reviewbuddy.shared.PagedResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ProductControllerTests extends BaseIT {

    @Test
    void shouldShowFirstPageOfProducts() {
        var result = mvc.get().uri("/products").exchange();

        assertThat(result).hasStatus(HttpStatus.OK).hasViewName("products");
        assertThat(result).model().extractingByKey("productsPage").isInstanceOfSatisfying(PagedResult.class, page -> {
            assertThat(page.currentPageNo()).isEqualTo(1);
            assertThat(page.data()).hasSize(ProductService.PAGE_SIZE);
            assertThat(page.totalElements()).isEqualTo(8);
            assertThat(page.hasNextPage()).isTrue();
        });
    }

    @Test
    void shouldShowRequestedPageOfProducts() {
        var result = mvc.get().uri("/products?page=2").exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).model().extractingByKey("productsPage").isInstanceOfSatisfying(PagedResult.class, page -> {
            assertThat(page.currentPageNo()).isEqualTo(2);
            assertThat(page.data()).hasSize(2);
            assertThat(page.hasNextPage()).isFalse();
        });
    }

    @Test
    void shouldSearchProductsByKeyword() {
        var result = mvc.get().uri("/products?q=budget").exchange();

        assertThat(result).model().extractingByKey("productsPage").isInstanceOfSatisfying(PagedResult.class, page -> {
            assertThat(page.totalElements()).isEqualTo(1);
            assertThat(((ProductDto) page.data().getFirst()).name()).isEqualTo("BudgetBee");
        });
    }

    @Test
    void shouldFilterProductsByCategory() {
        var result = mvc.get().uri("/products?category=developer-tools").exchange();

        assertThat(result)
                .model()
                .extractingByKey("productsPage")
                .isInstanceOfSatisfying(
                        PagedResult.class,
                        page -> assertThat(page.totalElements()).isEqualTo(2));
    }

    @Test
    void shouldCombineSearchAndCategoryFilter() {
        var result = mvc.get().uri("/products?q=api&category=productivity").exchange();

        assertThat(result)
                .model()
                .extractingByKey("productsPage")
                .isInstanceOfSatisfying(
                        PagedResult.class, page -> assertThat(page.data()).isEmpty());
    }
}
