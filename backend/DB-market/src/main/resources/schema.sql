
-- DB-market database initialization script.
-- Exported from the latest local `market` database snapshot.
-- Import with an UTF-8 client, for example:
-- mysql --default-character-set=utf8mb4 -uroot -p < schema.sql

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `market` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `market`;
DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `addr_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地址id',
  `usr` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户id',
  `location` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地址位置描述',
  PRIMARY KEY (`addr_id`),
  KEY `idx_address_usr` (`usr`),
  CONSTRAINT `address_ibfk_1` FOREIGN KEY (`usr`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` (`addr_id`, `usr`, `location`) VALUES ('1','2','Customer demo address'),('2','3','Merchant demo address'),('3','4','Driver service area');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `after_sale_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `after_sale_ticket` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工单id',
  `order_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联订单id',
  `customer_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '顾客id',
  `merchant_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家id',
  `type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工单类型',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题描述',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工单状态',
  `handler_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理人id',
  `handler_note` text COLLATE utf8mb4_unicode_ci COMMENT '处理备注',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_after_sale_customer` (`customer_id`),
  KEY `idx_after_sale_merchant` (`merchant_id`),
  KEY `idx_after_sale_status` (`status`),
  KEY `idx_after_sale_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='售后工单表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `after_sale_ticket` WRITE;
/*!40000 ALTER TABLE `after_sale_ticket` DISABLE KEYS */;
INSERT INTO `after_sale_ticket` (`id`, `order_id`, `customer_id`, `merchant_id`, `type`, `content`, `status`, `handler_id`, `handler_note`, `created_time`, `updated_time`) VALUES ('11162415','19448242','2','3','投诉反馈','自动化演示工单：包装轻微破损，申请客服跟进。','待处理',NULL,NULL,'2026-06-16 16:03:13','2026-06-16 16:03:13'),('demo-ticket-after-sale','demo-order-after-sale','2','3','投诉反馈','自动化演示工单：包装轻微破损，申请客服跟进。','待处理',NULL,NULL,'2026-06-16 16:03:13','2026-06-16 16:03:13');
/*!40000 ALTER TABLE `after_sale_ticket` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `cus` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户id',
  `prod` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品id',
  `number` int NOT NULL DEFAULT '1' COMMENT '商品数量',
  PRIMARY KEY (`cus`,`prod`),
  KEY `idx_cart_cus` (`cus`),
  KEY `idx_cart_prod` (`prod`),
  CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`cus`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cart_ibfk_2` FOREIGN KEY (`prod`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品类别名称',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品类别表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` (`name`) VALUES ('其他'),('家居'),('数码产品'),('服装'),('生鲜蔬菜'),('食品'),('饮品');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `time_slot` datetime NOT NULL COMMENT '发送时间',
  `sender` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送人id',
  `receiver` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接受人id',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '信息内容',
  PRIMARY KEY (`time_slot`,`sender`,`receiver`),
  KEY `idx_message_sender` (`sender`),
  KEY `idx_message_receiver` (`receiver`),
  CONSTRAINT `message_ibfk_1` FOREIGN KEY (`sender`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `message_ibfk_2` FOREIGN KEY (`receiver`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `operation_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_audit_log` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志id',
  `actor_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人id',
  `actor_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人名称',
  `actor_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人角色',
  `action_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动作类型',
  `target_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标类型',
  `target_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标id',
  `target_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标名称',
  `detail` text COLLATE utf8mb4_unicode_ci COMMENT '动作描述',
  `result` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '处理结果',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operation_audit_actor` (`actor_id`),
  KEY `idx_operation_audit_type` (`action_type`),
  KEY `idx_operation_audit_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂展示版操作审计日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `operation_audit_log` WRITE;
/*!40000 ALTER TABLE `operation_audit_log` DISABLE KEYS */;
INSERT INTO `operation_audit_log` (`id`, `actor_id`, `actor_name`, `actor_type`, `action_type`, `target_type`, `target_id`, `target_name`, `detail`, `result`, `created_time`) VALUES ('13727764','2','customer','cus','WALLET_PAY','order','19448242','customer','余额支付订单，金额 32','SUCCESS','2026-06-16 16:03:13'),('17445208','1','admin','admin','PRODUCT_AUDIT','product','52163670','52163670','商品审核状态更新为 1','SUCCESS','2026-06-16 16:01:40'),('20675943','1','admin','admin','PRODUCT_AUDIT','product','64810048','64810048','商品审核状态更新为 1','SUCCESS','2026-06-16 16:01:41'),('26427883','2','customer','cus','WALLET_PAY','order','29746852','customer','余额支付订单，金额 32','SUCCESS','2026-06-16 16:01:42'),('34735851','1','admin','admin','WALLET_RECHARGE','wallet','2','2','钱包模拟充值 500','SUCCESS','2026-06-16 16:01:41'),('41817509','2','customer','cus','WALLET_PAY','order','38343677','customer','余额支付订单，金额 28','SUCCESS','2026-06-16 16:01:42'),('42469963','2','customer','cus','WALLET_PAY','order','88219613','customer','余额支付订单，金额 12','SUCCESS','2026-06-16 16:03:13'),('54423477','2','customer','cus','WALLET_PAY','order','56362982','customer','余额支付订单，金额 10','SUCCESS','2026-06-17 19:13:08'),('61484714','1','admin','admin','PRODUCT_AUDIT','product','95428839','95428839','商品审核状态更新为 1','SUCCESS','2026-06-16 16:01:41'),('78114007','1','admin','admin','PRODUCT_AUDIT','product','23470833','23470833','商品审核状态更新为 1','SUCCESS','2026-06-16 16:01:40'),('92265102','3','merchant','mer','WALLET_REFUND','order','19448242','19448242','订单退款回滚，金额 32','SUCCESS','2026-06-16 16:19:50'),('95272388','2','customer','cus','WALLET_PAY','order','14964582','customer','余额支付订单，金额 24','SUCCESS','2026-06-16 16:01:42'),('98133305','2','customer','cus','WALLET_PAY','order','25163700','customer','余额支付订单，金额 25','SUCCESS','2026-06-16 16:01:42'),('99930850','2','customer','cus','WALLET_PAY','order','64002977','customer','余额支付订单，金额 28','SUCCESS','2026-06-16 16:18:47');
/*!40000 ALTER TABLE `operation_audit_log` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `operation_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_permission` (
  `role_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `permission_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限键',
  `permission_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `permission_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限类型 menu/action',
  `scope_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'admin/merchant',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '1启用 0禁用',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`role_code`,`permission_key`),
  KEY `idx_operation_permission_scope` (`scope_code`,`permission_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂展示版权限配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `operation_permission` WRITE;
/*!40000 ALTER TABLE `operation_permission` DISABLE KEYS */;
INSERT INTO `operation_permission` (`role_code`, `permission_key`, `permission_name`, `permission_type`, `scope_code`, `enabled`, `updated_time`) VALUES ('admin','admin.action.afterSale.handle','处理售后工单','action','admin',1,'2026-06-17 11:20:33'),('admin','admin.action.audit.view','查看审计日志','action','admin',1,'2026-06-17 11:20:33'),('admin','admin.action.category.manage','维护商品分类','action','admin',1,'2026-06-17 11:20:33'),('admin','admin.action.dashboard.view','查看平台看板','action','admin',1,'2026-06-17 11:20:33'),('admin','admin.action.permission.manage','维护权限配置','action','admin',1,'2026-06-17 11:20:33'),('admin','admin.action.product.audit','审核商品','action','admin',1,'2026-06-17 11:20:33'),('admin','admin.action.user.disable','启停用户账号','action','admin',1,'2026-06-17 11:20:33'),('admin','admin.action.user.view','查看用户列表','action','admin',1,'2026-06-17 11:20:33'),('admin','admin.action.wallet.view','查看钱包流水','action','admin',1,'2026-06-17 11:20:33'),('admin','admin.menu.afterSale','售后工单','menu','admin',1,'2026-06-17 11:20:33'),('admin','admin.menu.category','分类管理','menu','admin',1,'2026-06-17 11:20:33'),('admin','admin.menu.goods','商品治理','menu','admin',1,'2026-06-17 11:20:33'),('admin','admin.menu.ops','治理与经营','menu','admin',1,'2026-06-17 11:20:33'),('admin','admin.menu.order','订单总览','menu','admin',1,'2026-06-17 11:20:33'),('admin','admin.menu.user','用户管理','menu','admin',1,'2026-06-17 11:20:33'),('mer','merchant.action.afterSale.handle','处理售后工单','action','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.action.audit.view','查看操作日志','action','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.action.dashboard.view','查看经营看板','action','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.action.report.export','导出经营报表','action','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.action.store.manage','维护门店资料','action','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.menu.afterSale','售后处理','menu','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.menu.goods','商品管理','menu','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.menu.info','账号信息','menu','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.menu.ops','经营分析','menu','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.menu.order','订单处理','menu','merchant',1,'2026-06-17 11:20:33'),('mer','merchant.menu.store','门店资料','menu','merchant',1,'2026-06-17 11:20:33');
/*!40000 ALTER TABLE `operation_permission` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `order_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_info` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单id',
  `cus` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '买家Id',
  `mer` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卖家Id',
  `prod` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品Id',
  `prod_num` int NOT NULL COMMENT '下单购买数量',
  `time` datetime NOT NULL COMMENT '下单时间',
  `deli_addr` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发货地址id',
  `rec_addr` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货地址id',
  `state` int NOT NULL DEFAULT '-1' COMMENT '订单状态 -3已退货/退款 -2退货中 -1下单未支付 0已支付 1已发货 2已收货',
  `account` int NOT NULL COMMENT '订单金额',
  `driver_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'driver user id',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'customer remark',
  `expected_delivery_time` datetime DEFAULT NULL COMMENT 'expected delivery time',
  `pay_time` datetime DEFAULT NULL COMMENT 'payment time',
  `complain` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '是否被投诉 0未被投诉，1被投诉',
  `complain_reason` text COLLATE utf8mb4_unicode_ci COMMENT '投诉理由',
  `refund_reason` text COLLATE utf8mb4_unicode_ci COMMENT '退款理由，请求退款',
  PRIMARY KEY (`id`,`prod`),
  KEY `idx_order_info_cus` (`cus`),
  KEY `idx_order_info_mer` (`mer`),
  KEY `idx_order_info_prod` (`prod`),
  KEY `idx_order_info_state` (`state`),
  KEY `idx_order_info_driver_state` (`driver_id`,`state`),
  CONSTRAINT `order_info_ibfk_1` FOREIGN KEY (`cus`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_info_ibfk_2` FOREIGN KEY (`mer`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_info_ibfk_3` FOREIGN KEY (`prod`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `order_info` WRITE;
/*!40000 ALTER TABLE `order_info` DISABLE KEYS */;
INSERT INTO `order_info` (`id`, `cus`, `mer`, `prod`, `prod_num`, `time`, `deli_addr`, `rec_addr`, `state`, `account`, `driver_id`, `remark`, `expected_delivery_time`, `pay_time`, `complain`, `complain_reason`, `refund_reason`) VALUES ('14964582','2','3','64810048',2,'2026-06-16 16:01:42','2','1',4,24,NULL,'AUTO_DEMO_PREPARING',NULL,'2026-06-16 16:01:42','0',NULL,NULL),('15120985','2','3','asset-prod-iphone15',1,'2026-06-17 21:44:56','2','1',-1,5999,NULL,NULL,NULL,NULL,'0',NULL,NULL),('19448242','2','3','52163670',1,'2026-06-16 16:19:50','2','1',-3,32,'4','AUTO_DEMO_AFTER_SALE',NULL,'2026-06-16 16:03:13','1','自动化演示工单：包装轻微破损，申请客服跟进。',NULL),('25163700','2','3','95428839',1,'2026-06-17 21:19:46','2','1',2,25,'4','AUTO_DEMO_WAITING_DRIVER',NULL,'2026-06-16 16:01:42','0',NULL,NULL),('29746852','2','3','52163670',1,'2026-06-16 16:01:41','2','1',0,32,NULL,'AUTO_DEMO_PAID',NULL,'2026-06-16 16:01:41','0',NULL,NULL),('38343677','2','3','23470833',1,'2026-06-16 16:01:42','2','1',2,28,'4','AUTO_DEMO_COMPLETED_REVIEW',NULL,'2026-06-16 16:01:42','0',NULL,NULL),('56362982','2','3','asset-prod-pepper',1,'2026-06-17 21:17:58','2','1',2,10,'4',NULL,NULL,'2026-06-17 19:13:08','0',NULL,NULL),('64002977','2','3','23470833',1,'2026-06-16 16:18:47','2','1',0,28,NULL,'AUTO_DEMO_UNPAID',NULL,'2026-06-16 16:18:47','0',NULL,NULL),('88219613','2','3','64810048',1,'2026-06-16 16:19:17','2','1',-2,12,'4','AUTO_DEMO_DELIVERING',NULL,'2026-06-16 16:03:13','0',NULL,NULL),('demo-order-after-sale','2','3','demo-prod-tomato-beef',1,'2026-06-16 16:03:13','2','1',2,32,'4','AUTO_DEMO_AFTER_SALE',NULL,'2026-06-16 16:03:13','1','自动化演示工单：包装轻微破损，申请客服跟进。',NULL),('demo-order-completed-review','2','3','demo-prod-chicken-rice',1,'2026-06-16 16:01:42','2','1',2,28,'4','AUTO_DEMO_COMPLETED_REVIEW',NULL,'2026-06-16 16:01:42','0',NULL,NULL),('demo-order-delivering','2','3','demo-prod-lemon-tea',1,'2026-06-17 21:17:47','2','1',2,12,'4','AUTO_DEMO_DELIVERING',NULL,'2026-06-16 16:03:13','0',NULL,NULL),('demo-order-paid','2','3','demo-prod-tomato-beef',1,'2026-06-16 16:01:41','2','1',0,32,NULL,'AUTO_DEMO_PAID',NULL,'2026-06-16 16:01:41','0',NULL,NULL),('demo-order-preparing','2','3','demo-prod-lemon-tea',2,'2026-06-16 16:01:42','2','1',4,24,NULL,'AUTO_DEMO_PREPARING',NULL,'2026-06-16 16:01:42','0',NULL,NULL),('demo-order-unpaid','2','3','demo-prod-chicken-rice',1,'2026-06-16 16:01:41','2','1',-1,28,NULL,'AUTO_DEMO_UNPAID',NULL,NULL,'0',NULL,NULL),('demo-order-waiting-driver','2','3','demo-prod-beef-burger',1,'2026-06-17 21:20:38','2','1',2,25,'4','AUTO_DEMO_WAITING_DRIVER',NULL,'2026-06-16 16:01:42','0',NULL,NULL);
/*!40000 ALTER TABLE `order_info` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `order_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_review` (
  `order_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单id',
  `cus` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评价顾客id',
  `mer` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被评价商家id',
  `score` int NOT NULL COMMENT '评分 1-5',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评价内容',
  `created_time` datetime NOT NULL COMMENT '评价时间',
  `reply_content` text COLLATE utf8mb4_unicode_ci COMMENT '商家回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '商家回复时间',
  PRIMARY KEY (`order_id`),
  KEY `idx_order_review_mer` (`mer`),
  KEY `idx_order_review_cus` (`cus`),
  CONSTRAINT `order_review_ibfk_1` FOREIGN KEY (`cus`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_review_ibfk_2` FOREIGN KEY (`mer`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `order_review` WRITE;
/*!40000 ALTER TABLE `order_review` DISABLE KEYS */;
INSERT INTO `order_review` (`order_id`, `cus`, `mer`, `score`, `content`, `created_time`, `reply_content`, `reply_time`) VALUES ('38343677','2','3',5,'配送及时，餐品状态很好，适合作为课堂展示评价。','2026-06-16 16:03:13','感谢评价，我们会继续保持出餐速度和服务质量。','2026-06-16 16:03:13'),('demo-order-after-sale','2','3',5,'评价测试','2026-06-17 19:20:34',NULL,NULL),('demo-order-completed-review','2','3',5,'配送及时，餐品状态很好，适合作为课堂展示评价。','2026-06-16 16:03:13','感谢评价，我们会继续保持出餐速度和服务质量。','2026-06-16 16:03:13');
/*!40000 ALTER TABLE `order_review` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `prod_img`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prod_img` (
  `prod` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品id',
  `image` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品图片',
  PRIMARY KEY (`prod`,`image`),
  KEY `idx_prod_img_prod` (`prod`),
  CONSTRAINT `prod_img_ibfk_1` FOREIGN KEY (`prod`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `prod_img` WRITE;
/*!40000 ALTER TABLE `prod_img` DISABLE KEYS */;
INSERT INTO `prod_img` (`prod`, `image`) VALUES ('23470833','/storage/product/food/beef-1.png'),('23470833','/storage/product/food/beef-2.png'),('52163670','/storage/product/food/tomato-meal-1.png'),('52163670','/storage/product/food/tomato-meal-2.png'),('64810048','/storage/product/drink/coca-1.jpg'),('64810048','/storage/product/drink/pepsi-1.jpg'),('95428839','/storage/product/food/pork-1.png'),('95428839','/storage/product/food/pork-2.png'),('asset-prod-braised-beef','/storage/product/food/beef-1.png'),('asset-prod-braised-beef','/storage/product/food/beef-2.png'),('asset-prod-cabbage','/storage/product/vegetable/cabbage-1.png'),('asset-prod-cabbage','/storage/product/vegetable/cabbage-2.png'),('asset-prod-cabbage','/storage/product/vegetable/cabbage-3.png'),('asset-prod-celery','/storage/product/vegetable/celery-1.png'),('asset-prod-celery','/storage/product/vegetable/celery-2.png'),('asset-prod-celery','/storage/product/vegetable/celery-3.png'),('asset-prod-chives','/storage/product/vegetable/chives-1.png'),('asset-prod-chives','/storage/product/vegetable/chives-2.png'),('asset-prod-coca','/storage/product/drink/coca-1.jpg'),('asset-prod-coca','/storage/product/drink/coca-2.jpg'),('asset-prod-coca','/storage/product/drink/coca-3.jpg'),('asset-prod-iphone15','/storage/product/digital/iphone15-1.png'),('asset-prod-iphone15','/storage/product/digital/iphone15-2.png'),('asset-prod-laptop','/storage/product/digital/laptop-1.png'),('asset-prod-laptop','/storage/product/digital/laptop-2.png'),('asset-prod-laptop','/storage/product/digital/laptop-3.png'),('asset-prod-mate60','/storage/product/digital/mate60-pro-1.png'),('asset-prod-mate60','/storage/product/digital/mate60-pro-2.png'),('asset-prod-onion','/storage/product/vegetable/onion-1.png'),('asset-prod-onion','/storage/product/vegetable/onion-2.png'),('asset-prod-pepper','/storage/product/vegetable/pepper-1.png'),('asset-prod-pepper','/storage/product/vegetable/pepper-2.png'),('asset-prod-pepper','/storage/product/vegetable/pepper-3.png'),('asset-prod-pepsi','/storage/product/drink/pepsi-1.jpg'),('asset-prod-pepsi','/storage/product/drink/pepsi-2.jpg'),('asset-prod-pepsi','/storage/product/drink/pepsi-3.jpg'),('asset-prod-pork-set','/storage/product/food/pork-1.png'),('asset-prod-pork-set','/storage/product/food/pork-2.png'),('asset-prod-pork-set','/storage/product/food/pork-3.png'),('asset-prod-sprite','/storage/product/drink/sprite-1.png'),('asset-prod-sprite','/storage/product/drink/sprite-2.png'),('asset-prod-tomato-meal','/storage/product/food/tomato-meal-1.png'),('asset-prod-tomato-meal','/storage/product/food/tomato-meal-2.png'),('asset-prod-tomato-meal','/storage/product/food/tomato-meal-3.png'),('demo-prod-beef-burger','/storage/product/food/pork-1.png'),('demo-prod-beef-burger','/storage/product/food/pork-2.png'),('demo-prod-chicken-rice','/storage/product/food/beef-1.png'),('demo-prod-chicken-rice','/storage/product/food/beef-2.png'),('demo-prod-lemon-tea','/storage/product/drink/coca-1.jpg'),('demo-prod-lemon-tea','/storage/product/drink/pepsi-1.jpg'),('demo-prod-tomato-beef','/storage/product/food/tomato-meal-1.png'),('demo-prod-tomato-beef','/storage/product/food/tomato-meal-2.png');
/*!40000 ALTER TABLE `prod_img` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品ID',
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '商品描述',
  `price` int NOT NULL COMMENT '商品价格',
  `mer` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家id',
  `cat_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品类别',
  `number` int DEFAULT '0' COMMENT '商品库存',
  `state` int DEFAULT '0' COMMENT '商品状态(-1 未通过审核 0审核中 1审核通过)',
  `sales_refund` int DEFAULT '0' COMMENT '该商品退货量',
  `rate_refund` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '该商品退货率 %',
  `complain` int DEFAULT '0' COMMENT '该商品投诉量',
  `complain_rate` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '该商品投诉率',
  PRIMARY KEY (`id`),
  KEY `idx_product_mer` (`mer`),
  KEY `idx_product_cat_name` (`cat_name`),
  KEY `idx_product_state` (`state`),
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`cat_name`) REFERENCES `category` (`name`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` (`id`, `name`, `description`, `price`, `mer`, `cat_name`, `number`, `state`, `sales_refund`, `rate_refund`, `complain`, `complain_rate`) VALUES ('23470833','自动化演示-蜜汁鸡腿饭','课堂展示用热销主食，覆盖商品审核、下单和评价链路。',28,'3','食品',78,1,0,'0.0',0,'0.0'),('52163670','自动化演示-番茄牛腩饭','课堂展示用套餐商品，适合演示订单状态流转。',32,'3','食品',69,1,1,'0.5',1,'0.5'),('64810048','自动化演示-冰镇柠檬茶','课堂展示用饮品商品，补充低客单价数据。',12,'3','食品',117,1,0,'0.0',0,'0.0'),('95428839','自动化演示-香煎牛肉堡','课堂展示用快餐商品，覆盖库存和支付演示。',25,'3','食品',59,1,0,'0.0',0,'0.0'),('asset-prod-braised-beef','红烧牛肉饭','迁移自前端素材的主食商品，适合展示热销餐品。',30,'3','食品',88,1,0,'0.0',0,'0.0'),('asset-prod-cabbage','新鲜卷心菜','生鲜蔬菜素材商品，展示非餐品分类。',8,'3','生鲜蔬菜',120,1,0,'0.0',0,'0.0'),('asset-prod-celery','清香芹菜','生鲜蔬菜素材商品，适合筛选展示。',9,'3','生鲜蔬菜',120,1,0,'0.0',0,'0.0'),('asset-prod-chives','新鲜韭菜','生鲜蔬菜素材商品，补充门店菜单。',7,'3','生鲜蔬菜',120,1,0,'0.0',0,'0.0'),('asset-prod-coca','可口可乐','饮品素材商品，适合展示低价饮品。',6,'3','饮品',180,1,0,'0.0',0,'0.0'),('asset-prod-iphone15','iPhone 15 展示机','数码产品素材商品，用于丰富平台商品类型。',5999,'3','数码产品',12,1,0,'0.0',0,'0.0'),('asset-prod-laptop','轻薄笔记本电脑','数码产品素材商品，展示多图商品详情。',5299,'3','数码产品',8,1,0,'0.0',0,'0.0'),('asset-prod-mate60','Mate 60 Pro 展示机','数码产品素材商品，展示高客单价商品。',6999,'3','数码产品',10,1,0,'0.0',0,'0.0'),('asset-prod-onion','紫皮洋葱','生鲜蔬菜素材商品，丰富展示数据。',7,'3','生鲜蔬菜',120,1,0,'0.0',0,'0.0'),('asset-prod-pepper','彩椒组合','生鲜蔬菜素材商品，支持多图轮播展示。',10,'3','生鲜蔬菜',109,1,0,'0.0',0,'0.0'),('asset-prod-pepsi','百事可乐','饮品素材商品，丰富饮品分类。',6,'3','饮品',180,1,0,'0.0',0,'0.0'),('asset-prod-pork-set','香煎猪排套餐','迁移自前端素材的套餐商品，展示多图菜品详情。',29,'3','食品',82,1,0,'0.0',0,'0.0'),('asset-prod-sprite','雪碧','清爽饮品，用于丰富购物车和订单演示。',6,'3','饮品',160,1,0,'0.0',0,'0.0'),('asset-prod-tomato-meal','番茄浓汤套餐','番茄风味套餐，补充餐品类展示数据。',26,'3','食品',76,1,0,'0.0',0,'0.0'),('demo-prod-beef-burger','自动化演示-香煎牛肉堡','课堂展示用快餐商品，覆盖库存和支付演示。',25,'3','食品',59,1,0,'0.0',0,'0.0'),('demo-prod-chicken-rice','自动化演示-蜜汁鸡腿饭','课堂展示用热销主食，覆盖商品审核、下单和评价链路。',28,'3','食品',79,1,0,'0.0',0,'0.0'),('demo-prod-lemon-tea','自动化演示-冰镇柠檬茶','课堂展示用饮品商品，补充低客单价数据。',12,'3','饮品',117,1,0,'0.0',0,'0.0'),('demo-prod-tomato-beef','自动化演示-番茄牛腩饭','课堂展示用套餐商品，适合演示订单状态流转。',32,'3','食品',68,1,0,'0.0',1,'0.5');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `restaurant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `restaurant` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'restaurant id',
  `merchant_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'merchant user id',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'restaurant name',
  `logo` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'logo',
  `cover` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'cover',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'description',
  `notice` text COLLATE utf8mb4_unicode_ci COMMENT 'notice',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1 open, 0 closed',
  `business_hours` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'business hours',
  `delivery_fee` int DEFAULT '0' COMMENT 'delivery fee',
  `min_order_amount` int DEFAULT '0' COMMENT 'minimum order amount',
  `service_radius_km` double DEFAULT '5' COMMENT 'service radius',
  `delivery_eta_minutes` int DEFAULT '30' COMMENT 'eta minutes',
  `feature_tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'display tags separated by comma',
  `menu_categories` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'menu categories separated by comma',
  `address_text` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'restaurant address',
  `delivery_policy` text COLLATE utf8mb4_unicode_ci COMMENT 'delivery policy',
  `promo_text` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'promo text',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_restaurant_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `restaurant` WRITE;
/*!40000 ALTER TABLE `restaurant` DISABLE KEYS */;
INSERT INTO `restaurant` (`id`, `merchant_id`, `name`, `logo`, `cover`, `description`, `notice`, `status`, `business_hours`, `delivery_fee`, `min_order_amount`, `service_radius_km`, `delivery_eta_minutes`, `feature_tags`, `menu_categories`, `address_text`, `delivery_policy`, `promo_text`) VALUES ('3','3','merchant精选门店','/storage/restaurant/merchant-logo.jpg','/storage/restaurant/merchant-cover.jpg','课堂展示版门店示例，支持门店资料、排序筛选和详情展示。','欢迎光临，当前门店已切换为课堂展示版资料。',1,'10:00-21:30',4,18,5,28,'品牌门店,课堂展示推荐,当日现做','招牌套餐,热销主食,小吃饮品,新鲜蔬菜,数码优选','Merchant demo address','满18元起送，支持骑手课堂展示版配送。','新客首单享门店展示优惠');
/*!40000 ALTER TABLE `restaurant` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户唯一标识',
  `type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户类型',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
  `portrait` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `password` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `phone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `balance` int DEFAULT '0' COMMENT '用户余额',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `disabled` tinyint NOT NULL DEFAULT '0' COMMENT '0 active, 1 disabled',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_name` (`name`),
  KEY `idx_user_type` (`type`),
  KEY `idx_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`id`, `type`, `name`, `portrait`, `password`, `phone`, `balance`, `description`, `disabled`) VALUES ('1','admin','admin','/storage/avatar/default_avatar.jpg','sha256$2a77866c6b9054f4ff4a5705f6edbbbb09251f01c6fc81ab37f838ec29d3951b','13800000000',200,'platform administrator',0),('2','cus','customer','/storage/avatar/default_avatar.jpg','sha256$2a77866c6b9054f4ff4a5705f6edbbbb09251f01c6fc81ab37f838ec29d3951b','13800000001',537,'demo customer',0),('3','mer','merchant','/storage/avatar/default_avatar.jpg','sha256$2a77866c6b9054f4ff4a5705f6edbbbb09251f01c6fc81ab37f838ec29d3951b','13800000002',200,'demo merchant',0),('4','driver','driver','/storage/avatar/default_avatar.jpg','sha256$2a77866c6b9054f4ff4a5705f6edbbbb09251f01c6fc81ab37f838ec29d3951b','13800000003',200,'demo driver',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `wallet_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wallet_transaction` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流水id',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '钱包用户id',
  `user_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '钱包用户名称',
  `type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流水类型 RECHARGE/PAY/REFUND/ADJUST',
  `amount` int NOT NULL COMMENT '变动金额，收入为正，支出为负',
  `balance_before` int NOT NULL COMMENT '变动前余额',
  `balance_after` int NOT NULL COMMENT '变动后余额',
  `related_order_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联订单id',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `actor_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人id',
  `actor_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人名称',
  `actor_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人角色',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_wallet_transaction_user` (`user_id`),
  KEY `idx_wallet_transaction_type` (`type`),
  KEY `idx_wallet_transaction_order` (`related_order_id`),
  KEY `idx_wallet_transaction_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包余额流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `wallet_transaction` WRITE;
/*!40000 ALTER TABLE `wallet_transaction` DISABLE KEYS */;
INSERT INTO `wallet_transaction` (`id`, `user_id`, `user_name`, `type`, `amount`, `balance_before`, `balance_after`, `related_order_id`, `remark`, `actor_id`, `actor_name`, `actor_type`, `created_time`) VALUES ('02065607','2','customer','PAY',-32,591,559,'19448242','订单余额支付','2','customer','cus','2026-06-16 16:03:13'),('04012394','2','customer','PAY',-25,644,619,'25163700','订单余额支付','2','customer','cus','2026-06-16 16:01:42'),('06634164','2','customer','REFUND',32,519,551,'19448242','订单退款回滚','3','merchant','mer','2026-06-16 16:19:50'),('07389952','2','customer','PAY',-28,547,519,'64002977','订单余额支付','2','customer','cus','2026-06-16 16:18:47'),('11228847','2','customer','PAY',-28,619,591,'38343677','订单余额支付','2','customer','cus','2026-06-16 16:01:42'),('33235666','2','customer','PAY',-24,668,644,'14964582','订单余额支付','2','customer','cus','2026-06-16 16:01:42'),('53840416','2','customer','PAY',-10,547,537,'56362982','订单余额支付','2','customer','cus','2026-06-17 19:13:08'),('54843617','2','customer','PAY',-32,700,668,'29746852','订单余额支付','2','customer','cus','2026-06-16 16:01:41'),('56202291','2','customer','PAY',-12,559,547,'88219613','订单余额支付','2','customer','cus','2026-06-16 16:03:13'),('65228191','2','customer','RECHARGE',500,200,700,NULL,'AUTO_DEMO seed balance','1','admin','admin','2026-06-16 16:01:41'),('demo-wallet-pay-after-sale','2','customer','PAY',-32,591,559,'demo-order-after-sale','AUTO_DEMO wallet pay','2','customer','cus','2026-06-16 16:03:13'),('demo-wallet-pay-completed','2','customer','PAY',-28,619,591,'demo-order-completed-review','AUTO_DEMO wallet pay','2','customer','cus','2026-06-16 16:01:42'),('demo-wallet-pay-delivering','2','customer','PAY',-12,559,547,'demo-order-delivering','AUTO_DEMO wallet pay','2','customer','cus','2026-06-16 16:03:13'),('demo-wallet-pay-paid','2','customer','PAY',-32,700,668,'demo-order-paid','AUTO_DEMO wallet pay','2','customer','cus','2026-06-16 16:01:41'),('demo-wallet-pay-preparing','2','customer','PAY',-24,668,644,'demo-order-preparing','AUTO_DEMO wallet pay','2','customer','cus','2026-06-16 16:01:42'),('demo-wallet-pay-waiting-driver','2','customer','PAY',-25,644,619,'demo-order-waiting-driver','AUTO_DEMO wallet pay','2','customer','cus','2026-06-16 16:01:42'),('demo-wallet-recharge','2','customer','RECHARGE',500,200,700,NULL,'AUTO_DEMO seed balance','1','admin','admin','2026-06-16 16:01:40');
/*!40000 ALTER TABLE `wallet_transaction` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
