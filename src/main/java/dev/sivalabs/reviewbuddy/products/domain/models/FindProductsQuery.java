package dev.sivalabs.reviewbuddy.products.domain.models;

public record FindProductsQuery(String query, String categorySlug, int pageNo) {
    public FindProductsQuery {
        query = query == null ? "" : query.strip();
        categorySlug = categorySlug == null ? "" : categorySlug.strip();
        pageNo = Math.max(pageNo, 1);
    }
}
