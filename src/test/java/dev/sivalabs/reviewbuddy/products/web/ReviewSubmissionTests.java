package dev.sivalabs.reviewbuddy.products.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import dev.sivalabs.reviewbuddy.BaseIT;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Sql("/test-data.sql")
@Transactional
class ReviewSubmissionTests extends BaseIT {
    @Autowired
    UserDetailsService users;

    @Autowired
    JdbcClient jdbc;

    private MockMvcRequestBuilder review(long productId, String email, String liked, String improvements) {
        return mvc.post()
                .uri("/products/" + productId + "/reviews")
                .with(csrf())
                .with(user(users.loadUserByUsername(email)))
                .param("liked", liked)
                .param("improvements", improvements);
    }

    private long reviewCount() {
        return jdbc.sql("select count(*) from reviews").query(Long.class).single();
    }

    @Test
    void shouldRequireLoginAndCsrf() {
        assertThat(mvc.get().uri("/products/8/reviews/new").exchange()).hasStatus3xxRedirection();
        assertThat(mvc.post().uri("/products/8/reviews").with(csrf()).exchange())
                .hasStatus3xxRedirection();
        assertThat(mvc.post()
                        .uri("/products/8/reviews")
                        .with(user(users.loadUserByUsername("demouser@gmail.com")))
                        .param("liked", "Helpful")
                        .param("improvements", "More docs")
                        .exchange())
                .hasStatus(403);
        assertThat(reviewCount()).isZero();
    }

    @Test
    void shouldShowProductAndTwoFeedbackFields() throws java.io.UnsupportedEncodingException {
        var result = mvc.get()
                .uri("/products/8/reviews/new")
                .with(user(users.loadUserByUsername("demouser@gmail.com")))
                .exchange();
        assertThat(result).hasStatusOk().hasViewName("submit-review");
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("Review APIPilot", "name=\"liked\"", "name=\"improvements\"", "maxlength=\"400\"", "_csrf")
                .doesNotContain("name=\"rating\"");
    }

    @Test
    void shouldPersistSeparateFeedbackAndTrustedReviewerForMultipleReviewers() {
        assertThat(review(8, "demouser@gmail.com", "Useful mocks", "More examples")
                        .param("reviewerId", "1")
                        .param("productId", "1")
                        .exchange())
                .hasRedirectedUrl("/products/8");
        assertThat(review(8, "admin@gmail.com", "Fast setup", "Better docs").exchange())
                .hasRedirectedUrl("/products/8");
        assertThat(reviewCount()).isEqualTo(2);
        var saved = jdbc.sql("select product_id, reviewer_id, liked, improvements from reviews where reviewer_id = 2")
                .query()
                .singleRow();
        assertThat(saved)
                .containsEntry("product_id", 8L)
                .containsEntry("reviewer_id", 2L)
                .containsEntry("liked", "Useful mocks")
                .containsEntry("improvements", "More examples");
        assertThat(jdbc.sql("select count(*) from reviews where product_id <> 8")
                        .query(Long.class)
                        .single())
                .isZero();
    }

    @ParameterizedTest
    @CsvSource({"liked,''", "liked,'   '", "improvements,''", "improvements,'   '"})
    void shouldRetainInputAndRejectBlankFeedback(String field, String value)
            throws java.io.UnsupportedEncodingException {
        var liked = field.equals("liked") ? value : "Useful mocks";
        var improvements = field.equals("improvements") ? value : "More examples";
        var result = review(8, "demouser@gmail.com", liked, improvements).exchange();
        assertThat(result)
                .hasStatusOk()
                .hasViewName("submit-review")
                .model()
                .extractingBindingResult("review")
                .hasFieldErrors(field);
        assertThat(result).model().containsEntry("review", new SubmitReviewRequest(liked, improvements));
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("must not be blank");
        assertThat(reviewCount()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"liked", "improvements"})
    void shouldRejectOverlongFeedback(String field) {
        var result = review(
                        8,
                        "demouser@gmail.com",
                        field.equals("liked") ? "x".repeat(401) : "Helpful",
                        field.equals("improvements") ? "x".repeat(401) : "More docs")
                .exchange();
        assertThat(result)
                .hasViewName("submit-review")
                .model()
                .extractingBindingResult("review")
                .hasFieldErrors(field);
        assertThat(reviewCount()).isZero();
    }

    @Test
    void shouldAcceptMaximumFeedbackLengthsAndBlockRepeats() {
        assertThat(review(8, "demouser@gmail.com", "x".repeat(400), "y".repeat(400))
                        .exchange())
                .hasRedirectedUrl("/products/8");
        assertThat(review(8, "demouser@gmail.com", "Again", "Again").exchange()).hasStatus(403);
        assertThat(mvc.get()
                        .uri("/products/8/reviews/new")
                        .with(user(users.loadUserByUsername("demouser@gmail.com")))
                        .exchange())
                .hasStatus(403);
        assertThat(reviewCount()).isEqualTo(1);
    }

    @Test
    void shouldBlockSeedMakerSelfReviewUsingMigratedOwnership() {
        assertThat(jdbc.sql("select maker_id from products where id = 8")
                        .query(Long.class)
                        .single())
                .isEqualTo(18L);
        assertThat(review(8, "elena@makers.example.invalid", "Great", "Nothing").exchange())
                .hasStatus(403);
        assertThat(mvc.get()
                        .uri("/products/8/reviews/new")
                        .with(user(users.loadUserByUsername("elena@makers.example.invalid")))
                        .exchange())
                .hasStatus(403);
        assertThat(reviewCount()).isZero();
    }

    @Test
    void shouldBlockNewProductSelfReviewAndLetItsMakerReviewAnotherProduct() {
        var created = mvc.post()
                .uri("/products/submit")
                .with(csrf())
                .with(user(users.loadUserByUsername("demouser@gmail.com")))
                .param("name", "Maker tool")
                .param("tagline", "Helpful tool")
                .param("description", "Made for makers")
                .param("categorySlug", "developer-tools")
                .param("url", "https://example.com")
                .param("imageUrl", "")
                .exchange();
        assertThat(created).hasStatus3xxRedirection();
        var id = Long.parseLong(created.getResponse().getRedirectedUrl().replace("/products/", ""));
        assertThat(review(id, "demouser@gmail.com", "Great", "Nothing").exchange())
                .hasStatus(403);
        assertThat(review(8, "demouser@gmail.com", "Great", "More docs").exchange())
                .hasRedirectedUrl("/products/8");
        assertThat(reviewCount()).isEqualTo(1);
    }

    @Test
    void shouldRejectMissingProduct() {
        assertThat(review(999999, "demouser@gmail.com", "Great", "More docs").exchange())
                .hasStatus(404);
        assertThat(mvc.get()
                        .uri("/products/999999/reviews/new")
                        .with(user(users.loadUserByUsername("demouser@gmail.com")))
                        .exchange())
                .hasStatus(404);
        assertThat(reviewCount()).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Sql(statements = "delete from reviews where product_id = 8", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldAllowOnlyOneConcurrentSubmissionFromSameReviewer()
            throws InterruptedException, java.util.concurrent.ExecutionException,
                    java.util.concurrent.TimeoutException {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            java.util.concurrent.Callable<Integer> submit = () -> {
                ready.countDown();
                start.await();
                return review(8, "demouser@gmail.com", "Useful", "More docs")
                        .exchange()
                        .getResponse()
                        .getStatus();
            };
            var first = executor.submit(submit);
            var second = executor.submit(submit);
            assertThat(ready.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(java.util.List.of(
                            first.get(15, java.util.concurrent.TimeUnit.SECONDS),
                            second.get(15, java.util.concurrent.TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(302, 403);
        }
        assertThat(reviewCount()).isEqualTo(1);
    }
}
