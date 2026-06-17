# 外卖订单系统

本项目是一个前后端分离的课堂展示版外卖订单系统，覆盖顾客、商家、骑手、管理员四类角色。前端使用 Vue 3 + Vite + Element Plus，后端使用 Spring Boot + MyBatis + MySQL，业务图片由后端本地存储目录统一提供。

## 项目组成

| 路径 | 说明 |
| --- | --- |
| `front` | Vue 前端工程，包含登录页、四类角色页面、路由、接口封装和前端测试 |
| `backend/DB-market` | Spring Boot 后端工程，包含 Controller、Service、Mapper、POJO、数据库脚本和本地图片存储 |
| `backend/DB-market/src/main/resources/schema.sql` | MySQL 初始化脚本，已同步当前数据库的表结构和最新数据 |
| `backend/DB-market/src/main/resources/application.example.yml` | 后端配置模板 |
| `backend/DB-market/data/storage` | 后端静态业务图片，浏览器通过 `/storage/...` 访问 |
| `docs` | 需求进度、联调记录、验收清单等项目文档 |

## 环境准备

- JDK 8
- Maven 3.6+
- MySQL 8.x
- Node.js 18.x
- npm 10.x

## 数据库初始化

1. 启动 MySQL。
2. 在项目后端目录执行数据库脚本：

```powershell
cd backend/DB-market
mysql --default-character-set=utf8mb4 -uroot -p < src/main/resources/schema.sql
```

`schema.sql` 会创建并使用 `market` 数据库，重建脚本内包含的表，并导入当前数据库中的最新演示数据。导入时务必带上 `--default-character-set=utf8mb4`，否则中文数据可能出现乱码。

当前脚本已从本机 `market` 数据库重新导出，包含用户、分类、商品、商品图片、门店、订单、评价、售后工单、钱包流水、审计日志和权限配置等数据。

## 后端配置

后端默认读取 `backend/DB-market/src/main/resources/application.yml`。仓库中提供了 `application.example.yml` 作为模板；如果本地没有配置文件，可复制一份：

```powershell
cd backend/DB-market
copy src/main/resources/application.example.yml src/main/resources/application.yml
```

按本机 MySQL 情况修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/market?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
```

也可以不改文件，直接使用环境变量覆盖：

| 环境变量 | 说明 |
| --- | --- |
| `DBMARKET_DB_URL` | MySQL 连接地址 |
| `DBMARKET_DB_USERNAME` | MySQL 用户名 |
| `DBMARKET_DB_PASSWORD` | MySQL 密码 |
| `DBMARKET_STORAGE_DIR` | 本地图片存储目录，默认 `data/storage` |
| `DBMARKET_STORAGE_PUBLIC_PREFIX` | 静态图片 URL 前缀，默认 `/storage` |
| `DBMARKET_JWT_SECRET` | JWT 签名密钥，建议本地自行设置 |

支付宝沙箱参数保留在配置中作为扩展项；课堂展示主链路使用钱包余额充值、支付和退款。

## 启动流程

先启动后端：

```powershell
cd backend/DB-market
mvn spring-boot:run
```

后端默认端口为 `8080`。

再启动前端：

```powershell
cd front
npm install
npm run dev
```

Vite 启动后会在终端输出访问地址，通常是 `http://localhost:5173/` 或相邻可用端口。前端接口使用相对路径，请保持后端 `8080` 服务运行。

可选检查命令：

```powershell
cd backend/DB-market
mvn test

cd ../../front
npm test
npm run build
```

## 登录账户

四个演示账号的密码均为 `123456`。数据库中可能保存为 SHA-256 哈希，仍然使用明文 `123456` 登录。

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `123456` | 商品审核、用户管理、售后总控、平台看板、权限和审计 |
| 顾客 | `customer` | `123456` | 浏览门店、加购下单、余额支付、确认收货、评价、售后 |
| 商家 | `merchant` | `123456` | 商品管理、订单处理、门店资料、售后处理、经营分析 |
| 骑手 | `driver` | `123456` | 待接单、配送中、历史订单、收入和个人状态 |

## 简单使用介绍

1. 管理员登录后，可进入商品治理、用户管理、售后工单、治理与经营等页面，检查平台整体数据。
2. 顾客登录后，可浏览餐厅和商品，加入购物车，提交订单，通过余额完成支付，并在订单页确认收货、评价或发起售后。
3. 商家登录后，可维护商品和门店资料，处理顾客订单，回复评价，处理售后工单，并查看经营分析。
4. 骑手登录后，可查看待接单订单，接单后进入配送流程，并查看配送历史与收入概览。

常用接口和模块说明见 [backend/DB-market/DEVELOPMENT.md](backend/DB-market/DEVELOPMENT.md) 和 [front/README.md](front/README.md)。
