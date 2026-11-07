package dev.sivalabs.reviewbuddy.products.domain.models;

public record ReviewSummaryDto(String liked, String improvements) {
    public boolean hasBothFeedbackAreas() {
        return liked != null && !liked.isBlank() && improvements != null && !improvements.isBlank();
    }
}
