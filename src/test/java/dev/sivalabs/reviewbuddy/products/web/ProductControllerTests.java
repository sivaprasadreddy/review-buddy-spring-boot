package dev.sivalabs.reviewbuddy.products.web;

import static org.assertj.core.api.Assertions.assertThat;

import dev.sivalabs.reviewbuddy.BaseIT;
import dev.sivalabs.reviewbuddy.products.domain.ProductService;
import dev.sivalabs.reviewbuddy.products.domain.models.ProductDto;
import dev.sivalabs.reviewbuddy.shared.PagedResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;

class ProductControllerTests extends BaseIT {

    @ParameterizedTest
    @CsvSource({"1,TaskNest,APIPilot", "8,APIPilot,TaskNest"})
    void shouldIdentifyRequestedPublicProduct(Long id, String name, String otherProduct)
            throws java.io.UnsupportedEncodingException {
        var result = mvc.get().uri("/products/" + id).exchange();

        assertThat(result).hasStatusOk().hasViewName("product");
        var html = result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(result).model().extractingByKey("product").isInstanceOfSatisfying(ProductDto.class, product -> {
            assertThat(product.id()).isEqualTo(id);
            assertThat(product.name()).isEqualTo(name);
            assertThat(html)
                    .contains(product.name(), product.tagline(), product.description(), product.url())
                    .doesNotContain(otherProduct);
        });
    }

    @Test
    void shouldBrowseAndOpenListedProductWithoutAuthentication() throws java.io.UnsupportedEncodingException {
        var listing = mvc.get().uri("/products").exchange();
        assertThat(listing).hasStatusOk();
        var html = listing.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(html)
                .contains("href=\"/products/8\"", "APIPilot", "Design, mock and test REST APIs in one place")
                .doesNotContain("out of 5", "Top rated", "Most reviewed", "name=\"sort\"");
        assertThat(html.indexOf("APIPilot")).isLessThan(html.indexOf("PixelPress"));

        var details = mvc.get().uri("/products/8").exchange();
        assertThat(details).hasStatusOk().hasViewName("product");
        assertThat(details.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .contains("APIPilot", "Draft an OpenAPI spec", "https://example.com/apipilot");
    }

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
