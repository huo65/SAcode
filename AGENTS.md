# AGENTS.md

## Project Goal

This repository is a classroom-demo implementation of the food delivery order system described in the two course PPT files. The priority is a clear, runnable, explainable four-role business loop rather than production-level completeness.

Use these documents as the current source of truth:

- [README.md](README.md): project entry and document map.
- [docs/需求进度与后续计划.md](docs/需求进度与后续计划.md): requirements, current progress, and roadmap.
- [docs/课堂展示联调验收清单.md](docs/课堂展示联调验收清单.md): final demo verification checklist.
- [front/README.md](front/README.md): frontend commands and structure.
- [backend/DB-market/DEVELOPMENT.md](backend/DB-market/DEVELOPMENT.md): backend database, configuration, test, and run notes.

## Development Principles

- Prioritize features that can be demonstrated end to end in class.
- Keep the customer, merchant, driver, and admin paths coherent before adding production-grade enhancements.
- Prefer a stable local demo path for payment, storage, maps, and messaging when external integrations are not fully ready.
- Update `docs/需求进度与后续计划.md` when feature status or the roadmap changes.
- Update `docs/课堂展示联调验收清单.md` when verification steps or final demo evidence changes.

## How to Run (测试指导)

以下命令已在当前机器实测可行，供后续测试直接参考。详细说明见 [`.trae/rules/project_rules.md`](.trae/rules/project_rules.md)。

### 后端启动

工作目录：`backend/DB-market`（绝对路径 `D:\360MoveData\Users\86132\Desktop\SA\code\backend\DB-market`）

```powershell
mvn spring-boot:run -o -s maven-settings-public.xml "-Dmaven.repo.local=C:\Users\86132\.m2\repository"
```

关键点：
- 必须带 `-o`（离线模式）、`-s maven-settings-public.xml`（覆盖默认 3.9.8 settings 指向的内网镜像）和 `-Dmaven.repo.local=C:\Users\86132\.m2\repository`（IDEA 实际预下载缓存的仓库，含完整 spring-boot 2.7.17 依赖）；不要用默认 `mvn spring-boot:run`，否则会因 3.9.8 `yx_resp` 仓库未缓存依赖而下载失败并触发 `.lastUpdated` 写权限错误。切勿用文档早前记载的 `D:\Maven_Soft\apache-maven-3.6.1\mvn_resp`（该仓库只有少量 asm/xz 依赖，没有 spring-boot）。
- 首次启动前必须清理失败跟踪文件，否则离线模式会判定本地 artifact 为 absent（即使 pom/jar 实际存在），报 `Non-resolvable parent POM ... (absent)`：
  ```powershell
  Get-ChildItem -Path "C:\Users\86132\.m2\repository" -Filter "*.lastUpdated" -Recurse -Force | Remove-Item -Force
  Get-ChildItem -Path "C:\Users\86132\.m2\repository" -Filter "_remote.repositories" -Recurse -Force | Remove-Item -Force
  ```
- 启动成功判据：日志末尾出现 `Started DBmarketApplication in X seconds` 和 `项目启动成功!`，Tomcat 监听 `http://localhost:8080`。
- 前置条件：JDK 17、MySQL 已导入 `src/main/resources/schema.sql`、本地 `application.yml` 已配置、端口 8080 未占用。
- 备选：`mvn clean package -o -DskipTests -s maven-settings-public.xml "-Dmaven.repo.local=C:\Users\86132\.m2\repository"` 后 `java -jar target\DB-market-*.jar`；镜像有问题时改用 `mvn spring-boot:run -s D:\Maven_Soft\apache-maven-3.6.1\conf\settings.xml "-Dmaven.repo.local=C:\Users\86132\.m2\repository"`（3.6.1 自带 aliyun 镜像，在线模式）。

### 前端启动

工作目录：`front`：

```powershell
npm install
npm run dev
npm test
```

前端无独立 lint/typecheck 脚本，测试命令为 `npm test`（验证 `front/tests/*.test.mjs` 的 3 个工具函数单测用例）。

## Current Priority

1. Run one complete local verification round and fill in the checklist.
2. Confirm the `driver` role page, order pickup, and delivery completion path.
3. Verify upload, local storage, and image display.
4. Decide whether the class demo uses the stable balance payment path or also includes Alipay sandbox.
5. Keep later production-level work, such as pagination, cache, monitoring, backup, CI, and E2E, as roadmap items unless the main demo is already stable.
