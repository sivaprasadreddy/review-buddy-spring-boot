create sequence category_id_seq start with 100 increment by 50;

create table categories
(
    id         bigint    not null default nextval('category_id_seq'),
    name       text      not null,
    slug       text      not null,
    created_at timestamp not null default CURRENT_TIMESTAMP,
    updated_at timestamp,
    version    bigint    not null default 0,
    primary key (id),
    constraint category_name_unique unique (name),
    constraint category_slug_unique unique (slug)
);

insert into categories(id, name, slug) values
(1, 'Developer Tools', 'developer-tools'),
(2, 'Productivity', 'productivity'),
(3, 'Finance', 'finance'),
(4, 'Education', 'education'),
(5, 'Design', 'design'),
(6, 'Lifestyle', 'lifestyle');

create sequence product_id_seq start with 100 increment by 50;

create table products
(
    id          bigint    not null default nextval('product_id_seq'),
    name        text      not null,
    tagline     text      not null,
    description text      not null,
    category_id bigint    not null references categories (id),
    maker       text      not null,
    url         text      not null,
    image_url   text,
    created_at  timestamp not null default CURRENT_TIMESTAMP,
    updated_at  timestamp,
    version     bigint    not null default 0,
    primary key (id)
);

create index products_category_id_idx on products (category_id);

insert into products(id, name, tagline, description, category_id, maker, url, image_url, created_at) values
(1, 'TaskNest', 'A calm to-do list that plans your day for you', 'TaskNest turns your messy task list into a realistic daily plan. It estimates effort, spots overload and moves work to days that fit.', 2, 'Anika Rao', 'https://example.com/tasknest', 'https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?auto=format&fit=crop&w=900&q=70', '2026-08-02'),
(2, 'CodeLens', 'Understand any codebase in minutes', 'CodeLens maps modules, dependencies and hot spots in a repository so new team members can find their way around on day one.', 1, 'Marcus Lee', 'https://example.com/codelens', 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=900&q=70', '2026-07-21'),
(3, 'BudgetBee', 'Budgeting for people who hate budgeting', 'Connect your accounts, set one monthly limit, and BudgetBee nudges you only when something looks off.', 3, 'Sofia Alvarez', 'https://example.com/budgetbee', 'https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=900&q=70', '2026-08-15'),
(4, 'LingoLoop', 'Learn a language with five-minute conversations', 'Short, spoken practice sessions with an AI partner that adapts to your level and remembers the words you struggle with.', 4, 'Tomás Greene', 'https://example.com/lingoloop', 'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=900&q=70', '2026-06-30'),
(5, 'PixelPress', 'Social images from a single sentence', 'Describe the post, pick a brand palette, and get ready-to-publish images in every social format.', 5, 'Hana Kim', 'https://example.com/pixelpress', 'https://images.unsplash.com/photo-1558655146-9f40138edfeb?auto=format&fit=crop&w=900&q=70', '2026-09-10'),
(6, 'MealMap', 'Weekly meal plans built from what''s in your fridge', 'Scan your fridge, get a week of recipes that reduce waste, and a shopping list for the gaps.', 6, 'Priya Nair', 'https://example.com/mealmap', 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=900&q=70', '2026-05-18'),
(7, 'FocusFlow', 'Deep work timer with distraction blocking', 'Run focused sessions, block distracting sites, and see weekly reports on when you do your best work.', 2, 'Daniel Okafor', 'https://example.com/focusflow', 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=900&q=70', '2026-04-09'),
(8, 'APIPilot', 'Design, mock and test REST APIs in one place', 'Draft an OpenAPI spec, get a live mock server immediately, and generate contract tests from the same file.', 1, 'Elena Petrova', 'https://example.com/apipilot', 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=900&q=70', '2026-09-25');
