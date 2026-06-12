# Phase 4 售后客服闭环改造说明

## 1. 改造目标

本阶段按课堂展示版标准推进，目标是把“评价之后”的问题处理链路补齐，让顾客、商家、管理员三端形成一条可展示、可讲解、可回看结果的售后闭环。

本轮强调：

- 优先做“售后工单闭环”，而不是实时聊天客服。
- 优先让顾客在订单页直接发起售后，降低演示步骤。
- 优先让商家和管理员共用统一处理台，保证状态流转清晰。
- 优先让售后结果能回显到订单卡片，形成前后台一致的结果反馈。

## 2. 本轮完成内容

### 2.1 后端

- 新增 `after_sale_ticket` 工单表，承载：
  - 工单编号
  - 关联订单
  - 顾客 / 商家
  - 问题类型
  - 问题描述
  - 工单状态
  - 处理人
  - 处理备注
  - 创建时间 / 更新时间
- 新增售后服务模块：
  - 顾客提交工单
  - 顾客 / 商家 / 管理员按角色查看工单列表
  - 商家 / 管理员更新工单处理状态与备注
  - 平台和商家查看售后统计卡片
- 兼容课堂展示版需求：
  - 不做实时客服消息流
  - 不做复杂工单派单
  - 只保留最容易展示的 4 个状态：`待处理 / 处理中 / 已解决 / 已关闭`

### 2.2 前端

- 顾客订单页新增售后工单入口：
  - 可在订单卡片直接提交售后
  - 支持问题类型与描述
  - 提交后在订单卡片显示工单类型、状态、处理备注
- 新增统一售后处理台组件：
  - 平台侧显示“平台售后总控台”
  - 商家侧显示“商家售后处理台”
  - 支持统计卡片、筛选、工单表格、详情抽屉、状态更新
- 管理端首页重做：
  - 增加治理中心 Hero 区
  - 新增 `After-Sale` 标签页
  - 顶部展示待处理售后数量
- 商家端首页升级：
  - 增加待处理售后统计
  - 新增 `After-Sale` 标签页

## 3. 课堂展示版取舍

以下能力本轮按“可演示即可”的标准处理：

- 不做聊天客服、不做 WebSocket
- 不做复杂工单转派、抢单、SLA 策略
- 不做完整审批流，只保留处理备注与状态流转
- 退款类问题先纳入工单类型和台账视图，不单独拆出复杂退款中心

## 4. 前端设计方向

本轮前端改造遵循 `frontend-design` 路线，选择的是：

- 平台侧：冷色“控制台 / 指挥台”视觉
- 商家侧：延续门店运营中心但新增售后处理分区
- 售后处理台：强调状态、数量、处理节奏与信息层次

设计目标是让课堂展示时：

- 一眼看出“这是平台/商家的问题处理后台”
- 工单状态清晰
- 处理动作集中
- 结果反馈明显

## 5. 主要影响范围

### 5.1 后端文件

- `backend/DB-market/src/main/resources/schema.sql`
- `backend/DB-market/src/main/java/com/DB/DBmarket/mapper/AfterSaleTicketMapper.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/pojo/afterSale/AfterSaleTicket.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/pojo/afterSale/AfterSaleTicketView.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/service/AfterSaleService.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/service/impl/AfterSaleServiceImpl.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/controller/AfterSaleController.java`

### 5.2 前端文件

- `front/src/api/apis.js`
- `front/src/components/order/index.vue`
- `front/src/components/after-sale/index.vue`
- `front/src/page/home/admin/index.vue`
- `front/src/page/home/merchant/index.vue`

## 6. 后续可选深化方向

- 扩展实时客服消息
- 工单自动分派与优先级策略
- 退款审核工作台与财务台账细化
- 售后处理时长统计、满意度统计、异常预警
- 工单与审计日志联动
