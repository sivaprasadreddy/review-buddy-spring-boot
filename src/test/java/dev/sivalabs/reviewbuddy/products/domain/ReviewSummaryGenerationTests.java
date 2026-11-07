package dev.sivalabs.reviewbuddy.products.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import dev.sivalabs.reviewbuddy.BaseIT;
import dev.sivalabs.reviewbuddy.products.domain.models.ReviewSummaryDto;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

@Sql("/test-data.sql")
@Sql(
        statements = {"delete from review_summaries", "delete from reviews"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ReviewSummaryGenerationTests extends BaseIT {
    @MockitoBean
    ChatModel model;

    @Autowired
    UserDetailsService users;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    ReviewSummaryRepository summaries;

    @Autowired
    TransactionTemplate transactions;

    private void submit(long productId, String email, String liked) {
        assertThat(mvc.post()
                        .uri("/products/" + productId + "/reviews")
                        .with(csrf())
                        .with(user(users.loadUserByUsername(email)))
                        .param("liked", liked)
                        .param("improvements", "More documentation")
                        .exchange())
                .hasRedirectedUrl("/products/" + productId);
    }

    private long summaryCount() {
        return jdbc.sql("select count(*) from review_summaries")
                .query(Long.class)
                .single();
    }

    @BeforeEach
    void configureModelOptions() {
        when(model.getOptions()).thenReturn(ChatOptions.builder().build());
    }

    @Test
    void shouldGenerateAfterThirdCommittedReviewAndRefreshAfterFourth() {
        when(model.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(
                        "{\"liked\":\"Fast and useful\",\"improvements\":\"More documentation\"}")))));
        submit(1, "demouser@gmail.com", "Other product feedback");
        submit(8, "demouser@gmail.com", "Easy setup");
        submit(8, "admin@gmail.com", "Fast response");
        assertThat(summaryCount()).isZero();

        submit(8, "anika@makers.example.invalid", "Useful mocks");
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(summaryCount()).isEqualTo(1);
            assertThat(jdbc.sql("select review_count from review_summaries where product_id = 8")
                            .query(Integer.class)
                            .single())
                    .isEqualTo(3);
        });
        var prompt = org.mockito.ArgumentCaptor.forClass(Prompt.class);
        verify(model, atLeastOnce()).call(prompt.capture());
        assertThat(prompt.getValue().getUserMessage().getText())
                .contains("Easy setup", "Fast response", "Useful mocks", "More documentation")
                .doesNotContain("Other product feedback", "demouser@gmail.com");

        submit(8, "marcus@makers.example.invalid", "Helpful UI");
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () -> assertThat(jdbc.sql("select review_count from review_summaries where product_id = 8")
                                        .query(Integer.class)
                                        .single())
                                .isEqualTo(4));

        summaries.save(8L, 3, new ReviewSummaryDto("Stale feedback", "Stale improvements"));
        assertThat(jdbc.sql("select liked from review_summaries where product_id = 8")
                        .query(String.class)
                        .single())
                .isEqualTo("Fast and useful");
        assertThat(jdbc.sql("select improvements from review_summaries where product_id = 8")
                        .query(String.class)
                        .single())
                .isEqualTo("More documentation");

        clearInvocations(model);
        when(model.call(any(Prompt.class))).thenThrow(new IllegalStateException("Provider unavailable"));
        submit(8, "sofia@makers.example.invalid", "Clear navigation");
        verify(model, timeout(5000).times(1)).call(any(Prompt.class));
        assertThat(jdbc.sql("select review_count from review_summaries where product_id = 8")
                        .query(Integer.class)
                        .single())
                .isEqualTo(4);
        assertThat(jdbc.sql("select liked from review_summaries where product_id = 8")
                        .query(String.class)
                        .single())
                .isEqualTo("Fast and useful");
    }

    @Test
    void shouldNotGenerateForRolledBackReview() {
        transactions.executeWithoutResult(status -> {
            submit(8, "demouser@gmail.com", "One");
            submit(8, "admin@gmail.com", "Two");
            submit(8, "anika@makers.example.invalid", "Three");
            status.setRollbackOnly();
        });
        assertThat(jdbc.sql("select count(*) from reviews").query(Long.class).single())
                .isZero();
        assertThat(summaryCount()).isZero();
        verifyNoInteractions(model);
    }
}
