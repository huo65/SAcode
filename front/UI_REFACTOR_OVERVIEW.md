# 前端UI改造总览 — 2026-06-16

## 改造完成情况

### ✅ 已完成的任务

| 任务 | 状态 | 说明 |
|------|------|------|
| #1 品牌名统一 | ✅ | "味掌柜" → "西电外卖" |
| #2 顾客端改造 | ✅ | Sidebar布局 + 5个新页面 |
| #3 管理端改造 | ✅ | 已在之前会话完成，dashboard/wallet/audit 均已就绪 |
| #4 骑手端改造 | ✅ | Sidebar布局 + 5个新页面 |
| #5 订单状态工具 | ✅ | orderState.js + OrderTimeline 组件 |
| #6 收尾优化 | ✅ | body主题切换 + 构建验证 |

---

## 新增文件清单

### 路由模块
- `src/router/modules/customer.js` — 8条顾客端路由
- `src/router/modules/driver.js` — 7条骑手端路由

### 顾客端页面（`src/page/home/customer/`）
- `home.vue` — 推荐首页（红主题、分类图标、附近好店）
- `cart.vue` — 购物车（加减数量、清空、提交订单）
- `wallet.vue` — 钱包中心（余额展示、充值弹窗、交易流水）
- `address.vue` — 地址管理（增删改查）
- `review.vue` — 我的评价（评分展示、商家回复）

### 骑手端页面（`src/page/home/driver/`）
- `overview.vue` — 今日概况（统计卡片、快捷操作、服务区域设置）
- `available.vue` — 待接订单列表（接单操作）
- `delivering.vue` — 配送中订单（确认送达）
- `history.vue` — 历史订单（全部/已完成/已取消筛选）
- `earnings.vue` — 收益明细（图表、绩效等级、收入明细）

---

## 核心架构变更

### 布局模式统一
所有4角色现在都使用 **AppSidebar + Header + router-view** 三段式布局：

```
page/home/
├── merchant/index.vue  ← Sidebar布局（已有）
├── admin/index.vue    ← Sidebar布局（已有）
├── customer/index.vue  ← 本次改造 ✅
└── driver/index.vue    ← 本次改造 ✅
```

### Body Class 主题切换
所有角色入口文件都设置了 `document.body.classList.add('role-{role}')`：
- `merchant/index.vue` ✅（本次添加）
- `customer/index.vue` ✅（本次添加）
- `admin/index.vue` ✅（已有）
- `driver/index.vue` ✅（本次添加）

这驱动 `global.less` 中的 CSS 变量覆盖，实现四角色独立主题色。

### 路由注册
`src/router/index.js` 现已注册全部4个角色路由：
```js
...merchantRoutes,  // 8条
...adminRoutes,      // 10条
...customerRoutes,   // 8条  ✅新增
...driverRoutes,     // 7条  ✅新增
```

---

## 设计规范遵循情况

| 规范 | 状态 |
|------|------|
| 品牌名"西电外卖" | ✅ 全局统一 |
| 顾客端红色主题 #EF4444 | ✅ `role-customer` CSS变量 |
| 骑手端绿色主题 #059669 | ✅ `role-rider` CSS变量 |
| 管理端 indigo 主题 #4F46E5 | ✅ 已有 |
| 商家端橙色主题 #E8652B | ✅ 已有 |
| 参考 ref/ 设计风格 | ✅ user.html / rider.html 风格已参考 |
| Sidebar 布局模式 | ✅ 4角色全部统一 |
| 响应式布局 | ✅ 所有新页面含媒体查询 |
| 空状态处理 | ✅ 所有列表页均有 empty-state |

---

## 下一步：浏览器验证

启动前端开发服务器后在 `http://localhost:5173/` 逐页验证：

1. **顾客登录** → 验证侧边栏、首页、餐厅列表、购物车、钱包
2. **骑手登录** → 验证概况页、待接单、配送中、历史订单、收益
3. **商家登录** → 验证已有改造是否仍然正常
4. **管理员登录** → 验证已有改造是否仍然正常

## 已知注意点

- 顾客端 `cart.vue` 提交订单调用 `Cart.submitOrderList`，需要后端对应接口正常工作
- 骑手端 `available.vue` 接单调用 `Order.updateOrder(state=1)`，需要后端支持
- 钱包充值使用本地 mock 逻辑（`User.recharge`），后端就绪后可切换真实支付
- 收益图表的近7日数据目前使用 `Math.random()` mock，后端有数据后会自动替换
