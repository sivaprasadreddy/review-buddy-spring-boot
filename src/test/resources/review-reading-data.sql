insert into reviews(id, product_id, reviewer_id, liked, improvements, created_at) values
(1, 8, 1, 'Earlier feedback', 'Earlier improvement', '2026-10-01'),
(2, 8, 2, 'Tie lower ID feedback', 'Tie lower ID improvement', '2026-10-02'),
(3, 8, 11, 'Tie higher ID feedback', 'Tie higher ID improvement', '2026-10-02'),
(4, 1, 2, 'Other product feedback', 'Other product improvement', '2026-10-03'),
(5, 2, 2, '<script>alert("liked")</script>', '<img src=x onerror=alert("improve")>', '2026-10-04');
