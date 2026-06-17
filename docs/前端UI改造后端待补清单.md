# 前端 UI 改造后端待补清单

> 记录前端 UI 重构和浏览器联调中发现的后端能力缺口。前端会尽量做空态、降级和静默兜底，但以下能力建议后续在后端补齐，避免演示或真实使用时出现数据不完整、接口失败或只能依赖前端模拟数据的问题。

## 1. 商家评价列表接口

### 当前现象

- 商家端已经补充了“评价回复”页面。
- 前端已有 `Review.merchantList` 配置，指向 `GET /review/merchant/list`。
- 后端目前 `OrderReviewController` 仅实现：
  - `POST /review/add`
  - `POST /review/reply`
- 浏览器联调时，商家评价页会请求评价列表接口，但后端没有对应接口。前端已临时改为静默降级展示模拟数据，避免页面弹出 `request failed`。

### 建议补充

新增：

```http
GET /review/merchant/list
```

建议请求参数：

- `keyword`：可选，按顾客名、订单号、评价内容、商品名搜索。
- `rating`：可选，评分筛选。`5/4/3/-1`，其中 `-1` 表示 1-2 星差评。
- `replyStatus`：可选，`all/pending/replied`。

建议返回：

```json
{
  "list": [
    {
      "id": "review-id",
      "orderId": "order-id",
      "userName": "customer",
      "productName": "商品名称",
      "productImage": "/storage/xxx.jpg",
      "rating": 5,
      "content": "评价内容",
      "tags": ["味道好"],
      "images": [],
      "replied": true,
      "reply": "商家回复内容",
      "replyTime": "2026-06-17 15:00",
      "createdAt": "2026-06-17 14:30"
    }
  ],
  "overview": {
    "total": 286,
    "average": 4.6,
    "goodRate": 92,
    "pending": 3
  },
  "ratingDistribution": [
    { "star": 5, "count": 198, "percent": 69 }
  ]
}
```

### 关联前端页面

- `front/src/components/review/merchant-reply.vue`
- `front/src/router/modules/merchant.js` 中 `/home/merchant/review`

## 2. 评价回复接口字段兼容

### 当前现象

- 后端 `POST /review/reply` 当前读取字段：
  - `orderId`
  - `replyContent`
- 商家评价页的独立回复页面更自然地以 `reviewId` 为主键提交。

### 建议补充

后端可兼容两种提交方式：

```json
{
  "orderId": "order-id",
  "replyContent": "回复内容"
}
```

或：

```json
{
  "reviewId": "review-id",
  "replyContent": "回复内容"
}
```

这样订单页内回复和评价管理页内回复都能共用同一后端能力。

## 3. 订单查询参数命名统一

### 当前现象

- 后端 `OrderInfoController` 的 `POST /order/get` 要求参数名为 `usrId`。
- 前端历史代码中同时出现过 `usrId` 和 `userId`。
- 本轮前端已将通用订单组件、商家首页统计等位置统一修正为 `usrId`。

### 建议补充

后端可以继续保留 `usrId`，也可兼容 `userId` 作为别名，降低前端历史代码或第三方调用误用导致的失败概率。

建议规则：

- 如果 `usrId` 存在，优先使用 `usrId`。
- 如果 `usrId` 为空且 `userId` 存在，则使用 `userId`。
- 对非管理员角色，最终仍以 JWT 当前用户为准，避免越权查询。

## 4. 钱包流水返回结构统一

### 当前现象

- 顾客个人中心、钱包中心、管理员钱包流水页面都需要展示余额和交易流水。
- 后端部分接口返回字段包含 `transactions`，前端部分历史页面兼容 `list/data/transactions` 多种结构。

### 建议补充

统一：

```http
GET /user/wallet
GET /user/wallet/transactions
GET /ops/wallet/transactions
```

建议返回字段：

```json
{
  "balance": 547,
  "totalRecharge": 1000,
  "totalPay": 334,
  "totalRefund": 32,
  "transactions": [
    {
      "id": "tx-id",
      "userId": "1",
      "userName": "customer",
      "type": "RECHARGE",
      "amount": 100,
      "balanceBefore": 447,
      "balanceAfter": 547,
      "relatedOrderId": null,
      "remark": "余额充值",
      "createdTime": "2026-06-17 15:00"
    }
  ]
}
```

管理员平台流水建议支持：

- `userId`
- `type`
- `limit`
- 后续可扩展 `page/pageSize/startTime/endTime`

### 已发现的筛选枚举问题

管理端钱包流水页面曾出现如下请求：

```http
GET /ops/wallet/transactions?type=all&page=1&pageSize=20
```

后端返回：

```json
{
  "code": 0,
  "data": null,
  "msg": "Invalid wallet transaction type."
}
```

前端已修正：当筛选器选择“全部类型”时不再传 `type=all`，而是省略 `type` 参数。

后端后续也建议兼容：

- `type` 缺省、空字符串、`all`、`ALL` 均表示全部类型。
- 具体类型仍限定为 `RECHARGE/PAY/REFUND`。
- 非法类型返回错误可以保留，但错误文案建议包含允许值，便于前端定位。

## 5. 售后列表与统计字段稳定化

### 当前现象

- 顾客端、商家端、管理员端均已接入售后工单页面。
- 当前前端依赖：
  - `GET /afterSale/list`
  - `GET /afterSale/stats`
  - `POST /afterSale/update`
- 联调中售后页面可以渲染数据，但不同角色页面对字段命名较敏感。

### 建议补充

`GET /afterSale/list` 建议稳定返回：

```json
{
  "ticketList": [
    {
      "id": "ticket-id",
      "orderId": "order-id",
      "type": "投诉反馈",
      "content": "问题描述",
      "status": "待处理",
      "customerName": "customer",
      "merchantName": "merchant",
      "orderAmount": 32,
      "receiveAddress": "收货地址",
      "handlerName": "处理人",
      "handlerNote": "处理备注",
      "createdTime": "2026-06-17 15:00",
      "updatedTime": "2026-06-17 15:20"
    }
  ]
}
```

`GET /afterSale/stats` 建议稳定返回：

```json
{
  "stats": {
    "total": 2,
    "pending": 2,
    "processing": 0,
    "resolved": 0,
    "closed": 0,
    "refund": 0
  }
}
```

## 6. 管理端/商家端经营看板容错

### 当前现象

- 商家端经营分析、管理员平台看板已经接入 `Ops` 相关接口。
- 前端希望在接口暂无数据时展示空态，而不是弹出错误提示。

### 建议补充

以下接口建议保证即使无业务数据也返回成功空结构：

- `GET /ops/admin/dashboard`
- `GET /ops/merchant/dashboard`
- `GET /ops/audit/list`
- `GET /ops/merchant/export`

例如：

```json
{
  "summary": {},
  "trend": [],
  "recentOrders": [],
  "recentAudits": [],
  "hotProducts": []
}
```

避免“无数据”被表示为业务失败。

## 7. 骑手端配送能力后续增强

### 当前现象

- 骑手端已完成移动端壳层和核心页面：
  - 概况
  - 待接单
  - 配送中
  - 历史订单
  - 收益明细
  - 个人中心
- 当前真实地图导航、路线规划、自动派单仍是文档中确认的后续增强项。

### 建议补充

后续可按优先级补：

- 骑手当前位置上报接口。
- 订单取餐点/收货点经纬度字段。
- 预计距离和预计送达时间后端计算。
- 自动派单策略或手动派单接口。
- 配送异常上报持久化接口。

## 8. 推荐后续处理顺序

1. 补 `GET /review/merchant/list`，让商家评价页从模拟数据切换为真实数据。
2. 兼容 `POST /review/reply` 的 `reviewId` 提交方式。
3. 统一订单查询参数，后端兼容 `usrId/userId`。
4. 稳定钱包、售后、经营看板返回结构。
5. 增强骑手位置、路线和异常上报能力。

## 9. 当前前端兜底策略

- 评价列表接口缺失时：前端静默降级为演示数据，不再弹错误。
- 钱包/售后/经营数据为空时：前端展示空态或零值卡片。
- 订单查询参数：前端已统一改为后端当前要求的 `usrId`。
- 顾客端与骑手端：已补手机端退出入口，便于角色切换和真实使用。
