package dev.sivalabs.reviewbuddy.products.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import dev.sivalabs.reviewbuddy.BaseIT;
import dev.sivalabs.reviewbuddy.products.domain.models.ReviewDto;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Sql({"/test-data.sql", "/review-reading-data.sql"})
@Transactional
class ReviewReadingTests extends BaseIT {
    @Autowired
    UserDetailsService users;

    @Test
    void shouldMakeNewlySubmittedFeedbackPubliclyReadable() throws java.io.UnsupportedEncodingException {
        var submitted = mvc.post()
                .uri("/products/3/reviews")
                .with(csrf())
                .with(user(users.loadUserByUsername("demouser@gmail.com")))
                .param("liked", "Simple budget setup")
                .param("improvements", "Support more banks")
                .exchange();
        assertThat(submitted).hasRedirectedUrl("/products/3");

        var result = mvc.get().uri(submitted.getResponse().getRedirectedUrl()).exchange();
        assertThat(result).hasStatusOk().hasViewName("product");
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("Simple budget setup", "Support more banks")
                .doesNotContain("Demo User", "demouser@gmail.com", "No reviews yet.");
    }

    @Test
    void shouldShowAllIndividualFeedbackPubliclyForOnlyRequestedProduct() throws java.io.UnsupportedEncodingException {
        var result = mvc.get().uri("/products/8").exchange();
        assertThat(result).hasStatusOk().hasViewName("product");
        assertThat(result)
                .model()
                .containsEntry(
                        "reviews",
                        List.of(
                                new ReviewDto("Tie higher ID feedback", "Tie higher ID improvement"),
                                new ReviewDto("Tie lower ID feedback", "Tie lower ID improvement"),
                                new ReviewDto("Earlier feedback", "Earlier improvement")));
        var html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains(
                        "Liked",
                        "Can be improved",
                        "Earlier feedback",
                        "Earlier improvement",
                        "Tie higher ID feedback",
                        "Tie higher ID improvement",
                        "Tie lower ID feedback",
                        "Tie lower ID improvement")
                .doesNotContain(
                        "Other product feedback",
                        "Other product improvement",
                        "Demo User",
                        "Administrator",
                        "Anika Rao",
                        "demouser@gmail.com",
                        "2026-10-01",
                        "2026-10-02",
                        "No reviews yet.");
        assertThat(html.indexOf("Tie higher ID feedback")).isLessThan(html.indexOf("Tie lower ID feedback"));
        assertThat(html.indexOf("Tie lower ID feedback")).isLessThan(html.indexOf("Earlier feedback"));
        assertThat(html.split("aria-label=\"Product review\"", -1)).hasSize(4);
    }

    @Test
    void shouldLetMakerReadReviewsOfOwnProduct() throws java.io.UnsupportedEncodingException {
        var result = mvc.get()
                .uri("/products/8")
                .with(user(users.loadUserByUsername("elena@makers.example.invalid")))
                .exchange();
        assertThat(result).hasStatusOk().hasViewName("product");
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("Earlier feedback", "Earlier improvement", "Tie higher ID feedback");
    }

    @Test
    void shouldKeepProductWithoutReviewsPubliclyViewable() throws java.io.UnsupportedEncodingException {
        var result = mvc.get().uri("/products/3").exchange();
        assertThat(result).hasStatusOk().hasViewName("product");
        assertThat(result).model().containsEntry("reviews", List.of());
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("BudgetBee", "No reviews yet.")
                .doesNotContain("Earlier feedback", "Other product feedback", "aria-label=\"Product review\"");
    }

    @Test
    void shouldEscapeBothFeedbackFields() throws java.io.UnsupportedEncodingException {
        var result = mvc.get().uri("/products/2").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("&lt;script&gt;", "&lt;img")
                .doesNotContain("<script>alert(", "<img src=x");
    }
}
