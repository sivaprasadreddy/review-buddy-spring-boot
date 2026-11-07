package dev.sivalabs.reviewbuddy.products.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import dev.sivalabs.reviewbuddy.BaseIT;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Sql({"/test-data.sql", "/review-reading-data.sql", "/summary-reading-data.sql"})
@Transactional
class ReviewSummaryReadingTests extends BaseIT {
    @Autowired
    UserDetailsService users;

    @Test
    void shouldShowOnlyRequestedProductsSummaryAndIndividualReviewsPublicly() throws Exception {
        var result = mvc.get().uri("/products/8").exchange();
        assertThat(result).hasStatusOk().hasViewName("product");
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains(
                        "AI-generated review summary",
                        "Liked",
                        "Can be improved",
                        "Clear API workflows",
                        "Improve API documentation",
                        "Earlier feedback",
                        "Earlier improvement",
                        "Tie higher ID feedback",
                        "Tie lower ID feedback",
                        "href=\"#reviews-heading\"")
                .doesNotContain(
                        "Other product summary",
                        "Other product suggestions",
                        "Other product feedback",
                        "AI summary not available yet");
        var other = mvc.get().uri("/products/1").exchange();
        assertThat(other).hasStatusOk();
        assertThat(other.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("Other product summary", "Other product suggestions", "Other product feedback")
                .doesNotContain("Clear API workflows", "Improve API documentation");
    }

    @Test
    void shouldLetMakerReadOwnProductsSummary() throws Exception {
        var result = mvc.get()
                .uri("/products/8")
                .with(user(users.loadUserByUsername("elena@makers.example.invalid")))
                .exchange();
        assertThat(result).hasStatusOk();
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains(
                        "AI-generated review summary",
                        "Clear API workflows",
                        "Improve API documentation",
                        "Earlier feedback");
    }

    @Test
    @Sql({"/test-data.sql", "/review-reading-data.sql"})
    void shouldKeepReviewsVisibleWhenSummaryIsUnavailable() throws Exception {
        var result = mvc.get().uri("/products/8").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains(
                        "AI summary not available yet",
                        "Earlier feedback",
                        "Earlier improvement",
                        "Tie higher ID feedback",
                        "Tie lower ID feedback");
    }

    @Test
    void shouldShowUnavailableMessageForProductWithoutReviews() throws Exception {
        var result = mvc.get().uri("/products/3").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("AI summary not available yet", "No reviews yet.")
                .doesNotContain("Clear API workflows", "Other product summary");
    }

    @Test
    void shouldEscapeBothSummaryFeedbackAreas() throws Exception {
        var result = mvc.get().uri("/products/2").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("&lt;script&gt;", "&lt;img")
                .doesNotContain("<script>alert(", "<img src=x");
    }
}
