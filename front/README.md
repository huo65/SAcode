# 前端说明

前端用于课堂展示外卖订单系统的四端页面：顾客、商家、骑手、管理员。

## 环境

- Node.js `18.x`
- npm `10.x`

## 命令

```powershell
npm install
npm run dev
npm run build
npm test
```

## 目录

| 目录 | 说明 |
| --- | --- |
| `src/api` | 接口封装 |
| `src/components/goods` | 商品、购物车、支付相关组件 |
| `src/components/order` | 订单列表、状态流转、评价入口 |
| `src/components/restaurant` | 餐厅列表、详情、门店资料 |
| `src/components/after-sale` | 售后工单处理台 |
| `src/components/operation` | 平台治理、商家经营分析 |
| `src/page/home` | 各角色首页与布局 |
| `src/router` | 路由配置 |
| `tests` | 前端工具函数测试 |

## 当前展示范围

- 顾客：餐厅浏览、下单、支付、确认收货、评价、售后。
- 商家：商品管理、接单/备餐、门店资料、售后处理、经营分析。
- 骑手：接单、配送、状态和收入展示。
- 管理员：用户管理、商品审核、售后总控、RBAC、平台看板、审计日志。

后续计划统一维护在 [`../docs/需求进度与后续计划.md`](../docs/需求进度与后续计划.md)。

├─lib # 辅助模块

├─mock # mock数据

├─page # 页面

│  ├─home # 内容页

│  │  ├─admin # 管理员身份

│  │  ├─customer # 用户身份

│  │  └─merchant # 商家身份

│  └─login # 登录页

├─router # 路由

├─store # 状态管理

└─style # 公用样式
