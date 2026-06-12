# Phase 3 门店实体化改造说明

## 1. 改造目标

本阶段按课堂展示版标准推进，目标是把当前“按商家聚合商品”的基础餐厅能力升级为“独立门店实体 + 商家门店资料管理 + 顾客端门店化展示”的完整演示闭环。

本轮强调：

- 优先保证顾客端看到的是“门店”而不是“商品集合”。
- 优先保证商家端能够直接维护门店资料，并立即反映到顾客端。
- 优先保证课堂展示效果，包括筛选排序、营业状态、配送规则、门店视觉展示。
- 不追求生产级地图、真实地理距离和复杂多门店体系，先实现课堂展示版最小闭环。

## 2. 本轮完成内容

### 2.1 后端

- 新增 `restaurant` 门店表，承载门店名称、Logo、封面、公告、营业状态、营业时间、配送费、起送价、配送范围、菜单分类等运营字段。
- 新增 `RestaurantStore`、`RestaurantStoreMapper`。
- 扩展餐厅服务：
  - `/restaurant/list` 支持 `keyword`、`category`、`sortBy`、`onlyOpen`、`minScore`
  - `/restaurant/info` 返回更完整的门店资料
  - `/restaurant/manage/info` 提供商家端门店资料读取
  - `/restaurant/manage/update` 提供商家端门店资料保存
- 保留课堂展示版兼容策略：
  - 若门店资料未完全配置，系统回退到商家信息、地址、商品和评价做默认聚合
  - 距离使用展示版估算值，不依赖真实地图 SDK

### 2.2 前端

- 顾客端餐厅列表页重做：
  - 增加门店视觉 Hero 区
  - 增加分类、关键词、排序、营业中、最低评分筛选
  - 每张门店卡片展示门店状态、距离、配送费、起送价、评分、标签和公告
- 顾客端餐厅详情弹窗重做：
  - 增加门店封面、营业状态、营业时间、配送规则、活动文案、距离、预计送达
  - 增加门店菜单分区展示
  - 保留商品详情打开能力
  - 优化评价和商家回复展示
- 商家端新增 `Store` 门店资料页：
  - 可维护门店名称、封面、Logo、营业状态、营业时间、公告、配送费、起送价、配送说明、标签、菜单分类等
  - 提供顾客端预览卡片，便于课堂展示前快速校对
- 商家首页视觉重构：
  - 新增门店运营 Hero 区
  - 在标签页中增加 `Store` 入口

## 3. 课堂展示版取舍

以下能力本轮按“可演示即可”的标准处理：

- 距离：采用展示版估算值，而不是接入地图和真实坐标
- 门店与商家关系：当前仍按“一商家一门店”推进，避免扩展到复杂多门店模型
- 菜单分类归属门店：以门店资料中的菜单分类配置驱动展示，减少对全局分类的直接暴露
- 门店运营字段：优先展示营业、配送、公告、活动文案，暂不扩展到完整经营分析体系

## 4. 主要影响范围

### 4.1 后端文件

- `backend/DB-market/src/main/resources/schema.sql`
- `backend/DB-market/src/main/java/com/DB/DBmarket/mapper/RestaurantStoreMapper.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/pojo/restaurant/RestaurantStore.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/pojo/restaurant/RestaurantSummary.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/pojo/restaurant/RestaurantDetail.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/service/RestaurantService.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/service/impl/RestaurantServiceImpl.java`
- `backend/DB-market/src/main/java/com/DB/DBmarket/controller/RestaurantController.java`

### 4.2 前端文件

- `front/src/api/apis.js`
- `front/src/components/restaurant/index.vue`
- `front/src/components/restaurant/restaurant-dialog.vue`
- `front/src/components/restaurant/store-manage.vue`
- `front/src/page/home/merchant/index.vue`

## 5. 后续可选深化方向

- 接入真实地图 SDK，替换展示版距离估算
- 支持一个商家管理多个门店
- 给商品增加更强的门店菜单分类归属关系
- 增加门店营业日历、门店配送范围地图、多维排序
- 增加门店经营报表和商家销售统计联动
