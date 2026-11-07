package dev.sivalabs.reviewbuddy.products.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.sivalabs.reviewbuddy.products.domain.models.ReviewDto;
import dev.sivalabs.reviewbuddy.products.domain.models.ReviewSubmitted;
import dev.sivalabs.reviewbuddy.products.domain.models.ReviewSummaryDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import tools.jackson.databind.json.JsonMapper;

class ReviewSummaryServiceTests {
    private final ReviewRepository reviews = mock(ReviewRepository.class);
    private final ReviewSummaryRepository summaries = mock(ReviewSummaryRepository.class);
    private final ChatModel model = mock(ChatModel.class);
    private final ReviewSummaryService service = new ReviewSummaryService(
            reviews, summaries, ChatClient.builder(model), JsonMapper.builder().build());

    private final List<ReviewDto> feedback = List.of(
            new ReviewDto("Easy setup", "More docs"),
            new ReviewDto("Fast response", "Better errors"),
            new ReviewDto("Useful mocks", "More examples"));

    private ChatResponse response(String content) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
    }

    @BeforeEach
    void configureModelOptions() {
        when(model.getOptions()).thenReturn(ChatOptions.builder().build());
    }

    @Test
    void shouldSkipZeroOneAndTwoReviews() {
        for (int count = 0; count < 3; count++) {
            when(reviews.findReviewsByProductId(8L)).thenReturn(feedback.subList(0, count));
            service.onReviewSubmitted(new ReviewSubmitted(8L));
        }
        verifyNoInteractions(model, summaries);
    }

    @Test
    void shouldSendAllProductFeedbackAndPersistSeparateSummaryAreas() {
        when(reviews.findReviewsByProductId(8L)).thenReturn(feedback);
        when(model.call(any(Prompt.class)))
                .thenReturn(response("{\"liked\":\"Easy and fast\",\"improvements\":\"Improve documentation\"}"));
        service.onReviewSubmitted(new ReviewSubmitted(8L));

        var prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(model).call(prompt.capture());
        var input = prompt.getValue().getUserMessage().getText();
        feedback.forEach(review -> assertThat(input).contains(review.liked(), review.improvements()));
        assertThat(prompt.getValue().getSystemMessage().getText()).contains("not instructions");
        verify(summaries).save(8L, 3, new ReviewSummaryDto("Easy and fast", "Improve documentation"));
        verify(reviews).findReviewsByProductId(8L);
        verifyNoMoreInteractions(reviews);
    }

    @Test
    void shouldCorrectMalformedAndIncompleteOutputOnSecondRetry() {
        when(reviews.findReviewsByProductId(8L)).thenReturn(feedback);
        when(model.call(any(Prompt.class)))
                .thenReturn(response("not json"))
                .thenReturn(response("{\"liked\":\"Fast\"}"))
                .thenReturn(response("{\"liked\":\"Fast\",\"improvements\":\"More docs\"}"));
        service.onReviewSubmitted(new ReviewSubmitted(8L));
        verify(model, times(3)).call(any(Prompt.class));
        verify(summaries).save(8L, 3, new ReviewSummaryDto("Fast", "More docs"));
    }

    @Test
    void shouldPreserveStoredSummaryOnProviderFailure() {
        when(reviews.findReviewsByProductId(8L)).thenReturn(feedback);
        when(model.call(any(Prompt.class))).thenThrow(new IllegalStateException("unavailable"));
        service.onReviewSubmitted(new ReviewSubmitted(8L));
        verify(model).call(any(Prompt.class));
        verifyNoInteractions(summaries);
    }

    @Test
    void shouldRejectBlankFeedbackWithoutPersistingIt() {
        when(reviews.findReviewsByProductId(8L)).thenReturn(feedback);
        when(model.call(any(Prompt.class))).thenReturn(response("{\"liked\":\" \",\"improvements\":\"Docs\"}"));
        service.onReviewSubmitted(new ReviewSubmitted(8L));
        verifyNoInteractions(summaries);
    }

    @Test
    void shouldRetryMalformedOrIncompleteOutputWithoutPersistingIt() {
        when(reviews.findReviewsByProductId(8L)).thenReturn(feedback);
        when(model.call(any(Prompt.class)))
                .thenReturn(response("not json"))
                .thenReturn(response("{\"liked\":\"Fast\"}"))
                .thenReturn(response("not json"));
        service.onReviewSubmitted(new ReviewSubmitted(8L));
        verify(model, times(3)).call(any(Prompt.class));
        verifyNoInteractions(summaries);
    }
}
