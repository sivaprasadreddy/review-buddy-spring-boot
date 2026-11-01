-- Seed makers have dedicated accounts; invalid password hashes prevent demo-account login.
insert into users(id, email, password, name, role) values
(11, 'anika@makers.example.invalid', '!', 'Anika Rao', 'ROLE_USER'),
(12, 'marcus@makers.example.invalid', '!', 'Marcus Lee', 'ROLE_USER'),
(13, 'sofia@makers.example.invalid', '!', 'Sofia Alvarez', 'ROLE_USER'),
(14, 'tomas@makers.example.invalid', '!', 'Tomás Greene', 'ROLE_USER'),
(15, 'hana@makers.example.invalid', '!', 'Hana Kim', 'ROLE_USER'),
(16, 'priya@makers.example.invalid', '!', 'Priya Nair', 'ROLE_USER'),
(17, 'daniel@makers.example.invalid', '!', 'Daniel Okafor', 'ROLE_USER'),
(18, 'elena@makers.example.invalid', '!', 'Elena Petrova', 'ROLE_USER');

alter table products add column maker_id bigint references users(id);
update products set maker_id = id + 10 where id between 1 and 8;
-- Any additional pre-existing products require an explicit ownership mapping before migration.
alter table products alter column maker_id set not null;

create sequence review_id_seq start with 100 increment by 50;
create table reviews (
    id bigint primary key default nextval('review_id_seq'),
    product_id bigint not null references products(id),
    reviewer_id bigint not null references users(id),
    liked varchar(400) not null check (length(trim(liked)) > 0),
    improvements varchar(400) not null check (length(trim(improvements)) > 0),
    created_at timestamp not null default CURRENT_TIMESTAMP,
    updated_at timestamp,
    version bigint not null default 0,
    constraint review_product_reviewer_unique unique (product_id, reviewer_id)
);
