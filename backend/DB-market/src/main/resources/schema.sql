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

-- Order Review table
CREATE TABLE IF NOT EXISTS order_review (
    order_id VARCHAR(64) PRIMARY KEY COMMENT '订单id',
    cus VARCHAR(64) NOT NULL COMMENT '评价顾客id',
    mer VARCHAR(64) NOT NULL COMMENT '被评价商家id',
    score INT NOT NULL COMMENT '评分 1-5',
    content TEXT NOT NULL COMMENT '评价内容',
    created_time DATETIME NOT NULL COMMENT '评价时间',
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
