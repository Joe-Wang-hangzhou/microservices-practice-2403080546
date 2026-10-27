# 第六周：微服务拆分

验证日期：2026-10-27。个人项目为图书借阅系统，基础包 `com.zjgsu.wch`。原 `monolith/` 代码保留。

## 服务拆分设计

按业务能力拆为读者、图书、借阅三个服务，设计见 [microservices-design.md](../../microservices-design.md)，包含原模块映射、端口、数据库/账号、表归属、调用关系和 Mermaid 图。

| 服务 | 主机端口 | 独立数据库 | 拥有表 |
| --- | --- | --- | --- |
| reader-service | 18086 | reader_db | readers |
| book-service | 18087 | book_db | books、book_copies |
| loan-service | 18088 | loan_db | loans |

原单体只有 readerName，本周新增独立读者实体。图书与副本属于同一内聚边界，借阅记录仅保存 readerId 和 copyId。跨服务不建立外键、不直接访问对方表或 Repository。旧库保留，本周从新库创建演示数据，不实施历史数据自动迁移。

查看设计：`cat docs/microservices-design.md`。结论：三项业务能力独立部署、独立访问数据，通过 HTTP 协作。

## 代码拆分

各 `services/<服务名>/` 均为独立 Spring Boot 工程，包含 pom.xml、src/main、src/test、application.yml、Controller、Service、Repository 和健康接口。基础包分别为 com.zjgsu.wch.reader、com.zjgsu.wch.book、com.zjgsu.wch.loan。

图书的 Book、BookCopy 及 Repository 从单体迁移；原 LibraryService 的借还逻辑拆成图书副本状态管理和借阅流程编排。loan-service 的 LibraryClient 使用 RestTemplate：GET 校验读者，POST 修改图书副本状态；地址通过环境变量覆盖，连接超时 2000ms，读取超时 3000ms。依赖缺失返回 404，状态冲突返回 409，连接失败/超时/远端 5xx 返回 503，不跳过校验。

在仓库根目录运行：

```bash
mvn -B -f services/reader-service/pom.xml test
mvn -B -f services/book-service/pom.xml test
mvn -B -f services/loan-service/pom.xml test
```

实测 reader 2 项、book 2 项、loan 5 项，共 9 项通过，Failures/Errors 均为 0；另已执行 package，三个独立应用构建成功。读者和图书测试使用 H2；借阅测试通过 JDK HttpServer 建立真实 HTTP 连接，验证成功、缺失资源、依赖错误及连接拒绝，检查失败后本地数据未增加。

证据：[reader](evidence/reader-tests.log)、[book](evidence/book-tests.log)、[loan](evidence/loan-tests.log)。

## 本地编排

```bash
docker compose up -d --build
docker compose ps
```

根 compose.yaml 启动三个应用和三个 MySQL 8.4.11 数据库。每个数据库使用独立数据库名、非 root 账号和命名卷。应用通过 Compose 服务名访问数据库及其他服务，容器间不使用 localhost。数据库通过健康检查后启动对应应用。

保留原 Compose 项目名 library-week05，因而容器名前缀仍显示 library-week05；本周应用镜像标签为 week06，服务名和端口以当前 compose.yaml 为准。第五周原单体配置备份为 compose.week05.yaml，原 mysql_data 卷未删除。

实际结果：六个容器运行，三个数据库 healthy，三个应用端口分别为 18086、18087、18088，健康接口返回 UP。查询 CURRENT_USER、SHOW TABLES 和 SHOW GRANTS，确认 reader、book、loan 账号分别仅有自己数据库的业务表权限。

证据：[编排构建与启动](evidence/compose-start-final.log)、[容器状态](evidence/compose-status.log)、[数据库隔离](evidence/database-isolation.log)。初次构建遇到 Maven 下载 TLS 连接中断；复用本机已下载依赖预热 BuildKit 缓存后，重新构建成功。各 Dockerfile 使用 Maven 缓存挂载，仍在镜像内执行 package 和测试，无需提交本机缓存。

停止命令 `docker compose down` 保留数据卷，不使用 `down -v`。

## 验证

脚本可从仓库根目录直接执行：

```bash
./scripts/smoke-test.sh
# 附加验证依赖宕机，短暂停止 book-service，finally 中恢复
./scripts/smoke-test.sh --unavailable
```

脚本使用 Python 3 标准库发起 HTTP 请求、检查状态码和返回数据，可重复运行，每次创建新读者和唯一 ISBN。默认地址可用 READER_URL、BOOK_URL、LOAN_URL 覆盖。

首次完整实测对象：readerId=1、bookId=1、copyId=1、loanId=1。请求与数据变化如下：

| 请求 | 状态码 | 数据变化/断言 |
| --- | --- | --- |
| GET :18086、:18087、:18088 /actuator/health | 200 | 三项均 UP；启动阶段短暂 503 会轮询等待 |
| POST :18086/api/readers，name=王超浩 | 201 | readers 新增 1 条 |
| POST :18087/api/books，title=微服务拆分实践、唯一 ISBN | 201 | books 新增书目，book_copies 自动新增副本 |
| POST :18088/api/loans，readerId=1、copyId=1 | 201 | loans 从 0 变 1 条，远端副本 borrowed=true |
| 再次提交相同借阅 | 409 | loans 仍为 1 条 |
| POST :18088/api/loans/1/return | 200 | returnedAt=2026-10-27，副本 borrowed=false |
| POST /api/loans，readerId=9223372036854775807、copyId=1 | 404 | loans 仍为 1 条，副本仍可借 |
| POST /api/loans，readerId=1、copyId=9223372036854775807 | 404 | loans 仍为 1 条 |
| docker compose stop book-service 后提交 readerId=1、copyId=1 | 503 | loans 仍为 1 条，返回依赖不可用错误 |
| 重新启动 book-service 后检查 | 200 | UP，副本仍 borrowed=false |

完整日志：[smoke-test.log](evidence/smoke-test.log)，最终输出 `ALL SMOKE CHECKS PASSED`。脚本检查了跨服务成功和基本失败处理，没有把只通过单元测试等同于容器运行成功。

### 远端成功、本地失败窗口

借出顺序是远端图书服务先提交副本状态，再在借阅服务保存本地记录。如果远端成功后借阅库提交失败，本地 @Transactional 只能回滚本库，不能撤销远端状态。归还也存在副本先释放、本地记录更新失败的窗口。网络超时还可能出现远端已提交但调用方未收到响应。本周未实现跨服务原子事务、自动补偿、消息机制、并发抢借保障或重试幂等；符合本周顺序验证的任务范围。

## 手动截图与网站提交

截图由本人手动完成，当前未代生成或上传图片。将代码、编排和验证截图保存至 [screenshots/](screenshots/)，并分别上传第 12 题「代码拆分」「本地编排」「验证」附件栏；设计文本填写公开仓库完整链接和不少于 50 字说明。

本机详细指南：`/Users/Zhuanz/Desktop/大三上/微服务/提交材料06/填写与截图提交指南.md`。截图保存后需追加 Git 提交并推送，网站作答和最终提交仍需本人完成。第 11 题选课系统的独立服务位于提交材料06/课内实践/enrollment-service，不混入个人图书项目。
