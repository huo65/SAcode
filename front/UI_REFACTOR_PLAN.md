# SA外卖平台 · 前端UI改造计划

> 基于 `ref/` 目录下四个参考页面设计，对现有前端进行系统性UI升级
> 创建时间：2026-06-15

---

## 一、现状诊断

| 维度 | 当前状态 | 目标状态 |
|------|---------|---------|
| 布局结构 | `el-tabs` 顶部横向切换 | 左侧 Sidebar + 右侧内容区 |
| 色彩系统 | `theme.less` 3个无效变量 | 4套完整 CSS 变量（每角色独立主题色） |
| 导航模式 | 每角色页内 tab 切换 | 全局 Sidebar 导航 + 页内子路由 |
| 数据展示 | 以 `el-table` 为主 | 统计卡片 + 图表 + 表格三合一 |
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

## 三、改造阶段划分

---

### Phase 1：基础设施层

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
└── vite.config.js          ✏️ 修改：全局Less变量注入
```

**交付标准**：
- [ ] `theme.less` 完整4套变量，使用 `--role-*` 前缀
- [ ] `AppSidebar` 组件支持4角色菜单动态渲染
- [ ] 全局 Less 文件通过 `vite.config.js` 的 `additionalData` 注入
- [ ] 现有页面样式不被破坏（向后兼容）

---

### Phase 2：商家端改造（优先级 ⭐ 最高）

**参考**：`ref/index.html`

**页面结构变更**：

```
改造前                         改造后
─────────────────────────     ─────────────────────────
┌─────────────────────┐     ┌──────────┬──────────────┐
│  el-tabs 顶部切换    │     │ Sidebar  │  Content     │
│  ─────────────────  │     │ (新增)   │ ┌──────────┐ │
│  内容区              │  →  │          │ │hero-stats │ │
│                      │     │          │ ├──────────┤ │
│                      │     │          │ │ router-  │ │
│                      │     │          │ │ view     │ │
│                      │     │          │ └──────────┘ │
└─────────────────────┘     └──────────┴──────────────┘
```

**Sidebar 菜单项（商家端）**：

```
经营概览  (/home/merchant/dashboard)
────────────────────────────────
商品管理    (/home/merchant/goods)
订单管理    (/home/merchant/orders)
售后管理    (/home/merchant/after-sale)
门店资料    (/home/merchant/store)
经营分析    (/home/merchant/ops)
```

**文件变更清单**：

```
✏️  src/page/home/merchant/index.vue  → 大改：左侧加Sidebar，顶部hero保留
🆕  src/router/modules/merchant.js   → 新建：商家子路由配置
✏️  src/components/goods/index.vue    → 改：列表改卡片网格（参考 .menu-grid）
✏️  src/components/order/index.vue    → 改：表格加状态badge（参考 .badge）
✏️  src/components/after-sale/        → 改：应用新样式
✏️  src/components/restaurant/        → 改：应用新样式
```

**交付标准**：
- [ ] 商家端完整 Sidebar 导航（6个菜单项）
- [ ] 经营概览页：4个统计卡片 + 图表区域
- [ ] 商品管理：网格卡片视图
- [ ] 订单管理：表格 + 状态 badge
- [ ] 所有子页面能在 Sidebar 切换后正常渲染

---

### Phase 3：顾客端改造

**参考**：`ref/user.html`

**Sidebar 菜单项（顾客端）**：

```
首页/推荐    (/home/customer/home)
────────────────────────────────
我的订单    (/home/customer/orders)
购物车      (/home/customer/cart)
个人中心    (/home/customer/profile)
```

**文件变更清单**：

```
✏️  src/page/home/customer/index.vue   → 大改：加Sidebar
✏️  src/components/restaurant/index.vue → 改：列表改卡片（参考 .restaurant-card）
✏️  src/components/goods/cart.vue      → 改：应用新样式
🆕  src/components/timeline/index.vue   → 新建：订单状态时间线组件
🆕  src/router/modules/customer.js     → 新建：顾客端子路由
```

---

### Phase 4：管理端改造

**参考**：`ref/admin.html`

**Sidebar 菜单项（管理端）**：

```
平台概览    (/home/admin/dashboard)
────────────────────────────────
用户管理    (/home/admin/users)
商家管理    (/home/admin/merchants)
骑手管理    (/home/admin/riders)
订单管理    (/home/admin/orders)
财务结算    (/home/admin/finance)
数据报表    (/home/admin/reports)
系统设置    (/home/admin/settings)
操作日志    (/home/admin/logs)
```

**文件变更清单**：

```
✏️  src/page/home/admin/index.vue     → 大改：完整Sidebar + 9个子页
🆕  src/page/home/admin/dashboard.vue → 新建：平台概览页
🆕  src/page/home/admin/users.vue     → 新建：用户管理页
🆕  src/page/home/admin/merchants.vue → 新建：商家管理页
🆕  src/page/home/admin/riders.vue    → 新建：骑手管理页
🆕  src/router/modules/admin.js       → 新建：管理端子路由
✏️  src/components/operation/admin-ops.vue → 改：并入管理端
```

---

### Phase 5：骑手端改造

**参考**：`ref/rider.html`

**Sidebar 菜单项（骑手端）**：

```
今日概况    (/home/driver/dashboard)
────────────────────────────────
接单大厅    (/home/driver/available)
配送中      (/home/driver/delivering)
我的收益    (/home/driver/earnings)
```

**文件变更清单**：

```
✏️  src/page/home/driver/index.vue   → 大改：加Sidebar
🆕  src/components/driver/order-card.vue → 新建：可接订单卡片
🆕  src/components/driver/delivery-progress.vue → 新建：配送进度组件
🆕  src/router/modules/driver.js     → 新建：骑手端子路由
```

---

### Phase 6：收尾优化

```
收尾清单：
├── [ ] 响应式适配：breakpoint 960px/640px（参考ref的media query）
├── [ ] 皮肤切换：4角色主题色动态切换（CSS变量覆盖）
├── [ ] 过渡动画：页面切换 fade/slide 效果
├── [ ] 字体优化：引入系统字体栈（已在ref中定义）
└── [ ] 浏览器测试：Chrome/Edge/Firefox 兼容性
```

---

## 四、风险与应对

| 风险 | 影响 | 应对方案 |
|------|------|---------|
| `el-tabs` 改 Sidebar 导致路由逻辑大改 | 高 | Phase 2 先做商家端，验证可行再推广 |
| Element Plus 默认样式覆盖困难 | 中 | 用 `:deep()` 强制覆盖，或参考 ref 用纯 HTML+CSS 重写关键组件 |
| 现有功能回归 | 中 | 每 Phase 完成做冒烟测试，保留 `el-tabs` 作为 fallback |
| CSS 变量名冲突 | 低 | 统一用 `--role-*` 前缀（如 `--merchant-primary`） |

---

## 五、执行跟踪

> 每完成一个 Phase，在此更新进度

- [ ] Phase 1：基础设施层
- [ ] Phase 2：商家端改造
- [ ] Phase 3：顾客端改造
- [ ] Phase 4：管理端改造
- [ ] Phase 5：骑手端改造
- [ ] Phase 6：收尾优化

---

*本文档随改造进度持续更新*
