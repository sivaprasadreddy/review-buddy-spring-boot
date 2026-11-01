package dev.sivalabs.reviewbuddy.products.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import dev.sivalabs.reviewbuddy.BaseIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@Sql("/test-data.sql")
@Transactional
class ProductSubmissionTests extends BaseIT {
    @Autowired
    UserDetailsService userDetailsService;

    private MockMvcRequestBuilder submission() {
        return submission("", "");
    }

    private MockMvcRequestBuilder submission(String field, String value) {
        var request = mvc.post()
                .uri("/products/submit")
                .with(csrf())
                .with(user(userDetailsService.loadUserByUsername("demouser@gmail.com")));
        var fields = java.util.Map.of(
                "name",
                "New Product",
                "tagline",
                "Useful software",
                "description",
                "A helpful tool for makers",
                "categorySlug",
                "developer-tools",
                "url",
                "https://example.com/new-product",
                "imageUrl",
                "");
        fields.forEach((key, defaultValue) -> request.param(key, key.equals(field) ? value : defaultValue));
        return request;
    }

    @Test
    void shouldRequireLoginForFormAndSubmission() {
        assertThat(mvc.get().uri("/products/submit").exchange()).hasStatus3xxRedirection();
        assertThat(mvc.post().uri("/products/submit").with(csrf()).exchange()).hasStatus3xxRedirection();
    }

    @Test
    void shouldShowFormWithCategories() throws java.io.UnsupportedEncodingException {
        var result = mvc.get()
                .uri("/products/submit")
                .with(user(userDetailsService.loadUserByUsername("demouser@gmail.com")))
                .exchange();
        assertThat(result).hasStatusOk().hasViewName("submit-product");
        assertThat(result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .contains("Developer Tools", "_csrf", "maxlength=\"60\"");
    }

    @Test
    void shouldPersistMultipleProductsAndDisplayPubliclyWithAuthenticatedMaker()
            throws java.io.UnsupportedEncodingException {
        var first = submission().param("maker", "Forged maker").exchange();
        var second = submission().exchange();
        assertThat(first).hasStatus3xxRedirection();
        assertThat(second).hasStatus3xxRedirection();
        assertThat(first.getResponse().getRedirectedUrl())
                .isNotEqualTo(second.getResponse().getRedirectedUrl());
        var publicPage = mvc.get().uri(first.getResponse().getRedirectedUrl()).exchange();
        assertThat(publicPage).hasStatusOk().hasViewName("product");
        assertThat(publicPage.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .contains("New Product", "Demo User", "https://example.com/new-product")
                .doesNotContain("Forged maker");
        var listing = mvc.get().uri("/products").param("q", "New Product").exchange();
        assertThat(listing.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .contains("New Product", "href=\"" + first.getResponse().getRedirectedUrl() + "\"");
    }

    @Test
    void shouldLetMakerAndVisitorViewNewProductBeforeAnyReviewsExist() throws java.io.UnsupportedEncodingException {
        var submitted = submission().exchange();
        assertThat(submitted).hasStatus3xxRedirection();
        var publicUrl = submitted.getResponse().getRedirectedUrl();

        var makerView = mvc.get()
                .uri(publicUrl)
                .with(user(userDetailsService.loadUserByUsername("demouser@gmail.com")))
                .exchange();
        var visitorView = mvc.get().uri(publicUrl).exchange();

        assertThat(makerView).hasStatusOk().hasViewName("product");
        assertThat(visitorView).hasStatusOk().hasViewName("product");
        for (var view : java.util.List.of(makerView, visitorView)) {
            assertThat(view.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                    .contains(
                            "New Product",
                            "Useful software",
                            "A helpful tool for makers",
                            "Demo User",
                            "https://example.com/new-product");
        }
    }

    @ParameterizedTest
    @CsvSource({"name,61", "tagline,101", "description,501"})
    void shouldRejectOverlongFields(String field, int length) {
        var result = submission(field, "x".repeat(length)).exchange();
        assertThat(result)
                .hasViewName("submit-product")
                .model()
                .extractingBindingResult("product")
                .hasFieldErrors(field);
    }

    @ParameterizedTest
    @CsvSource({
        "name,''",
        "tagline,''",
        "description,' '",
        "categorySlug,missing",
        "url,javascript:alert(1)",
        "imageUrl,invalid"
    })
    void shouldRejectInvalidFields(String field, String value) {
        var result = submission(field, value).exchange();
        assertThat(result)
                .hasViewName("submit-product")
                .model()
                .extractingBindingResult("product")
                .hasFieldErrors(field);
    }

    @ParameterizedTest
    @CsvSource({"name,60", "tagline,100", "description,500"})
    void shouldAcceptMaximumFieldLengths(String field, int length) {
        assertThat(submission(field, "x".repeat(length)).exchange()).hasStatus3xxRedirection();
    }

    @Test
    void shouldRequireCsrfToken() {
        var result = mvc.post()
                .uri("/products/submit")
                .with(user(userDetailsService.loadUserByUsername("demouser@gmail.com")))
                .exchange();
        assertThat(result).hasStatus(403);
    }

    @Test
    void shouldReturnNotFoundForUnknownProduct() {
        assertThat(mvc.get().uri("/products/999999").exchange()).hasStatus(404);
    }
}
