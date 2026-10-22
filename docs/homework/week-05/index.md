# 第五周：Docker 容器化

沿用图书借阅系统与 `monolith/`，验证日期为 2026-10-22。基础包名仍为 `com.zjgsu.wch`。

## 镜像构建

`monolith/Dockerfile` 使用 Maven + Temurin JDK 25 编译、测试和打包，再将可执行 JAR 复制到 Temurin 25 JRE 运行镜像。
运行镜像不携带 Maven 和源码。`.dockerignore` 排除本机构建产物、IDE 配置和密码文件。

```bash
docker build --progress=plain -t library-monolith:week05 ./monolith
docker image ls library-monolith:week05
```

构建阶段实际执行原有 10 项测试，全部通过，见 [测试摘要](evidence/tests.log)。
首次下载较慢，切换本机代理后继续构建并复用已成功的 Maven 构建层。

## Compose 配置

根目录 `compose.yaml` 包含 app 与 MySQL 8.4.11 两个服务，不含已废弃的 version 字段。
app 的构建上下文为 `./monolith`，默认端口映射为 `18085:8080`。
两个服务共享 backend 网络，数据库主机名使用服务名 mysql；MySQL 不额外向宿主机映射端口。
通过 DB_URL、DB_USER、DB_PASSWORD 注入连接信息；`.env.example` 是占位模板，真实 `.env` 不提交。
MySQL 使用健康检查，app 的 depends_on 指定 service_healthy。
命名卷 mysql_data 挂载到 MySQL 的 `/var/lib/mysql`。

```bash
# 新环境首次配置；已有 .env 时跳过复制
cp .env.example .env
# 修改 .env 中的两个密码占位符
docker compose up -d --build
docker compose ps
docker compose logs --tail=30 app
```

## 业务 API 与数据持久化

验证健康接口与创建、查询业务接口：

```bash
curl -i http://localhost:18085/actuator/health
curl -i -X POST http://localhost:18085/api/books \
  -H 'Content-Type: application/json' \
  -d '{"title":"Docker容器化实践","author":"王超浩","isbn":"W05-20261022"}'
curl -i http://localhost:18085/api/books
# 将下方 1 替换成创建响应中的真实 ID
curl -i http://localhost:18085/api/books/1
docker compose down
docker compose up -d
# 等待健康接口返回 UP，再查询原 ID
curl -i http://localhost:18085/api/books/1
```

重复创建相同 ISBN 会返回 409；再次演示时使用新的 ISBN。
普通 down 删除容器与网络但保留命名数据卷，重建的 MySQL 容器重新挂载原数据目录。
本项不执行 `down -v`，以免删除数据卷。

## 本机实际验证结果

- 镜像 `library-monolith:week05` 构建成功；见 [最终构建日志](evidence/build-final.log)。
- app 与 MySQL 容器启动成功，MySQL 为 healthy；见 [启动日志](evidence/startup.log) 和 [状态记录](evidence/status.log)。
- GET /actuator/health 返回 200、UP；POST /api/books 返回 201、Location=/api/books/1；列表与详情返回 200；见 [API 响应记录](evidence/api.json)。
- 创建记录为 ID=1、书名=Docker容器化实践、作者=王超浩、ISBN=W05-20261022。
- 实际执行 down 后再 up -d，重新查询 ID=1，四个字段与重建前完全相同；见 [重建命令输出](evidence/recreate.log) 和 [前后数据比较](evidence/persistence.json)。
- 以上为真实命令与 HTTP 验证，不以自动生成图片代替手动截图。

## 手动截图

截图由学生本人手动截取，目前不包含代制图片。截图保存到本目录的 `screenshots/` 后提交 GitHub，同时上传作业系统对应栏位。

| 文件名 | 内容 | 作业栏 |
| --- | --- | --- |
| 12-1-镜像构建.png | 构建成功尾部、镜像名称标签和大小 | 12-1 Dockerfile 与镜像构建 |
| 12-2-编排启动.png | compose ps 中 app Up、mysql healthy、端口映射 | 12-2 Compose 编排与启动 |
| 12-3-健康与创建.png | 健康 UP、创建 201、请求与响应 | 12-3 个人业务 API 验证 |
| 12-3-查询.png | 查询 200 与刚创建的图书 | 12-3 个人业务 API 验证 |
| 12-4-重建前.png | 指定 ID 的完整记录 | 12-4 数据持久化验证 |
| 12-4-重建后.png | down/up 过程和同一 ID 的相同记录 | 12-4 数据持久化验证 |

构建与重建过程如一张图放不下可拆图；每栏 1—5 张。校园选课课内实践与个人图书项目分别运行，不混用截图。
