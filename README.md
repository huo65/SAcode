# 外卖订单系统课堂展示版

本项目基于两份课程实践 PPT 的目标，完成一个课堂展示级的外卖订单系统。系统采用前后端分离和分层架构：前端 Vue 负责页面与交互，后端 Spring Boot 按 Controller / Service / Mapper / POJO 分层承载业务，MySQL 保存核心数据。

## 文档入口

- [需求进度与后续计划](docs/需求进度与后续计划.md)：PPT 需求对照、当前开发进度、剩余计划。
- [课堂展示联调验收清单](docs/课堂展示联调验收清单.md)：演示前按角色逐项验证。
- [前端说明](front/README.md)：前端技术栈、目录、启动与构建命令。
- [后端说明](backend/DB-market/DEVELOPMENT.md)：数据库、配置、测试与启动说明。

旧的 Phase 过程文档、过期待办文档和局部 UI 方案已经合并到上述文档中，避免同一状态在多处维护。

## 当前结论

项目主体已经达到课堂展示级：顾客、商家、骑手、管理员四类角色均有可进入页面，商品审核、餐厅浏览、下单支付、商家接单、骑手配送、确认收货、评价回复、售后工单、平台治理和经营分析均已有实现基础。

仍需重点补齐的是演示前联调记录、支付结算收口、上传链路验收、骑手端运行时复核，以及少量质量与交付增强。

## 快速启动

后端：

```powershell
cd backend/DB-market
mvn test
mvn spring-boot:run
```

前端：

```powershell
cd front
npm install
npm run dev
```

默认前端请求 `127.0.0.1:8080` 后端服务。演示账号来自 `backend/DB-market/src/main/resources/schema.sql`，默认密码为 `123456`。
