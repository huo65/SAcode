# 后端说明

## Database

后端使用 Spring Boot + MyBatis + MySQL，按 Controller / Service / Mapper / POJO 分层组织。

初始化数据库只使用一个脚本：

```sql
source src/main/resources/schema.sql;
```

`schema.sql` 包含核心表结构、索引、配送/订单字段、钱包流水、演示账号和种子地址。演示账号默认密码为 `123456`，首次成功登录后会迁移为 SHA-256 哈希。

## Configuration

保留 `src/main/resources/application.example.yml` 作为提交模板。本地运行时使用自己的 `application.yml` 或环境变量配置数据库和 JWT。课堂展示版支付采用钱包余额模拟，支付宝沙箱参数只作为后续第三方支付扩展。

JWT secret 从 `DBMARKET_JWT_SECRET` 读取；未配置时使用开发回退值。

## Verification and Run

原项目自带 Maven Wrapper 不完整，建议使用本地 Maven：

```powershell
mvn test
mvn spring-boot:run
```

如果 Maven 私有镜像导致依赖解析失败，先修复本机 `settings.xml` 或切换到可用镜像，再判断是否为代码问题。

This project also includes `maven-settings-public.xml` for bypassing a broken local mirror:

```powershell
mvn -s maven-settings-public.xml test
```

If a global Maven settings file still injects a broken mirror, override both global and user settings:

```powershell
mvn -gs maven-settings-public.xml -s maven-settings-public.xml test
```

## Main Modules

| 模块 | 说明 |
| --- | --- |
| `UserController` | 登录注册、用户信息、地址、余额充值、余额支付、钱包查询、用户管理 |
| `ProductController` | 商品新增、搜索、详情、更新、审核 |
| `RestaurantController` | 餐厅列表/详情、商家门店资料 |
| `OrderInfoController` | 订单查询、过滤、状态流转 |
| `OrderReviewController` | 顾客评价、商家回复 |
| `AfterSaleController` | 售后工单创建、查询、处理、统计 |
| `OperationsController` | RBAC、平台看板、商家经营分析、审计日志、管理员钱包流水查询 |
| `StorageController` | 本地文件上传 |
| `AliPayController` | 支付宝沙箱支付、回调、退款、查询；课堂展示主链路不使用 |

## Wallet Demo Flow

课堂展示支付链路：

1. 顾客调用 `/user/recharge` 充值余额，模拟第三方支付入账。
2. 顾客下单后调用 `/user/pay`，后端计算订单金额并完成余额扣减、库存扣减、订单状态更新和钱包流水记录。
3. 商家取消或确认退款时，通过订单状态机触发余额退款、库存恢复、退款流水和审计日志。
4. 顾客可用 `/user/wallet`、`/user/wallet/transactions` 查看自己的余额和流水；管理员可用 `/ops/wallet/transactions` 查看平台钱包流水。

后续计划统一维护在 [`../../docs/需求进度与后续计划.md`](../../docs/需求进度与后续计划.md)。
