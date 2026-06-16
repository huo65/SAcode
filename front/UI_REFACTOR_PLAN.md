# SA外卖平台 · 前端UI改造计划

> 基于 `ref/` 目录下四个参考页面设计，对现有前端进行系统性UI升级
> 同时结合后端功能地图与余额支付改造说明，确保前端页面覆盖课堂展示所需全部功能
> 创建时间：2026-06-15 | 上次更新：2026-06-16

---

## 一、现状诊断

| 维度 | 当前状态 | 目标状态 |
|-------|---------|---------|
| 布局结构 | `el-tabs` 顶部横向切换 | 左侧 Sidebar + 右侧内容区 |
| 色彩系统 | `theme.less` 3个无效变量 | 4套完整 CSS 变量（每角色独立主题色） |
| 导航模式 | 每角色页内 tab 切换 | 全局 Sidebar 导航 + 页内子路由 |
| 数据展示 | 以 `el-table` 为主 | 统计卡片 + 图表 + 表格三合一 |
| 支付口径 | 支付宝沙箱 + 余额支付并存 | 统一为余额支付，移除支付宝主链路入口 |
| 响应式 | 部分适配 | 完整移动端适配（参考 ref 的 media query） |

---

## 二、参考设计分析

### 色彩系统（4角色独立主题）

| 角色 | 主色 | 背景渐变 | 来源文件 |
|------|------|---------|---------|
| 商家 | `#E8652B` 橙 | 暖白→米白 | `ref/index.html` |
| 顾客 | `#EF4444` 红 | 白→浅红 | `ref/user.html` |
| 管理员 | `#4F46E5` 蓝 | 白→靛蓝 | `ref/admin.html` |
| 骑手 | `#059669` 绿 | 白→翠绿 | `ref/rider.html` |

### 通用设计语言

参考页面中以下类名风格统一，可直接作为组件规范：

- `.stat-card` — 统计卡片（数字 + 标签 + 趋势）
- `.badge` — 状态标签（success/warning/danger/info）
- `.btn` — 按钮（primary/outline/ghost 三种变体）
- `.timeline` — 时间线（订单状态跟踪）
- `.sidebar` — 侧边导航（active 状态 + 图标）
- `.menu-grid` — 卡片网格（餐厅/菜品浏览）

---

## 三、课堂展示功能优先级

> 来源：`docs/后端功能地图与前端页面设计参考.md` 第11节

### P0：必须展示

1. 四角色登录和角色化首页
2. 顾客餐厅浏览、下单、充值、余额支付
3. 商家接单、备餐、推送骑手
4. 骑手接单、配送中订单
5. 顾客确认收货、评价
6. 商家/管理员退款，余额和库存回滚
7. 管理员商品审核、平台看板、用户管理

### P1：增强展示

1. 钱包流水
2. 售后工单
3. 商家经营分析和 CSV 导出
4. 权限矩阵
5. 审计日志
6. 上传商品图/门店图回显

### P2：后续美化和生产化

1. 移动端适配
2. 订单时间线
3. 空状态、加载态、错误态
4. 地图导航占位优化
5. 消息通知中心
6. 分页和高级筛选

---

## 四、改造阶段划分（调整后）

> **调整说明**：将余额支付改造提前为 Phase 2.5（P0 功能，且为跨角色任务）；按 P0→P1→P2 顺序排列后续 Phase

---

### Phase 1：基础设施层 ✅ 已完成（2026-06-15）

**目标**：搭建全局设计系统，不影响现有功能

**文件变更清单**：

```
files/
├── src/style/
│   ├── theme.less          ✏️ 重写：4套角色CSS变量
│   ├── sidebar.less        🆕 新建：Sidebar布局样式
│   ├── stat-card.less      🆕 新建：统计卡片组件样式
│   ├── badge.less          🆕 新建：状态标签样式
│   └── global.less         🆕 新建：全局重置与通用类
├── src/components/
│   └── AppSidebar/
│       ├── index.vue        🆕 新建：全局侧边栏组件
│       └── index.less      🆕 新建：侧边栏样式
├── src/main.js              ✏️ 修改：引入 global.less
└── vite.config.js          ✏️ 修改：全局Less变量注入
```

**交付标准**：

- [x] `theme.less` 完整4套变量（商家橙/顾客红/管理蓝/骑手绿），保留 `@price` 等旧变量兼容
- [x] `AppSidebar` 组件支持4角色菜单动态渲染
- [x] 全局 Less 文件通过 `vite.config.js` 的 `additionalData` 注入
- [x] 现有页面样式不被破坏（向后兼容）

---

### Phase 2：商家端改造 ✅ 已完成（2026-06-16，含评价回复页）

**参考**：`ref/index.html` + 后端功能地图商家端

**已完成范围**：

| 页面 | 路由 | 状态 |
|------|------|------|
| 经营概览 | `/home/merchant/dashboard` | ✅ 已完成 |
| 商品管理 | `/home/merchant/goods` | ✅ 已完成（复用现有组件） |
| 订单管理 | `/home/merchant/orders` | ✅ 已完成（复用现有组件） |
| 售后管理 | `/home/merchant/after-sale` | ✅ 已完成（复用现有组件） |
| 门店资料 | `/home/merchant/store` | ✅ 已完成（复用现有组件） |
| 经营分析 | `/home/merchant/ops` | ✅ 已完成（复用现有组件） |
| 个人资料 | `/home/merchant/info` | ✅ 已完成（复用现有组件） |

**待补充（Phase 2 收尾）**：

- [x] **评价回复页** `src/components/review/merchant-reply.vue` — 商家回复顾客评价 ✅ 已完成
- [x] **AppSidebar 商家菜单补齐**"评价回复"项（带"待回复"角标 pendingReviews）✅ 已完成
- [ ] **操作日志页** `src/page/home/merchant/audit.vue` — 商家级审计日志（暂不实现，复用管理端审计页）

**文件变更清单**：

```
✏️  src/page/home/index.vue          → 简化为角色分发器
✏️  src/page/home/merchant/index.vue → 大改：AppSidebar + router-view 布局
🆕  src/page/home/merchant/dashboard.vue → 新建：经营概览页（stat-card + CSS趋势图）
🆕  src/router/modules/merchant.js   → 新建：7条商家子路由
✏️  src/page/home/components/Header.vue → 改：适配Sidebar，增加汉堡按钮
✏️  src/style/stat-card.less         → 完善：补全所有状态变体
✏️  src/page/home/merchant/index.vue <style> → 增加Sidebar环境子组件覆盖样式
```

---

### Phase 2.5：余额支付改造 ⭐ P0 紧急

> 来源：`docs/前端适配余额支付改造说明.md`
> **必须在课堂展示前完成**，影响顾客端、购物车、商品详情、支付弹窗

**改造范围**：

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/api/apis.js` | ✏️ 修改 | 新增 `User.recharge`、`User.wallet`、`User.walletTransactions`、`Ops.walletTransactions`；支付宝接口保留但不用于主链路 |
| `src/components/goods/pay.vue` | ✏️ 大改 | 移除支付宝选项和二维码，改为"余额支付"弹窗，展示余额和快捷充值入口 |
| `src/components/goods/cart.vue` | ✏️ 修改 | 删除 `startAlipayPayment` 相关逻辑，统一调用 `Order.payOrder2` |
| `src/components/goods/detail.vue` | ✏️ 修改 | 删除支付宝分支，统一调用 `Order.payOrder2` |
| `src/components/info/index.vue` | ✏️ 修改 | 增加"钱包余额"和"最近流水"展示（顾客个人信息页） |
| `src/components/operation/admin-ops.vue` | ✏️ 修改 | 增加"钱包流水"列表（管理员治理页，P1） |

**新增 API（`src/api/apis.js`）**：

```js
User: {
  recharge:           { post: '/user/recharge' },
  wallet:             { get: '/user/wallet' },
  walletTransactions:  { get: '/user/wallet/transactions' },
}
Ops: {
  walletTransactions:  { get: '/ops/wallet/transactions' },
}
```

**支付弹窗新流程**：

```
顾客点击"去支付"
    ↓
打开余额支付弹窗
    ↓
展示：订单金额 / 当前余额 / 余额是否充足
    ↓
余额充足 → "确认余额支付"按钮 → 调用 POST /user/pay
余额不足 → 显示"余额不足，请先充值" → 弹出充值输入框 → 调用 POST /user/recharge
    ↓
支付成功 → 刷新订单状态、余额、购物车 → 跳转订单页
支付失败 → 展示后端错误（余额不足/库存不足/订单状态异常）
```

**交付标准**：

- [ ] `pay.vue` 只保留余额支付方式，移除支付宝入口
- [ ] `cart.vue` 和 `detail.vue` 不再调用支付宝接口
- [ ] 支付弹窗展示当前余额，余额不足时提供快捷充值
- [ ] 顾客个人信息页展示钱包余额和最近流水
- [ ] 管理员治理页可查看平台钱包流水（P1）
- [ ] 跑通主链路：充值 → 下单 → 余额支付 → 确认收货 → 评价

---

### Phase 3：顾客端改造

**参考**：`ref/user.html` + 后端功能地图顾客端

**推荐页面清单**（来源：后端功能地图第9节）：

```
顾客端页面结构：
├── 餐厅首页              (/home/customer/home)
├── 餐厅详情/菜单         (/home/customer/restaurant/:id)
├── 商品详情             (/home/customer/product/:id)
├── 购物车               (/home/customer/cart)
├── 订单确认              (/home/customer/checkout)
├── 余额支付弹窗          (复用 pay.vue，Phase 2.5 改造后)
├── 钱包中心              (/home/customer/wallet)
├── 我的订单              (/home/customer/orders)
├── 评价列表/评价弹窗     (/home/customer/reviews)
├── 售后工单              (/home/customer/after-sale)
└── 个人资料/地址管理     (/home/customer/profile)
```

**Sidebar 菜单项（顾客端）**：

```
首页/推荐       (/home/customer/home)
────────────────────────────────
我的订单       (/home/customer/orders)
购物车         (/home/customer/cart)
钱包中心       (/home/customer/wallet)
收货地址       (/home/customer/address)
售后工单       (/home/customer/after-sale)
个人资料       (/home/customer/profile)
```

**文件变更清单**：

```
✏️  src/page/home/customer/index.vue    → 大改：加Sidebar + router-view
🆕  src/router/modules/customer.js      → 新建：顾客端子路由
🆕  src/page/home/customer/wallet.vue   → 新建：钱包中心（余额+充值+流水）
🆕  src/page/home/customer/address.vue  → 新建：地址管理页
🆕  src/page/home/customer/reviews.vue  → 新建：评价列表页
✏️  src/components/restaurant/index.vue → 改：列表改卡片（参考 .restaurant-card）
✏️  src/components/goods/cart.vue       → 改：应用新样式（Phase 2.5同步修改）
🆕  src/components/timeline/index.vue    → 新建：订单状态时间线组件
```

---

### Phase 4：管理端改造

**参考**：`ref/admin.html` + 后端功能地图管理端

**推荐页面清单**（来源：后端功能地图第9节）：

```
管理端页面结构：
├── 平台看板              (/home/admin/dashboard)
├── 商品审核              (/home/admin/product-audit)
├── 分类管理              (/home/admin/categories)
├── 订单总览              (/home/admin/orders)
├── 用户管理              (/home/admin/users)
├── 售后总控              (/home/admin/after-sale)
├── 权限矩阵              (/home/admin/permissions)
├── 审计日志              (/home/admin/audit)
└── 钱包流水              (/home/admin/wallet)
```

**Sidebar 菜单项（管理端）**：

```
平台概览       (/home/admin/dashboard)
────────────────────────────────
用户管理       (/home/admin/users)
商家管理       (/home/admin/merchants)
骑手管理       (/home/admin/riders)
订单管理       (/home/admin/orders)
商品审核       (/home/admin/product-audit)
分类管理       (/home/admin/categories)
售后总控       (/home/admin/after-sale)
权限矩阵       (/home/admin/permissions)
审计日志       (/home/admin/audit)
钱包流水       (/home/admin/wallet)
```

**文件变更清单**：

```
✏️  src/page/home/admin/index.vue        → 大改：完整Sidebar + 10个子页
🆕  src/router/modules/admin.js          → 新建：管理端子路由
🆕  src/page/home/admin/dashboard.vue    → 新建：平台概览页（stat-card + 图表）
🆕  src/page/home/admin/users.vue        → 新建：用户管理页（启用/禁用+详情）
🆕  src/page/home/admin/product-audit.vue → 新建：商品审核页
🆕  src/page/home/admin/permissions.vue  → 新建：权限矩阵页（开关式）
🆕  src/page/home/admin/audit.vue        → 新建：审计日志页
🆕  src/page/home/admin/wallet.vue       → 新建：钱包流水页
✏️  src/components/operation/admin-ops.vue → 改：并入管理端，或移除
```

---

### Phase 5：骑手端改造

**参考**：`ref/rider.html` + 后端功能地图骑手端

**推荐页面清单**（来源：后端功能地图第9节）：

```
骑手端页面结构：
├── 待接单                (/home/driver/available)
├── 配送中                (/home/driver/delivering)
├── 历史订单              (/home/driver/history)
├── 订单详情              (/home/driver/order/:id)
└── 个人资料              (/home/driver/profile)
```

**Sidebar 菜单项（骑手端）**：

```
今日概况       (/home/driver/dashboard)
────────────────────────────────
待接单         (/home/driver/available)
配送中         (/home/driver/delivering)
已完成         (/home/driver/history)
我的收益       (/home/driver/earnings)
个人资料       (/home/driver/profile)
```

**文件变更清单**：

```
✏️  src/page/home/driver/index.vue         → 大改：加Sidebar + router-view
🆕  src/router/modules/driver.js           → 新建：骑手端子路由
🆕  src/page/home/driver/dashboard.vue     → 新建：今日概况页（统计卡片）
🆕  src/page/home/driver/available.vue     → 新建：待接单列表
🆕  src/page/home/driver/delivering.vue    → 新建：配送中订单
🆕  src/components/driver/order-card.vue    → 新建：可接订单卡片
🆕  src/components/driver/delivery-progress.vue → 新建：配送进度组件
```

**注意事项**（来源：后端功能地图第6节）：

- 骑手只能接 `state = 3` 的订单
- 同一骑手已有配送中订单时，不能再接第二单
- 地图导航目前是前端占位，真实地图属于后续增强

---

### Phase 6：收尾优化

> 以 P2 功能为主，课堂展示后可继续完善

```
收尾清单：
├── [ ] 响应式适配：breakpoint 960px/640px（参考ref的media query）
├── [ ] 订单状态时间线组件（Phase 3 可能已提前完成）
├── [ ] 空状态、加载态、错误态统一设计
├── [ ] 地图导航占位优化（骑手端）
├── [ ] 消息通知中心（后端 MessageController 仍为空壳，可后续增强）
├── [ ] 分页和高级筛选（各列表页）
├── [ ] 皮肤切换动画优化
└── [ ] 浏览器测试：Chrome/Edge/Firefox 兼容性
```

---

## 五、订单状态机统一工具

> 来源：后端功能地图第8节，建议前端做成统一工具

**状态映射**（前端建议封装为 `src/utils/orderState.js`）：

| 状态值 | 状态名 | 顾客可见操作 | 商家可见操作 | 骑手可见操作 |
|--------|--------|-------------|-------------|-------------|
| `-3` | 已退款 | 查看退款结果 | 确认退款 | — |
| `-2` | 退款中 | 等待处理 | 处理退款 | — |
| `-1` | 待支付 | 去支付/取消 | — | — |
| `0` | 已支付/待接单 | 等待商家 | 接单/拒单 | — |
| `4` | 备餐中 | 等待出餐 | 呼叫骑手 | — |
| `3` | 待骑手接单 | 等待骑手 | — | 接单 |
| `1` | 配送中 | 确认收货 | — | 配送中 |
| `2` | 已完成 | 评价/售后 | 查看评价 | 查看历史 |

---

## 六、风险与应对

| 风险 | 影响 | 应对方案 |
|------|------|---------|
| `el-tabs` 改 Sidebar 导致路由逻辑大改 | 高 | Phase 2 已完成商家端验证，可推广到顾客/管理/骑手端 |
| Element Plus 默认样式覆盖困难 | 中 | 用 `:deep()` 强制覆盖，或参考 ref 用纯 HTML+CSS 重写关键组件 |
| 现有功能回归 | 中 | 每 Phase 完成做冒烟测试，保留 `el-tabs` 作为 fallback |
| CSS 变量名冲突 | 低 | 统一用 `--role-*` 前缀（如 `--merchant-primary`） |
| 余额支付改造影响多个组件 | 中 | 单独作为 Phase 2.5，集中测试主链路 |
| 后端接口变动 | 中 | 以 `docs/后端功能地图与前端页面设计参考.md` 为权威参考 |

---

## 七、执行跟踪

> 每完成一个 Phase，在此更新进度

- [x] Phase 1：基础设施层（2026-06-15 完成）
- [x] Phase 2：商家端改造（2026-06-16 完成，含评价回复页 merchant-reply.vue）
- [x] **Phase 2.5：余额支付改造（2026-06-16 完成）**
- [x] **Phase 2 补齐：管理端 AppSidebar 接入 + 钱包流水页 + 审计日志页 + 平台概览页（2026-06-16 完成）**
- [ ] Phase 3：顾客端改造
- [ ] Phase 4：管理端改造（首页+2 个独立子页完成，其余子页待做）
- [ ] Phase 5：骑手端改造
- [ ] Phase 6：收尾优化

---

## 八、文档依赖

以下文档为本次改造的权威参考，改造过程中如有冲突以这些文档为准：

1. `docs/后端功能地图与前端页面设计参考.md` — 后端接口清单、推荐前端页面结构、优先级
2. `docs/前端适配余额支付改造说明.md` — 余额支付改造范围、API 变更、验收清单
3. `front/ref/` 目录下四个 HTML 文件 — UI 设计参考、CSS 变量、组件样式

---

*本文档随改造进度持续更新*
