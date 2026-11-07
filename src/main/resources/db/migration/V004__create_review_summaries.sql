create table review_summaries (
    product_id bigint primary key references products(id),
    review_count integer not null check (review_count >= 3),
    liked text not null check (length(trim(liked)) > 0),
    improvements text not null check (length(trim(improvements)) > 0),
    generated_at timestamp not null default CURRENT_TIMESTAMP
);
