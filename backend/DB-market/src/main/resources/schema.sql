-- DB-market Database Initialization Script
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS market DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE market;

-- User table
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

-- Category table
CREATE TABLE IF NOT EXISTS category (
    name VARCHAR(64) PRIMARY KEY COMMENT '商品类别名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品类别表';

-- Product table
CREATE TABLE IF NOT EXISTS product (
    id VARCHAR(64) PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(256) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price INT NOT NULL COMMENT '商品价格',
    mer VARCHAR(64) NOT NULL COMMENT '商家id',
    cat_name VARCHAR(64) COMMENT '商品类别',
    number INT DEFAULT 0 COMMENT '商品库存',
    state INT DEFAULT 0 COMMENT '商品状态(-1 未通过审核 0审核中 1审核通过)',
    sales_refund INT DEFAULT 0 COMMENT '该商品退货量',
    rate_refund VARCHAR(32) COMMENT '该商品退货率 %',
    complain INT DEFAULT 0 COMMENT '该商品投诉量',
    complain_rate VARCHAR(32) COMMENT '该商品投诉率',
    INDEX idx_product_mer (mer),
    INDEX idx_product_cat_name (cat_name),
    INDEX idx_product_state (state),
    FOREIGN KEY (cat_name) REFERENCES category(name) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- Product Image table
CREATE TABLE IF NOT EXISTS prod_img (
    prod VARCHAR(64) NOT NULL COMMENT '商品id',
    image VARCHAR(512) NOT NULL COMMENT '商品图片',
    PRIMARY KEY (prod, image),
    INDEX idx_prod_img_prod (prod),
    FOREIGN KEY (prod) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- Address table
CREATE TABLE IF NOT EXISTS address (
    addr_id VARCHAR(64) PRIMARY KEY COMMENT '地址id',
    usr VARCHAR(64) NOT NULL COMMENT '用户id',
    location TEXT NOT NULL COMMENT '地址位置描述',
    INDEX idx_address_usr (usr),
    FOREIGN KEY (usr) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';

-- Restaurant / Store table
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

-- Cart table
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

-- Order Info table
CREATE TABLE IF NOT EXISTS order_info (
    id VARCHAR(64) NOT NULL COMMENT '订单id',
    cus VARCHAR(64) NOT NULL COMMENT '买家Id',
    mer VARCHAR(64) NOT NULL COMMENT '卖家Id',
    prod VARCHAR(64) NOT NULL COMMENT '商品Id',
    prod_num INT NOT NULL COMMENT '下单购买数量',
    time DATETIME NOT NULL COMMENT '下单时间',
    deli_addr VARCHAR(64) COMMENT '发货地址id',
    rec_addr VARCHAR(64) COMMENT '收货地址id',
    state INT NOT NULL DEFAULT -1 COMMENT '订单状态 -3已退货/退款 -2退货中 -1下单未支付 0已支付 1已发货 2已收货',
    account INT NOT NULL COMMENT '订单金额',
    driver_id VARCHAR(64) COMMENT 'driver user id',
    remark VARCHAR(500) COMMENT 'customer remark',
    expected_delivery_time DATETIME COMMENT 'expected delivery time',
    pay_time DATETIME COMMENT 'payment time',
    complain VARCHAR(32) DEFAULT '0' COMMENT '是否被投诉 0未被投诉，1被投诉',
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

-- Message table
CREATE TABLE IF NOT EXISTS message (
    time_slot DATETIME NOT NULL COMMENT '发送时间',
    sender VARCHAR(64) NOT NULL COMMENT '发送人id',
    receiver VARCHAR(64) NOT NULL COMMENT '接受人id',
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

-- Order Review table
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

-- Insert default categories
INSERT IGNORE INTO category (name) VALUES
    ('数码产品'),
    ('食品'),
    ('服装'),
    ('家居'),
    ('其他');

-- Demo accounts. Passwords are plaintext intentionally; the backend migrates them
-- to hashed values after successful login.
INSERT IGNORE INTO user (id, type, name, portrait, password, phone, balance, description, disabled) VALUES
    ('1', 'admin', 'admin', 'default_avatar', '123456', '13800000000', 200, 'platform administrator', 0),
    ('2', 'cus', 'customer', 'default_avatar', '123456', '13800000001', 200, 'demo customer', 0),
    ('3', 'mer', 'merchant', 'default_avatar', '123456', '13800000002', 200, 'demo merchant', 0),
    ('4', 'driver', 'driver', 'default_avatar', '123456', '13800000003', 200, 'demo driver', 0);

INSERT IGNORE INTO address (addr_id, usr, location) VALUES
    ('1', '2', 'Customer demo address'),
    ('2', '3', 'Merchant demo address'),
    ('3', '4', 'Driver service area');

INSERT IGNORE INTO restaurant (id, merchant_id, name, logo, cover, description, notice, status, business_hours, delivery_fee, min_order_amount, service_radius_km, delivery_eta_minutes, feature_tags, menu_categories, address_text, delivery_policy, promo_text) VALUES
    ('3', '3', 'merchant精选门店', 'default_avatar', 'default_avatar', '课堂展示版门店示例，支持门店资料、排序筛选和详情展示。', '欢迎光临，当前门店已切换为课堂展示版资料。', 1, '10:00-21:30', 4, 18, 5, 28, '品牌门店,课堂展示推荐,当日现做', '招牌套餐,热销主食,小吃饮品', 'Merchant demo address', '满18元起送，支持骑手课堂展示版配送。', '新客首单享门店展示优惠');

INSERT IGNORE INTO operation_permission (role_code, permission_key, permission_name, permission_type, scope_code, enabled, updated_time) VALUES
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
    ('admin', 'admin.action.afterSale.handle', '处理售后工单', 'action', 'admin', 1, NOW()),
    ('admin', 'admin.action.audit.view', '查看审计日志', 'action', 'admin', 1, NOW()),
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
    ('mer', 'merchant.action.audit.view', '查看操作日志', 'action', 'merchant', 1, NOW());
