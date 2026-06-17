-- DB-market database initialization script.
-- Always import this file with an UTF-8 client, for example:
-- mysql --default-character-set=utf8mb4 -uroot -p < schema.sql

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS market DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE market;

CREATE TABLE IF NOT EXISTS user (
    id VARCHAR(64) PRIMARY KEY COMMENT '用户唯一标识',
    type VARCHAR(32) NOT NULL COMMENT '用户类型',
    name VARCHAR(128) NOT NULL COMMENT '用户昵称',
    portrait VARCHAR(512) COMMENT '头像',
    password VARCHAR(256) NOT NULL COMMENT '密码',
    phone VARCHAR(32) COMMENT '手机号',
    balance INT DEFAULT 0 COMMENT '用户余额',
    description TEXT COMMENT '描述',
    disabled TINYINT NOT NULL DEFAULT 0 COMMENT '0 active, 1 disabled',
    UNIQUE KEY uk_user_name (name),
    INDEX idx_user_type (type),
    INDEX idx_user_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS category (
    name VARCHAR(64) PRIMARY KEY COMMENT '商品类别名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品类别表';

CREATE TABLE IF NOT EXISTS product (
    id VARCHAR(64) PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(256) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price INT NOT NULL COMMENT '商品价格',
    mer VARCHAR(64) NOT NULL COMMENT '商家id',
    cat_name VARCHAR(64) COMMENT '商品类别',
    number INT DEFAULT 0 COMMENT '商品库存',
    state INT DEFAULT 0 COMMENT '商品状态(-1 未通过审核 0 审核中 1 审核通过)',
    sales_refund INT DEFAULT 0 COMMENT '该商品退货量',
    rate_refund VARCHAR(32) COMMENT '该商品退货率 %',
    complain INT DEFAULT 0 COMMENT '该商品投诉量',
    complain_rate VARCHAR(32) COMMENT '该商品投诉率',
    INDEX idx_product_mer (mer),
    INDEX idx_product_cat_name (cat_name),
    INDEX idx_product_state (state),
    FOREIGN KEY (cat_name) REFERENCES category(name) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE IF NOT EXISTS prod_img (
    prod VARCHAR(64) NOT NULL COMMENT '商品id',
    image VARCHAR(512) NOT NULL COMMENT '商品图片',
    PRIMARY KEY (prod, image),
    INDEX idx_prod_img_prod (prod),
    FOREIGN KEY (prod) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

CREATE TABLE IF NOT EXISTS address (
    addr_id VARCHAR(64) PRIMARY KEY COMMENT '地址id',
    usr VARCHAR(64) NOT NULL COMMENT '用户id',
    location TEXT NOT NULL COMMENT '地址位置描述',
    INDEX idx_address_usr (usr),
    FOREIGN KEY (usr) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';

CREATE TABLE IF NOT EXISTS restaurant (
    id VARCHAR(64) PRIMARY KEY COMMENT '门店id',
    merchant_id VARCHAR(64) NOT NULL COMMENT '所属商家id',
    name VARCHAR(128) NOT NULL COMMENT '门店名称',
    logo VARCHAR(512) COMMENT '门店logo',
    cover VARCHAR(512) COMMENT '门店封面',
    description TEXT COMMENT '门店简介',
    notice TEXT COMMENT '门店公告',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '营业状态 1营业中 0休息中',
    business_hours VARCHAR(128) COMMENT '营业时间',
    delivery_fee INT DEFAULT 0 COMMENT '配送费',
    min_order_amount INT DEFAULT 0 COMMENT '起送价',
    service_radius_km DOUBLE DEFAULT 5 COMMENT '配送范围km',
    delivery_eta_minutes INT DEFAULT 30 COMMENT '预计送达分钟数',
    feature_tags VARCHAR(500) COMMENT '门店标签，逗号分隔',
    menu_categories VARCHAR(500) COMMENT '门店菜单分类，逗号分隔',
    address_text VARCHAR(500) COMMENT '门店地址',
    delivery_policy TEXT COMMENT '配送说明',
    promo_text VARCHAR(255) COMMENT '活动文案',
    UNIQUE KEY uk_restaurant_merchant (merchant_id),
    INDEX idx_restaurant_status (status),
    FOREIGN KEY (merchant_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店表';

CREATE TABLE IF NOT EXISTS cart (
    cus VARCHAR(64) NOT NULL COMMENT '用户id',
    prod VARCHAR(64) NOT NULL COMMENT '商品id',
    number INT NOT NULL DEFAULT 1 COMMENT '商品数量',
    PRIMARY KEY (cus, prod),
    INDEX idx_cart_cus (cus),
    INDEX idx_cart_prod (prod),
    FOREIGN KEY (cus) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (prod) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

CREATE TABLE IF NOT EXISTS order_info (
    id VARCHAR(64) NOT NULL COMMENT '订单id',
    cus VARCHAR(64) NOT NULL COMMENT '买家Id',
    mer VARCHAR(64) NOT NULL COMMENT '卖家Id',
    prod VARCHAR(64) NOT NULL COMMENT '商品Id',
    prod_num INT NOT NULL COMMENT '下单购买数量',
    time DATETIME NOT NULL COMMENT '下单时间',
    deli_addr VARCHAR(64) COMMENT '发货地址id',
    rec_addr VARCHAR(64) COMMENT '收货地址id',
    state INT NOT NULL DEFAULT -1 COMMENT '订单状态(-3已退款 -2退货中 -1下单未支付 0已支付 1已发货 2已收货 3待接单 4备餐中)',
    account INT NOT NULL COMMENT '订单金额',
    driver_id VARCHAR(64) COMMENT 'driver user id',
    remark VARCHAR(500) COMMENT 'customer remark',
    expected_delivery_time DATETIME COMMENT 'expected delivery time',
    pay_time DATETIME COMMENT 'payment time',
    complain VARCHAR(32) DEFAULT '0' COMMENT '是否被投诉 0未被投诉 1被投诉',
    complain_reason TEXT COMMENT '投诉理由',
    refund_reason TEXT COMMENT '退款理由，请求退款',
    PRIMARY KEY (id, prod),
    INDEX idx_order_info_cus (cus),
    INDEX idx_order_info_mer (mer),
    INDEX idx_order_info_prod (prod),
    INDEX idx_order_info_state (state),
    INDEX idx_order_info_driver_state (driver_id, state),
    FOREIGN KEY (cus) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (mer) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (prod) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS message (
    time_slot DATETIME NOT NULL COMMENT '发送时间',
    sender VARCHAR(64) NOT NULL COMMENT '发送人id',
    receiver VARCHAR(64) NOT NULL COMMENT '接收人id',
    content TEXT NOT NULL COMMENT '信息内容',
    PRIMARY KEY (time_slot, sender, receiver),
    INDEX idx_message_sender (sender),
    INDEX idx_message_receiver (receiver),
    FOREIGN KEY (sender) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

CREATE TABLE IF NOT EXISTS after_sale_ticket (
    id VARCHAR(64) PRIMARY KEY COMMENT '工单id',
    order_id VARCHAR(64) NOT NULL COMMENT '关联订单id',
    customer_id VARCHAR(64) NOT NULL COMMENT '顾客id',
    merchant_id VARCHAR(64) NOT NULL COMMENT '商家id',
    type VARCHAR(32) NOT NULL COMMENT '工单类型',
    content TEXT NOT NULL COMMENT '问题描述',
    status VARCHAR(32) NOT NULL COMMENT '工单状态',
    handler_id VARCHAR(64) COMMENT '处理人id',
    handler_note TEXT COMMENT '处理备注',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    updated_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_after_sale_customer (customer_id),
    INDEX idx_after_sale_merchant (merchant_id),
    INDEX idx_after_sale_status (status),
    INDEX idx_after_sale_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='售后工单表';

CREATE TABLE IF NOT EXISTS operation_permission (
    role_code VARCHAR(32) NOT NULL COMMENT '角色编码',
    permission_key VARCHAR(128) NOT NULL COMMENT '权限键',
    permission_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    permission_type VARCHAR(32) NOT NULL COMMENT '权限类型 menu/action',
    scope_code VARCHAR(32) NOT NULL COMMENT 'admin/merchant',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    updated_time DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (role_code, permission_key),
    INDEX idx_operation_permission_scope (scope_code, permission_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂展示版权限配置表';

CREATE TABLE IF NOT EXISTS operation_audit_log (
    id VARCHAR(64) PRIMARY KEY COMMENT '日志id',
    actor_id VARCHAR(64) COMMENT '操作人id',
    actor_name VARCHAR(128) COMMENT '操作人名称',
    actor_type VARCHAR(32) COMMENT '操作人角色',
    action_type VARCHAR(64) NOT NULL COMMENT '动作类型',
    target_type VARCHAR(64) COMMENT '目标类型',
    target_id VARCHAR(64) COMMENT '目标id',
    target_name VARCHAR(128) COMMENT '目标名称',
    detail TEXT COMMENT '动作描述',
    result VARCHAR(32) NOT NULL COMMENT '处理结果',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_operation_audit_actor (actor_id),
    INDEX idx_operation_audit_type (action_type),
    INDEX idx_operation_audit_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂展示版操作审计日志表';

CREATE TABLE IF NOT EXISTS wallet_transaction (
    id VARCHAR(64) PRIMARY KEY COMMENT '流水id',
    user_id VARCHAR(64) NOT NULL COMMENT '钱包用户id',
    user_name VARCHAR(128) COMMENT '钱包用户名称',
    type VARCHAR(32) NOT NULL COMMENT '流水类型 RECHARGE/PAY/REFUND/ADJUST',
    amount INT NOT NULL COMMENT '变动金额，收入为正，支出为负',
    balance_before INT NOT NULL COMMENT '变动前余额',
    balance_after INT NOT NULL COMMENT '变动后余额',
    related_order_id VARCHAR(64) COMMENT '关联订单id',
    remark VARCHAR(500) COMMENT '备注',
    actor_id VARCHAR(64) COMMENT '操作人id',
    actor_name VARCHAR(128) COMMENT '操作人名称',
    actor_type VARCHAR(32) COMMENT '操作人角色',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_wallet_transaction_user (user_id),
    INDEX idx_wallet_transaction_type (type),
    INDEX idx_wallet_transaction_order (related_order_id),
    INDEX idx_wallet_transaction_time (created_time),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包余额流水表';

CREATE TABLE IF NOT EXISTS order_review (
    order_id VARCHAR(64) PRIMARY KEY COMMENT '订单id',
    cus VARCHAR(64) NOT NULL COMMENT '评价顾客id',
    mer VARCHAR(64) NOT NULL COMMENT '被评价商家id',
    score INT NOT NULL COMMENT '评分 1-5',
    content TEXT NOT NULL COMMENT '评价内容',
    created_time DATETIME NOT NULL COMMENT '评价时间',
    reply_content TEXT COMMENT '商家回复内容',
    reply_time DATETIME COMMENT '商家回复时间',
    INDEX idx_order_review_mer (mer),
    INDEX idx_order_review_cus (cus),
    FOREIGN KEY (cus) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (mer) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价表';

INSERT IGNORE INTO category (name) VALUES
    ('数码产品'),
    ('食品'),
    ('饮品'),
    ('生鲜蔬菜'),
    ('服装'),
    ('家居'),
    ('其他');

UPDATE product SET cat_name = '食品' WHERE HEX(cat_name) = 'E6A48BE786B7E690A7';
UPDATE product SET cat_name = '饮品' WHERE HEX(cat_name) = 'E6A5973FE690A7';
UPDATE product SET cat_name = '生鲜蔬菜' WHERE HEX(cat_name) = 'E990A2E786BCE79F9EE992843FE5BD8D';
UPDATE product SET cat_name = '数码产品' WHERE HEX(cat_name) = 'E98F81E689AEE7889CE6B59CD183E690A7';
UPDATE product SET cat_name = '服装' WHERE HEX(cat_name) = 'E98F88E5B6883F';
UPDATE product SET cat_name = '家居' WHERE HEX(cat_name) = 'E780B9E8B7BAE79CB3';
UPDATE product SET cat_name = '其他' WHERE HEX(cat_name) = 'E98D8FE69CB5E7B2AC';

INSERT INTO user (id, type, name, portrait, password, phone, balance, description, disabled) VALUES
    ('1', 'admin', 'admin', '/storage/avatar/default_avatar.jpg', '123456', '13800000000', 200, 'platform administrator', 0),
    ('2', 'cus', 'customer', '/storage/avatar/default_avatar.jpg', '123456', '13800000001', 200, 'demo customer', 0),
    ('3', 'mer', 'merchant', '/storage/avatar/default_avatar.jpg', '123456', '13800000002', 200, 'demo merchant', 0),
    ('4', 'driver', 'driver', '/storage/avatar/default_avatar.jpg', '123456', '13800000003', 200, 'demo driver', 0)
ON DUPLICATE KEY UPDATE
    portrait = VALUES(portrait),
    phone = VALUES(phone),
    description = VALUES(description),
    disabled = VALUES(disabled);

UPDATE user
SET portrait = '/storage/avatar/default_avatar.jpg'
WHERE id IN ('1', '2', '3', '4') AND (portrait = 'default_avatar' OR portrait LIKE '/img/%');

INSERT IGNORE INTO address (addr_id, usr, location) VALUES
    ('1', '2', 'Customer demo address'),
    ('2', '3', 'Merchant demo address'),
    ('3', '4', 'Driver service area');

INSERT INTO restaurant (id, merchant_id, name, logo, cover, description, notice, status, business_hours, delivery_fee, min_order_amount, service_radius_km, delivery_eta_minutes, feature_tags, menu_categories, address_text, delivery_policy, promo_text) VALUES
    ('3', '3', 'merchant精选门店', '/storage/restaurant/merchant-logo.jpg', '/storage/restaurant/merchant-cover.jpg', '课堂展示版门店示例，支持门店资料、排序筛选和详情展示。', '欢迎光临，当前门店已切换为课堂展示版资料。', 1, '10:00-21:30', 4, 18, 5, 28, '品牌门店,课堂展示推荐,当日现做', '招牌套餐,热销主食,小吃饮品,新鲜蔬菜,数码优选', 'Merchant demo address', '满18元起送，支持骑手课堂展示版配送。', '新客首单享门店展示优惠')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    logo = VALUES(logo),
    cover = VALUES(cover),
    description = VALUES(description),
    notice = VALUES(notice),
    status = VALUES(status),
    business_hours = VALUES(business_hours),
    delivery_fee = VALUES(delivery_fee),
    min_order_amount = VALUES(min_order_amount),
    service_radius_km = VALUES(service_radius_km),
    delivery_eta_minutes = VALUES(delivery_eta_minutes),
    feature_tags = VALUES(feature_tags),
    menu_categories = VALUES(menu_categories),
    address_text = VALUES(address_text),
    delivery_policy = VALUES(delivery_policy),
    promo_text = VALUES(promo_text);

INSERT INTO product (id, name, description, price, mer, cat_name, number, state, sales_refund, rate_refund, complain, complain_rate) VALUES
    ('demo-prod-chicken-rice', '自动化演示-蜜汁鸡腿饭', '课堂展示用热销主食，覆盖商品审核、下单和评价链路。', 28, '3', '食品', 79, 1, 0, '0.0', 0, '0.0'),
    ('demo-prod-tomato-beef', '自动化演示-番茄牛腩饭', '课堂展示用套餐商品，适合演示订单状态流转。', 32, '3', '食品', 68, 1, 0, '0.0', 1, '0.0'),
    ('demo-prod-lemon-tea', '自动化演示-冰镇柠檬茶', '课堂展示用饮品商品，补充低客单价数据。', 12, '3', '饮品', 117, 1, 0, '0.0', 0, '0.0'),
    ('demo-prod-beef-burger', '自动化演示-香煎牛肉堡', '课堂展示用快餐商品，覆盖库存和支付演示。', 25, '3', '食品', 59, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-braised-beef', '红烧牛肉饭', '迁移自前端素材的主食商品，适合展示热销餐品。', 30, '3', '食品', 88, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-pork-set', '香煎猪排套餐', '迁移自前端素材的套餐商品，展示多图菜品详情。', 29, '3', '食品', 82, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-tomato-meal', '番茄浓汤套餐', '番茄风味套餐，补充餐品类展示数据。', 26, '3', '食品', 76, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-coca', '可口可乐', '饮品素材商品，适合展示低价饮品。', 6, '3', '饮品', 180, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-pepsi', '百事可乐', '饮品素材商品，丰富饮品分类。', 6, '3', '饮品', 180, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-sprite', '雪碧', '清爽饮品，用于丰富购物车和订单演示。', 6, '3', '饮品', 160, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-cabbage', '新鲜卷心菜', '生鲜蔬菜素材商品，展示非餐品分类。', 8, '3', '生鲜蔬菜', 120, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-celery', '清香芹菜', '生鲜蔬菜素材商品，适合筛选展示。', 9, '3', '生鲜蔬菜', 120, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-chives', '新鲜韭菜', '生鲜蔬菜素材商品，补充门店菜单。', 7, '3', '生鲜蔬菜', 120, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-onion', '紫皮洋葱', '生鲜蔬菜素材商品，丰富展示数据。', 7, '3', '生鲜蔬菜', 120, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-pepper', '彩椒组合', '生鲜蔬菜素材商品，支持多图轮播展示。', 10, '3', '生鲜蔬菜', 110, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-iphone15', 'iPhone 15 展示机', '数码产品素材商品，用于丰富平台商品类型。', 5999, '3', '数码产品', 12, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-mate60', 'Mate 60 Pro 展示机', '数码产品素材商品，展示高客单价商品。', 6999, '3', '数码产品', 10, 1, 0, '0.0', 0, '0.0'),
    ('asset-prod-laptop', '轻薄笔记本电脑', '数码产品素材商品，展示多图商品详情。', 5299, '3', '数码产品', 8, 1, 0, '0.0', 0, '0.0')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    price = VALUES(price),
    mer = VALUES(mer),
    cat_name = VALUES(cat_name),
    number = VALUES(number),
    state = VALUES(state),
    sales_refund = VALUES(sales_refund),
    rate_refund = VALUES(rate_refund),
    complain = VALUES(complain),
    complain_rate = VALUES(complain_rate);

INSERT IGNORE INTO prod_img (prod, image) VALUES
    ('demo-prod-chicken-rice', '/storage/product/food/beef-1.png'),
    ('demo-prod-chicken-rice', '/storage/product/food/beef-2.png'),
    ('demo-prod-tomato-beef', '/storage/product/food/tomato-meal-1.png'),
    ('demo-prod-tomato-beef', '/storage/product/food/tomato-meal-2.png'),
    ('demo-prod-lemon-tea', '/storage/product/drink/coca-1.jpg'),
    ('demo-prod-lemon-tea', '/storage/product/drink/pepsi-1.jpg'),
    ('demo-prod-beef-burger', '/storage/product/food/pork-1.png'),
    ('demo-prod-beef-burger', '/storage/product/food/pork-2.png'),
    ('asset-prod-braised-beef', '/storage/product/food/beef-1.png'),
    ('asset-prod-braised-beef', '/storage/product/food/beef-2.png'),
    ('asset-prod-pork-set', '/storage/product/food/pork-1.png'),
    ('asset-prod-pork-set', '/storage/product/food/pork-2.png'),
    ('asset-prod-pork-set', '/storage/product/food/pork-3.png'),
    ('asset-prod-tomato-meal', '/storage/product/food/tomato-meal-1.png'),
    ('asset-prod-tomato-meal', '/storage/product/food/tomato-meal-2.png'),
    ('asset-prod-tomato-meal', '/storage/product/food/tomato-meal-3.png'),
    ('asset-prod-coca', '/storage/product/drink/coca-1.jpg'),
    ('asset-prod-coca', '/storage/product/drink/coca-2.jpg'),
    ('asset-prod-coca', '/storage/product/drink/coca-3.jpg'),
    ('asset-prod-pepsi', '/storage/product/drink/pepsi-1.jpg'),
    ('asset-prod-pepsi', '/storage/product/drink/pepsi-2.jpg'),
    ('asset-prod-pepsi', '/storage/product/drink/pepsi-3.jpg'),
    ('asset-prod-sprite', '/storage/product/drink/sprite-1.png'),
    ('asset-prod-sprite', '/storage/product/drink/sprite-2.png'),
    ('asset-prod-cabbage', '/storage/product/vegetable/cabbage-1.png'),
    ('asset-prod-cabbage', '/storage/product/vegetable/cabbage-2.png'),
    ('asset-prod-cabbage', '/storage/product/vegetable/cabbage-3.png'),
    ('asset-prod-celery', '/storage/product/vegetable/celery-1.png'),
    ('asset-prod-celery', '/storage/product/vegetable/celery-2.png'),
    ('asset-prod-celery', '/storage/product/vegetable/celery-3.png'),
    ('asset-prod-chives', '/storage/product/vegetable/chives-1.png'),
    ('asset-prod-chives', '/storage/product/vegetable/chives-2.png'),
    ('asset-prod-onion', '/storage/product/vegetable/onion-1.png'),
    ('asset-prod-onion', '/storage/product/vegetable/onion-2.png'),
    ('asset-prod-pepper', '/storage/product/vegetable/pepper-1.png'),
    ('asset-prod-pepper', '/storage/product/vegetable/pepper-2.png'),
    ('asset-prod-pepper', '/storage/product/vegetable/pepper-3.png'),
    ('asset-prod-iphone15', '/storage/product/digital/iphone15-1.png'),
    ('asset-prod-iphone15', '/storage/product/digital/iphone15-2.png'),
    ('asset-prod-mate60', '/storage/product/digital/mate60-pro-1.png'),
    ('asset-prod-mate60', '/storage/product/digital/mate60-pro-2.png'),
    ('asset-prod-laptop', '/storage/product/digital/laptop-1.png'),
    ('asset-prod-laptop', '/storage/product/digital/laptop-2.png'),
    ('asset-prod-laptop', '/storage/product/digital/laptop-3.png');

INSERT INTO order_info (id, cus, mer, prod, prod_num, time, deli_addr, rec_addr, state, account, driver_id, remark, expected_delivery_time, pay_time, complain, complain_reason, refund_reason) VALUES
    ('demo-order-unpaid', '2', '3', 'demo-prod-chicken-rice', 1, '2026-06-16 16:01:41', '2', '1', -1, 28, NULL, 'AUTO_DEMO_UNPAID', NULL, NULL, '0', NULL, NULL),
    ('demo-order-paid', '2', '3', 'demo-prod-tomato-beef', 1, '2026-06-16 16:01:41', '2', '1', 0, 32, NULL, 'AUTO_DEMO_PAID', NULL, '2026-06-16 16:01:41', '0', NULL, NULL),
    ('demo-order-preparing', '2', '3', 'demo-prod-lemon-tea', 2, '2026-06-16 16:01:42', '2', '1', 4, 24, NULL, 'AUTO_DEMO_PREPARING', NULL, '2026-06-16 16:01:42', '0', NULL, NULL),
    ('demo-order-waiting-driver', '2', '3', 'demo-prod-beef-burger', 1, '2026-06-16 16:01:42', '2', '1', 3, 25, NULL, 'AUTO_DEMO_WAITING_DRIVER', NULL, '2026-06-16 16:01:42', '0', NULL, NULL),
    ('demo-order-completed-review', '2', '3', 'demo-prod-chicken-rice', 1, '2026-06-16 16:01:42', '2', '1', 2, 28, '4', 'AUTO_DEMO_COMPLETED_REVIEW', NULL, '2026-06-16 16:01:42', '0', NULL, NULL),
    ('demo-order-after-sale', '2', '3', 'demo-prod-tomato-beef', 1, '2026-06-16 16:03:13', '2', '1', 2, 32, '4', 'AUTO_DEMO_AFTER_SALE', NULL, '2026-06-16 16:03:13', '1', '自动化演示工单：包装轻微破损，申请客服跟进。', NULL),
    ('demo-order-delivering', '2', '3', 'demo-prod-lemon-tea', 1, '2026-06-16 16:03:13', '2', '1', 1, 12, '4', 'AUTO_DEMO_DELIVERING', NULL, '2026-06-16 16:03:13', '0', NULL, NULL)
ON DUPLICATE KEY UPDATE
    state = VALUES(state),
    account = VALUES(account),
    driver_id = VALUES(driver_id),
    remark = VALUES(remark),
    pay_time = VALUES(pay_time),
    complain = VALUES(complain),
    complain_reason = VALUES(complain_reason),
    refund_reason = VALUES(refund_reason);

INSERT INTO order_review (order_id, cus, mer, score, content, created_time, reply_content, reply_time) VALUES
    ('demo-order-completed-review', '2', '3', 5, '配送及时，餐品状态很好，适合作为课堂展示评价。', '2026-06-16 16:03:13', '感谢评价，我们会继续保持出餐速度和服务质量。', '2026-06-16 16:03:13')
ON DUPLICATE KEY UPDATE
    score = VALUES(score),
    content = VALUES(content),
    reply_content = VALUES(reply_content),
    reply_time = VALUES(reply_time);

INSERT INTO after_sale_ticket (id, order_id, customer_id, merchant_id, type, content, status, handler_id, handler_note, created_time, updated_time) VALUES
    ('demo-ticket-after-sale', 'demo-order-after-sale', '2', '3', '投诉反馈', '自动化演示工单：包装轻微破损，申请客服跟进。', '待处理', NULL, NULL, '2026-06-16 16:03:13', '2026-06-16 16:03:13')
ON DUPLICATE KEY UPDATE
    type = VALUES(type),
    content = VALUES(content),
    status = VALUES(status),
    handler_id = VALUES(handler_id),
    handler_note = VALUES(handler_note),
    updated_time = VALUES(updated_time);

INSERT INTO wallet_transaction (id, user_id, user_name, type, amount, balance_before, balance_after, related_order_id, remark, actor_id, actor_name, actor_type, created_time) VALUES
    ('demo-wallet-recharge', '2', 'customer', 'RECHARGE', 500, 200, 700, NULL, 'AUTO_DEMO seed balance', '1', 'admin', 'admin', '2026-06-16 16:01:40'),
    ('demo-wallet-pay-paid', '2', 'customer', 'PAY', -32, 700, 668, 'demo-order-paid', 'AUTO_DEMO wallet pay', '2', 'customer', 'cus', '2026-06-16 16:01:41'),
    ('demo-wallet-pay-preparing', '2', 'customer', 'PAY', -24, 668, 644, 'demo-order-preparing', 'AUTO_DEMO wallet pay', '2', 'customer', 'cus', '2026-06-16 16:01:42'),
    ('demo-wallet-pay-waiting-driver', '2', 'customer', 'PAY', -25, 644, 619, 'demo-order-waiting-driver', 'AUTO_DEMO wallet pay', '2', 'customer', 'cus', '2026-06-16 16:01:42'),
    ('demo-wallet-pay-completed', '2', 'customer', 'PAY', -28, 619, 591, 'demo-order-completed-review', 'AUTO_DEMO wallet pay', '2', 'customer', 'cus', '2026-06-16 16:01:42'),
    ('demo-wallet-pay-after-sale', '2', 'customer', 'PAY', -32, 591, 559, 'demo-order-after-sale', 'AUTO_DEMO wallet pay', '2', 'customer', 'cus', '2026-06-16 16:03:13'),
    ('demo-wallet-pay-delivering', '2', 'customer', 'PAY', -12, 559, 547, 'demo-order-delivering', 'AUTO_DEMO wallet pay', '2', 'customer', 'cus', '2026-06-16 16:03:13')
ON DUPLICATE KEY UPDATE
    user_name = VALUES(user_name),
    type = VALUES(type),
    amount = VALUES(amount),
    balance_before = VALUES(balance_before),
    balance_after = VALUES(balance_after),
    related_order_id = VALUES(related_order_id),
    remark = VALUES(remark),
    actor_id = VALUES(actor_id),
    actor_name = VALUES(actor_name),
    actor_type = VALUES(actor_type);

UPDATE user SET balance = 547 WHERE id = '2';

INSERT INTO operation_permission (role_code, permission_key, permission_name, permission_type, scope_code, enabled, updated_time) VALUES
    ('admin', 'admin.menu.goods', '商品治理', 'menu', 'admin', 1, NOW()),
    ('admin', 'admin.menu.order', '订单总览', 'menu', 'admin', 1, NOW()),
    ('admin', 'admin.menu.category', '分类管理', 'menu', 'admin', 1, NOW()),
    ('admin', 'admin.menu.afterSale', '售后工单', 'menu', 'admin', 1, NOW()),
    ('admin', 'admin.menu.user', '用户管理', 'menu', 'admin', 1, NOW()),
    ('admin', 'admin.menu.ops', '治理与经营', 'menu', 'admin', 1, NOW()),
    ('admin', 'admin.action.dashboard.view', '查看平台看板', 'action', 'admin', 1, NOW()),
    ('admin', 'admin.action.permission.manage', '维护权限配置', 'action', 'admin', 1, NOW()),
    ('admin', 'admin.action.user.view', '查看用户列表', 'action', 'admin', 1, NOW()),
    ('admin', 'admin.action.user.disable', '启停用户账号', 'action', 'admin', 1, NOW()),
    ('admin', 'admin.action.product.audit', '审核商品', 'action', 'admin', 1, NOW()),
    ('admin', 'admin.action.category.manage', '维护商品分类', 'action', 'admin', 1, NOW()),
    ('admin', 'admin.action.afterSale.handle', '处理售后工单', 'action', 'admin', 1, NOW()),
    ('admin', 'admin.action.audit.view', '查看审计日志', 'action', 'admin', 1, NOW()),
    ('admin', 'admin.action.wallet.view', '查看钱包流水', 'action', 'admin', 1, NOW()),
    ('mer', 'merchant.menu.goods', '商品管理', 'menu', 'merchant', 1, NOW()),
    ('mer', 'merchant.menu.order', '订单处理', 'menu', 'merchant', 1, NOW()),
    ('mer', 'merchant.menu.afterSale', '售后处理', 'menu', 'merchant', 1, NOW()),
    ('mer', 'merchant.menu.store', '门店资料', 'menu', 'merchant', 1, NOW()),
    ('mer', 'merchant.menu.info', '账号信息', 'menu', 'merchant', 1, NOW()),
    ('mer', 'merchant.menu.ops', '经营分析', 'menu', 'merchant', 1, NOW()),
    ('mer', 'merchant.action.dashboard.view', '查看经营看板', 'action', 'merchant', 1, NOW()),
    ('mer', 'merchant.action.report.export', '导出经营报表', 'action', 'merchant', 1, NOW()),
    ('mer', 'merchant.action.afterSale.handle', '处理售后工单', 'action', 'merchant', 1, NOW()),
    ('mer', 'merchant.action.store.manage', '维护门店资料', 'action', 'merchant', 1, NOW()),
    ('mer', 'merchant.action.audit.view', '查看操作日志', 'action', 'merchant', 1, NOW())
ON DUPLICATE KEY UPDATE
    permission_name = VALUES(permission_name),
    permission_type = VALUES(permission_type),
    scope_code = VALUES(scope_code),
    enabled = VALUES(enabled),
    updated_time = VALUES(updated_time);

DELETE c
FROM category c
LEFT JOIN product p ON p.cat_name = c.name
WHERE p.cat_name IS NULL
  AND HEX(c.name) IN (
      'E6A48BE786B7E690A7',
      'E6A5973FE690A7',
      'E780B9E8B7BAE79CB3',
      'E98D8FE69CB5E7B2AC',
      'E98F81E689AEE7889CE6B59CD183E690A7',
      'E98F88E5B6883F',
      'E990A2E786BCE79F9EE992843FE5BD8D'
  );
