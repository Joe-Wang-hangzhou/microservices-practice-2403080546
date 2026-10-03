# 第三周：Spring Boot 基础

## 1. 本周计划

- 沿用第 02 周的图书借阅系统选题，在 `docs/project-proposal.md` 中记录项目名称、目标用户、优先实现的“图书借出与归还”场景，以及 `Book`、`BorrowRecord` 两个核心模型。
- 在 `monolith/` 下用 Spring Initializr 创建 Maven 工程：Java 25、Spring Boot 4.0.8，Group 与 Package name 都是 `com.zjgsu.wch`，依赖 Spring Web MVC 和 Actuator。
- 提供 `GET /api/hello` 接口，并确认 `/actuator/health` 返回 `UP`。
- 保留 `@SpringBootTest` 的 `contextLoads` 测试，确认 `./mvnw test` 通过。
- 本周不做业务建模、Service、Repository 和数据库。

## 2. 创建并运行 Spring Boot 工程

工程结构：

```text
monolith/
├── .mvn/wrapper/maven-wrapper.properties
├── mvnw / mvnw.cmd
├── pom.xml
└── src
    ├── main
    │   ├── java/com/zjgsu/wch
    │   │   ├── LibraryMonolithApplication.java      # 启动类
    │   │   └── controller/HelloController.java      # GET /api/hello
    │   └── resources/application.yml                # 端口 8080、应用名、Actuator 暴露
    └── test/java/com/zjgsu/wch
        └── LibraryMonolithApplicationTests.java     # contextLoads
```

启动命令：

```bash
cd monolith
./mvnw spring-boot:run
```

接口响应：

```bash
curl http://localhost:8080/api/hello
```

```json
{"project":"图书借阅系统","application":"library-monolith","message":"Hello, library borrowing system!"}
```

```bash
curl http://localhost:8080/actuator/health
```

```json
{"groups":["liveness","readiness"],"status":"UP"}
```

截图：

![项目结构与配置](screenshots/01-project-structure.png)
![应用启动日志](screenshots/02-app-running.png)
![接口响应](screenshots/03-api-response.png)

## 3. 启动测试

测试代码 `monolith/src/test/java/com/zjgsu/wch/LibraryMonolithApplicationTests.java`：

```java
@SpringBootTest
class LibraryMonolithApplicationTests {

	@Test
	void contextLoads() {
	}

}
```

执行：

```bash
cd monolith
./mvnw test
```

结果：`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`，Spring 应用上下文可以正常加载。

![测试代码](screenshots/04-test-code.png)
![测试通过](screenshots/05-test-success.png)

## 4. 本周完成内容

1. 新增 `docs/project-proposal.md`，确定优先场景和两个核心模型。
2. 在 `monolith/` 下创建 Spring Boot 4.0.8 + Maven Wrapper 工程，配置统一使用 `application.yml`。
3. 实现 `GET /api/hello`，开放 Actuator 的 `health`、`info` 端点，健康检查返回 `UP`。
4. `contextLoads` 启动测试通过。
5. 更新根目录 README，补充运行环境、启动与测试命令、访问地址和尚未实现的业务能力。
