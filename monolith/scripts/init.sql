-- Run after the first application startup creates the tables. Repeated imports do not duplicate these IDs.
SET NAMES utf8mb4;
INSERT IGNORE INTO books (id, title, author, isbn) VALUES (1001, '微服务入门', '课程示例作者', '9780000001001');
INSERT IGNORE INTO book_copies (id, book_id, borrowed) VALUES (1001, 1001, false);
