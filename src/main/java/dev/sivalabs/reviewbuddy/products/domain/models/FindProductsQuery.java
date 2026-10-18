package dev.sivalabs.reviewbuddy.products.domain.models;

public record FindProductsQuery(String query, String categorySlug, String sort, int pageNo) {
    public static final String SORT_RATING = "rating";
    public static final String SORT_REVIEWS = "reviews";
    public static final String SORT_NEWEST = "new";

    public FindProductsQuery {
        query = query == null ? "" : query.strip();
        categorySlug = categorySlug == null ? "" : categorySlug.strip();
        sort = SORT_REVIEWS.equals(sort) || SORT_NEWEST.equals(sort) ? sort : SORT_RATING;
        pageNo = Math.max(pageNo, 1);
    }
}
