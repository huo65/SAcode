# SA外卖平台 · 项目长期记忆

## 项目概况
- 项目路径：`C:\Users\huozj\Desktop\SA\SAcode\front`
- 技术栈：Vue 3 + Vite + Element Plus + Less + Vue-Router + Vuex
- 后端地址：`//127.0.0.1:8080`

## UI 改造进度（参考 UI_REFACTOR_PLAN.md）
- Phase 1（基础设施）✅ 完成
- Phase 2（商家端改造）✅ 完成 — 含子页面 UI 优化（store-manage, merchant-ops, merchant-reply, info）
- Phase 3（顾客端）✅ 完成 — 含首页、购物车、钱包、地址、评价页
- Phase 4（管理端）✅ 完成 — 含子页面 UI 优化（category, user, admin-ops, wallet, audit）
- Phase 5（骑手端）✅ 完成 — 含概况、待接单、配送中、历史、收益页
- Phase 6（收尾）🔄 待浏览器验证

## 关键设计决策
1. **路由策略**：商家端使用子路由（`/home/merchant/xxx`），首页 `home/index.vue` 按角色分发
2. **主题切换**：`AppSidebar` 组件自动给 `body` 添加 `role-merchant/role-customer/role-admin/role-rider` class，驱动 CSS 变量覆盖
3. **样式覆盖**：商家端 `index.vue` 有全局非 scoped 样式块，用 `.page-content` 前缀深度覆盖子组件
4. **旧版兼容**：`theme.less` 顶部保留了 `@price/@main-yellow/@light-yellow` 旧变量，若有组件引用不会报错

## 文件约定
- 新增角色子路由：`src/router/modules/{role}.js`，在 `router/index.js` 中用 `...routes` 展开
- AppSidebar props：`role / currentPage / badges / storeOpen / mobileOpen`
- 新增样式组件请放 `src/style/` 并在需要处 `@import`

## 注意事项
- `header.vue` 被简化，移除了 `page-shell` 包装，不再自带 padding
- 原有商家端 `el-tabs` 逻辑已移除，tab切换改为路由导航
- `refreshDataFnMap` 在 store 中仍然存在，子组件 onMounted 时应继续注册

## UI 组件替换规范
- `el-button` → `.btn` 体系（.btn-primary/.btn-outline/.btn-success/.btn-warning/.btn-danger/.btn-sm）
- `el-dialog` → `.modal-overlay` + `.modal`（.modal-header/.modal-body/.modal-footer）
- `el-form`/`el-form-item` → `.form-group` + `.form-label` + native input/select/textarea
- `el-descriptions` → `.info-grid` + `.info-row` 网格信息展示
- `el-table` → `.custom-table`（原生 table + CSS）
- `el-tag` → `.badge` 体系（.badge-success/.badge-warning/.badge-danger/.badge-info/.badge-primary）
- `el-rate` → `.star-rating`（★ 字符）
- `el-select` → native `<select>` + `.form-select`
- `el-input` → native `<input>` + `.form-input` / `.search-input`
- `el-switch` → `.toggle-switch`（纯 CSS checkbox 模拟）
- 保留 `el-upload`（文件上传复杂逻辑）、`el-date-picker`（日期范围选择）、`el-pagination`（分页）
- 商家端主色 #E8652B（橙），管理员端主色 #4F46E5（靛蓝），顾客端 #EF4444（红），骑手端 #059669（绿）
