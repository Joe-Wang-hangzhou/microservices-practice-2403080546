# 图书借阅系统数据库设计

数据库名 `library_db`，字符集 utf8mb4；Spring Data JPA 在首次启动时通过 `ddl-auto: update` 创建练习用表。

| 表 | 字段 | 主键及约束 |
| --- | --- | --- |
| books | id BIGINT、title VARCHAR(100)、author VARCHAR(100)、isbn VARCHAR(20) | id 自增主键；title/author/isbn 非空；isbn 唯一 |
| book_copies | id BIGINT、book_id BIGINT、borrowed BIT | id 自增主键；book_id 非空外键指向 books.id；borrowed 非空，表示是否已借出 |
| borrow_records | id BIGINT、copy_id BIGINT、reader_name VARCHAR(50)、borrowed_at DATE、due_at DATE、returned_at DATE | id 自增主键；copy_id 非空外键指向 book_copies.id；除 returned_at 外业务字段均非空 |

Book 1:N BookCopy，BookCopy 1:N BorrowRecord。归还日期为空表示借阅尚未结束。
读者姓名是借阅记录的普通字段，本周没有读者账户表。

借出时 Service 检查副本存在且未借出，在一个事务中将 borrowed 改为 true 并写入借阅记录，due_at 为 borrowed_at 加 30 天。
归还时检查记录存在且未归还，在一个事务中填写 returned_at 并将 borrowed 改为 false。
重复借出/归还返回 409，资源不存在返回 404，输入校验失败返回 400。

## 数据准备

应用第一次启动建表后执行：

```bash
mysql -u library -p library_db < monolith/scripts/init.sql
```

脚本使用固定示例 ID 1001 和 INSERT IGNORE，重复执行不会重复插入同一批记录，也不会覆盖已有借阅状态。
测试代码自行保存所需实体；Repository/Service 测试事务结束后回滚，H2 测试数据库按 create-drop 管理。

## 本次实际验证

2026-10-12：本机现有 MySQL 26.7.0，独立临时数据目录，端口 3307；未操作原有数据库。
课程示例推荐 MySQL 8.4.x，本次没有声称在 8.4.x 上运行验证。应用正常默认端口为 8080，实际验证临时使用 18084。
创建数据、借出、拒绝重复借出、归还和重启后查询均通过；初始化脚本连续导入两次，没有产生重复示例记录。
H2 自动测试不作为 MySQL 运行证明，MySQL 原始查询结果见 [运行记录](homework/week-04/evidence/database-rows.log)。
