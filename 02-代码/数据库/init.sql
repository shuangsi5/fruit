-- ============================================================
-- 水果交易智能管理系统 - 数据库初始化脚本
-- 数据库: fruit_trade_db
-- 字符集: utf8mb4 / utf8mb4_unicode_ci
-- 引擎: InnoDB
-- 密码说明: 示例用户密码统一为 bcrypt 加密的 "123456"
--   $2a$10$7JB720yubVSZvUI0rEqK/.VqGOVTHYKIiBZ0XQ4KQp0eB6QvYcDcW (BCrypt, 10轮)
-- ============================================================

DROP DATABASE IF EXISTS fruit_trade_db;
CREATE DATABASE fruit_trade_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE fruit_trade_db;

-- ------------------------------------------------------------
-- 1. 系统用户表
-- ------------------------------------------------------------
CREATE TABLE sys_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(64) NOT NULL COMMENT '登录账号',
  password VARCHAR(255) NOT NULL COMMENT 'BCrypt密码',
  real_name VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
  role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色码 USER/MERCHANT/ADMIN',
  merchant_id BIGINT UNSIGNED DEFAULT NULL COMMENT '商家ID',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_phone (phone),
  KEY idx_role (role),
  KEY idx_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ------------------------------------------------------------
-- 2. 商家表
-- ------------------------------------------------------------
CREATE TABLE merchant (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '账号ID',
  merchant_name VARCHAR(128) NOT NULL COMMENT '商家名称',
  license_no VARCHAR(64) DEFAULT NULL COMMENT '营业执照号',
  license_image VARCHAR(255) DEFAULT NULL COMMENT '执照图片',
  contact_person VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  address VARCHAR(255) DEFAULT NULL COMMENT '经营地址',
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审1通过2驳回',
  audit_remark VARCHAR(255) DEFAULT NULL COMMENT '审核意见',
  audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1营业0停业',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user (user_id),
  KEY idx_audit (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家表';

-- ------------------------------------------------------------
-- 3. 商品分类表
-- ------------------------------------------------------------
CREATE TABLE category (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  name VARCHAR(64) NOT NULL COMMENT '分类名称',
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID,0为一级',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
  icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用0停用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ------------------------------------------------------------
-- 4. 商品表
-- ------------------------------------------------------------
CREATE TABLE product (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  merchant_id BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  category_id BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  name VARCHAR(128) NOT NULL COMMENT '商品名称',
  subtitle VARCHAR(255) DEFAULT NULL COMMENT '副标题',
  description TEXT DEFAULT NULL COMMENT '详情',
  price DECIMAL(10,2) NOT NULL COMMENT '售价',
  original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  unit VARCHAR(20) NOT NULL DEFAULT '斤' COMMENT '单位',
  spec VARCHAR(64) DEFAULT NULL COMMENT '规格',
  origin VARCHAR(128) DEFAULT NULL COMMENT '产地',
  shelf_life_days INT NOT NULL DEFAULT 7 COMMENT '保质期天数',
  production_date DATE DEFAULT NULL COMMENT '批次生产日期',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0下架1上架',
  sales_count INT NOT NULL DEFAULT 0 COMMENT '累计销量',
  view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_merchant (merchant_id),
  KEY idx_category (category_id),
  KEY idx_status (status),
  KEY idx_sales (sales_count),
  KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ------------------------------------------------------------
-- 5. 商品图片表
-- ------------------------------------------------------------
CREATE TABLE product_image (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  url VARCHAR(255) NOT NULL COMMENT '图片地址',
  sort INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
  is_main TINYINT NOT NULL DEFAULT 0 COMMENT '是否主图',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- ------------------------------------------------------------
-- 6. 库存表
-- ------------------------------------------------------------
CREATE TABLE inventory (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  merchant_id BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  stock INT NOT NULL DEFAULT 0 COMMENT '可用库存',
  locked_stock INT NOT NULL DEFAULT 0 COMMENT '锁定库存',
  safety_stock INT NOT NULL DEFAULT 10 COMMENT '安全库存',
  warning_level TINYINT NOT NULL DEFAULT 0 COMMENT '预警等级0-3',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_product (product_id),
  KEY idx_merchant (merchant_id),
  KEY idx_warning (warning_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- ------------------------------------------------------------
-- 7. 购物车表
-- ------------------------------------------------------------
CREATE TABLE cart (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
  selected TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ------------------------------------------------------------
-- 8. 收货地址表
-- ------------------------------------------------------------
CREATE TABLE address (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  receiver VARCHAR(64) NOT NULL COMMENT '收货人',
  phone VARCHAR(20) NOT NULL COMMENT '联系电话',
  province VARCHAR(64) DEFAULT NULL COMMENT '省',
  city VARCHAR(64) DEFAULT NULL COMMENT '市',
  district VARCHAR(64) DEFAULT NULL COMMENT '区/县',
  detail VARCHAR(255) NOT NULL COMMENT '详细地址',
  is_default TINYINT NOT NULL DEFAULT 0 COMMENT '默认地址',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ------------------------------------------------------------
-- 9. 订单主表
-- ------------------------------------------------------------
CREATE TABLE orders (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  merchant_id BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  address_id BIGINT UNSIGNED NOT NULL COMMENT '地址ID',
  receiver VARCHAR(64) NOT NULL COMMENT '收件人',
  receiver_phone VARCHAR(20) NOT NULL COMMENT '收件电话',
  receiver_address VARCHAR(255) NOT NULL COMMENT '收件地址',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '商品总额',
  freight DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费',
  pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  pay_method TINYINT DEFAULT NULL COMMENT '支付方式',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态0待支付1待发货2运输中3完成4取消5售后',
  pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
  ship_time DATETIME DEFAULT NULL COMMENT '发货时间',
  finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
  close_time DATETIME DEFAULT NULL COMMENT '关闭时间',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_user (user_id),
  KEY idx_merchant (merchant_id),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- ------------------------------------------------------------
-- 10. 订单明细表
-- ------------------------------------------------------------
CREATE TABLE order_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  product_name VARCHAR(128) NOT NULL COMMENT '商品名称快照',
  product_image VARCHAR(255) DEFAULT NULL COMMENT '商品主图快照',
  price DECIMAL(10,2) NOT NULL COMMENT '下单单价快照',
  quantity INT NOT NULL COMMENT '购买数量',
  subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_order (order_id),
  KEY idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ------------------------------------------------------------
-- 11. 支付记录表
-- ------------------------------------------------------------
CREATE TABLE payment_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  payment_no VARCHAR(64) NOT NULL COMMENT '支付流水号',
  order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  pay_method TINYINT NOT NULL COMMENT '支付方式',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付1成功2失败3已退款',
  trade_no VARCHAR(64) DEFAULT NULL COMMENT '模拟交易流水号',
  paid_time DATETIME DEFAULT NULL COMMENT '支付完成时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_no (payment_no),
  KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ------------------------------------------------------------
-- 12. 物流表
-- ------------------------------------------------------------
CREATE TABLE logistics (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '物流ID',
  order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  logistics_no VARCHAR(64) NOT NULL COMMENT '运单号',
  carrier VARCHAR(64) DEFAULT NULL COMMENT '承运商',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待发货1已揽收2运输中3派送中4已签收',
  traces TEXT DEFAULT NULL COMMENT '物流轨迹JSON数组',
  ship_time DATETIME DEFAULT NULL COMMENT '发货时间',
  deliver_time DATETIME DEFAULT NULL COMMENT '签收时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order (order_id),
  KEY idx_logistics_no (logistics_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流表';

-- ------------------------------------------------------------
-- 13. 售后表
-- ------------------------------------------------------------
CREATE TABLE after_sale (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '售后ID',
  after_sale_no VARCHAR(64) NOT NULL COMMENT '售后单号',
  order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  order_item_id BIGINT UNSIGNED DEFAULT NULL COMMENT '明细ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '申请用户ID',
  merchant_id BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  type TINYINT NOT NULL COMMENT '1仅退款2退货退款3换货',
  reason VARCHAR(255) NOT NULL COMMENT '售后原因',
  description TEXT DEFAULT NULL COMMENT '问题描述',
  images TEXT DEFAULT NULL COMMENT '凭证图片JSON',
  amount DECIMAL(10,2) NOT NULL COMMENT '申请金额',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审1通过2处理中3完成4驳回',
  handle_remark VARCHAR(255) DEFAULT NULL COMMENT '处理意见',
  handler_id BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID',
  handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_after_sale_no (after_sale_no),
  KEY idx_order (order_id),
  KEY idx_user (user_id),
  KEY idx_merchant (merchant_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='售后表';

-- ------------------------------------------------------------
-- 14. 投诉表
-- ------------------------------------------------------------
CREATE TABLE complaint (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '投诉ID',
  complaint_no VARCHAR(64) NOT NULL COMMENT '投诉单号',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '投诉用户ID',
  merchant_id BIGINT UNSIGNED DEFAULT NULL COMMENT '被投诉商家ID',
  order_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID',
  type TINYINT NOT NULL COMMENT '1商品质量2服务态度3物流4其他',
  content VARCHAR(1000) NOT NULL COMMENT '投诉内容',
  images TEXT DEFAULT NULL COMMENT '凭证图片JSON',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理1处理中2已处理3已关闭',
  handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理意见',
  handler_id BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID',
  handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_complaint_no (complaint_no),
  KEY idx_user (user_id),
  KEY idx_merchant (merchant_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉表';

-- ------------------------------------------------------------
-- 15. 审核记录表
-- ------------------------------------------------------------
CREATE TABLE audit_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  biz_type VARCHAR(32) NOT NULL COMMENT 'MERCHANT/AFTER_SALE/COMPLAINT',
  biz_id BIGINT UNSIGNED NOT NULL COMMENT '业务对象ID',
  auditor_id BIGINT UNSIGNED NOT NULL COMMENT '审核人ID',
  action VARCHAR(32) NOT NULL COMMENT 'APPROVE/REJECT',
  before_status TINYINT DEFAULT NULL COMMENT '审核前状态',
  after_status TINYINT DEFAULT NULL COMMENT '审核后状态',
  remark VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  audit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_biz (biz_type, biz_id),
  KEY idx_auditor (auditor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- ------------------------------------------------------------
-- 16. 库存流水表
-- ------------------------------------------------------------
CREATE TABLE inventory_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  merchant_id BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  change_type TINYINT NOT NULL COMMENT '1入库2销售出库3售后回补4盘点5锁定6释放',
  quantity INT NOT NULL COMMENT '变动量',
  before_stock INT NOT NULL COMMENT '变动前库存',
  after_stock INT NOT NULL COMMENT '变动后库存',
  ref_type VARCHAR(32) DEFAULT NULL COMMENT 'ORDER/AFTER_SALE/MANUAL',
  ref_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联业务ID',
  operator_id BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (id),
  KEY idx_product (product_id),
  KEY idx_merchant (merchant_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- ------------------------------------------------------------
-- 17. 推荐记录表
-- ------------------------------------------------------------
CREATE TABLE recommendation_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '推荐商品ID',
  score DECIMAL(10,4) NOT NULL COMMENT '综合推荐得分',
  source VARCHAR(32) NOT NULL COMMENT 'FREQUENCY/CF/HOT/MIXED',
  algorithm_version VARCHAR(32) DEFAULT NULL COMMENT '算法版本',
  scene VARCHAR(32) DEFAULT NULL COMMENT 'HOME/DETAIL/CART',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '推荐时间',
  PRIMARY KEY (id),
  KEY idx_user (user_id),
  KEY idx_create_time (create_time),
  UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐记录表';

-- ============================================================
-- 种子数据
-- ============================================================

-- 用户（密码均为 123456 的 BCrypt 值）
INSERT INTO sys_user (id, username, password, real_name, phone, email, role, merchant_id, status) VALUES
 (1, 'admin',      '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOVTHYKIiBZ0XQ4KQp0eB6QvYcDcW', '系统管理员', '13800000001', 'admin@fruittrade.com',   'ADMIN',    NULL, 1),
 (2, 'merchant01', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOVTHYKIiBZ0XQ4KQp0eB6QvYcDcW', '陈鲜果',     '13800000002', 'merchant1@fruit.com',  'MERCHANT', 1,    1),
 (3, 'merchant02', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOVTHYKIiBZ0XQ4KQp0eB6QvYcDcW', '林果园',     '13800000003', 'merchant2@fruit.com',  'MERCHANT', 2,    1),
 (4, 'user01',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOVTHYKIiBZ0XQ4KQp0eB6QvYcDcW', '张三',       '13800000004', 'user01@fruit.com',     'USER',     NULL, 1),
 (5, 'user02',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOVTHYKIiBZ0XQ4KQp0eB6QvYcDcW', '李四',       '13800000005', 'user02@fruit.com',     'USER',     NULL, 1),
 (6, 'user03',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOVTHYKIiBZ0XQ4KQp0eB6QvYcDcW', '王五',       '13800000006', 'user03@fruit.com',     'USER',     NULL, 1);

-- 商家（2 个已审核营业中）
INSERT INTO merchant (id, user_id, merchant_name, license_no, contact_person, contact_phone, address, audit_status, audit_remark, audit_time, status) VALUES
 (1, 2, '鲜果优选',   'LIC-2026-001', '陈鲜果', '13800000002', '广东省深圳市南山区果品市场A座', 1, '资质齐全，审核通过', '2026-01-10 09:30:00', 1),
 (2, 3, '绿园鲜果',   'LIC-2026-002', '林果园', '13800000003', '浙江省杭州市余杭区农副产品市场B座', 1, '资质齐全，审核通过', '2026-02-15 10:00:00', 1),
 (3, 4, '待审果铺',   'LIC-2026-003', '赵待审', '13800000007', '江苏省南京市鼓楼区果品路8号', 0, NULL, NULL, 0);

-- 分类（6 个一级 + 6 个二级）
INSERT INTO category (id, name, parent_id, sort, icon, status) VALUES
 (1, '应季水果', 0, 1, NULL, 1),
 (2, '进口水果', 0, 2, NULL, 1),
 (3, '热带水果', 0, 3, NULL, 1),
 (4, '瓜类',     0, 4, NULL, 1),
 (5, '浆果类',   0, 5, NULL, 1),
 (6, '仁果类',   0, 6, NULL, 1),
 (11, '柑橘类',  1, 1, NULL, 1),
 (12, '苹果梨',  1, 2, NULL, 1),
 (21, '车厘子',  2, 1, NULL, 1),
 (31, '芒果',    3, 1, NULL, 1),
 (41, '西瓜',    4, 1, NULL, 1),
 (51, '草莓',    5, 1, NULL, 1);

-- 商品（20 个，含不同保质期与库存；日期以 2026-11-21 为今天）
INSERT INTO product (id, merchant_id, category_id, name, subtitle, description, price, original_price, unit, spec, origin, shelf_life_days, production_date, status, sales_count, view_count) VALUES
 (101, 1, 11, '赣南脐橙',   '酸甜多汁，产地直发', '江西赣州优质脐橙，果肉饱满。', 19.90, 25.00, '斤', '5斤装',  '江西赣州',   15, '2026-11-18', 1, 1200, 3200),
 (102, 1, 11, '砂糖橘',     '皮薄易剥，清甜可口', '广西砂糖橘，无渣多汁。',       12.50, 16.00, '斤', '3斤装',  '广西桂林',   10, '2026-11-19', 1, 980,  2100),
 (103, 1, 12, '红富士苹果', '脆甜多汁',           '陕西红富士，光照充足。',       9.90,  13.00, '斤', '5斤装',  '陕西渭南',   30, '2026-11-15', 1, 1500, 4000),
 (104, 1, 12, '砀山酥梨',   '清甜润喉',           '安徽砀山酥梨，皮薄肉细。',     7.50,  10.00, '斤', '5斤装',  '安徽砀山',   20, '2026-11-16', 1, 760,  1500),
 (105, 1, 5,  '章丘大葱',   '葱白长，味道足',     '山东章丘大葱，新鲜脆嫩。',     5.80,  8.00,  '斤', '2斤装',  '山东章丘',   12, '2026-11-17', 1, 640,  1200),
 (106, 1, 1,  '云南蓝莓',   '酸甜小浆果',         '云南高原蓝莓，果粉完整。',     29.90, 39.00, '盒', '125g/盒', '云南昆明',  7,  '2026-11-20', 1, 880,  2600),
 (107, 1, 21, '智利车厘子', '进口大樱桃',         '智利进口车厘子，JJ级。',       59.90, 79.00, '斤', '2斤装',  '智利进口',   10, '2026-11-19', 1, 520,  1800),
 (108, 1, 31, '海南芒果',   '香甜浓郁',           '海南金煌芒果，果肉厚实。',     15.80, 20.00, '斤', '5斤装',  '海南三亚',   8,  '2026-11-20', 1, 430,  1100),
 (109, 1, 41, '麒麟西瓜',   '沙甜多汁',           '山东麒麟西瓜，皮薄瓤红。',     3.50,  5.00,  '斤', '整瓜',   '山东潍坊',   14, '2026-11-18', 1, 2100, 5000),
 (110, 1, 51, '丹东草莓',   '奶油草莓，香甜',     '辽宁丹东九九草莓。',           39.90, 52.00, '盒', '500g/盒', '辽宁丹东',   5,  '2026-11-20', 1, 690,  2300),
 (111, 2, 11, '爱媛果冻橙', '爆汁果冻橙',         '四川爱媛38号，入口即化。',     22.80, 29.00, '斤', '5斤装',  '四川眉山',   15, '2026-11-17', 1, 1100, 2800),
 (112, 2, 11, '沃柑',       '晚熟柑橘，清甜',     '广西沃柑，糖酸比适中。',       11.80, 15.00, '斤', '5斤装',  '广西南宁',   18, '2026-11-16', 1, 920,  1900),
 (113, 2, 12, '阿克苏苹果', '冰糖心苹果',         '新疆阿克苏，糖心明显。',       16.50, 22.00, '斤', '5斤装',  '新疆阿克苏', 35, '2026-11-10', 1, 1300, 3100),
 (114, 2, 12, '皇冠梨',     '酥脆清甜',           '河北皇冠梨，果面光洁。',       8.80,  12.00, '斤', '5斤装',  '河北石家庄', 25, '2026-11-14', 1, 580,  1300),
 (115, 2, 31, '贵妃芒',     '红金芒果',           '海南贵妃芒，香甜纤维少。',     18.80, 24.00, '斤', '5斤装',  '海南三亚',   7,  '2026-11-21', 1, 470,  1400),
 (116, 2, 5,  '阳光玫瑰',   '玫瑰香味，脆甜',     '云南阳光玫瑰葡萄。',           25.80, 33.00, '斤', '2斤装',  '云南曲靖',   6,  '2026-11-20', 1, 380,  1700),
 (117, 2, 21, '美国车厘子', '进口大樱桃',         '美国西北车厘子，XL级。',       49.90, 65.00, '斤', '2斤装',  '美国进口',   12, '2026-11-18', 1, 340,  1600),
 (118, 2, 41, '8424西瓜',   '早熟薄皮西瓜',       '江苏8424西瓜，甜度高。',       4.20,  6.00,  '斤', '整瓜',   '江苏盐城',   12, '2026-11-19', 1, 1800, 4200),
 (119, 2, 51, '红颜草莓',   '日系草莓品种',       '江苏红颜草莓，果形端正。',     35.80, 46.00, '盒', '500g/盒', '江苏南京',   5,  '2026-11-21', 1, 410,  1200),
 (120, 2, 3,  '台湾凤梨',   '无需泡盐水',         '台湾金钻凤梨，甜度高。',       13.80, 18.00, '斤', '整果',   '台湾进口',   10, '2026-11-18', 0, 260,  900);

-- 商品图片
INSERT INTO product_image (product_id, url, sort, is_main) VALUES
 (101, '/img/orange1.jpg', 0, 1), (101, '/img/orange2.jpg', 1, 0),
 (103, '/img/apple1.jpg',  0, 1), (109, '/img/watermelon1.jpg', 0, 1),
 (111, '/img/orange3.jpg', 0, 1), (115, '/img/mango1.jpg', 0, 1);

-- 库存（覆盖健康、临期、低库存、零库存）
INSERT INTO inventory (product_id, merchant_id, stock, locked_stock, safety_stock, warning_level, version) VALUES
 (101, 1, 500, 0, 50, 0, 0),
 (102, 1, 300, 0, 50, 0, 0),
 (103, 1, 800, 0, 50, 0, 0),
 (104, 1, 120, 0, 50, 0, 0),
 (105, 1, 30,  0, 50, 2, 0),
 (106, 1, 200, 0, 30, 1, 0),
 (107, 1, 80,  0, 30, 0, 0),
 (108, 1, 150, 0, 30, 2, 0),
 (109, 1, 600, 0, 50, 0, 0),
 (110, 1, 90,  0, 30, 3, 0),
 (111, 2, 400, 0, 50, 0, 0),
 (112, 2, 350, 0, 50, 0, 0),
 (113, 2, 700, 0, 50, 0, 0),
 (114, 2, 260, 0, 50, 0, 0),
 (115, 2, 45,  0, 50, 3, 0),
 (116, 2, 110, 0, 30, 2, 0),
 (117, 2, 75,  0, 30, 0, 0),
 (118, 2, 420, 0, 50, 0, 0),
 (119, 2, 5,   0, 30, 3, 0),
 (120, 2, 0,   0, 50, 3, 0);

-- 收货地址
INSERT INTO address (id, user_id, receiver, phone, province, city, district, detail, is_default) VALUES
 (1, 4, '张三', '13900000004', '广东省', '深圳市', '南山区', '科技园路1号', 1),
 (2, 4, '张三', '13900000004', '广东省', '深圳市', '福田区', '深南大道200号', 0),
 (3, 5, '李四', '13900000005', '浙江省', '杭州市', '西湖区', '文三路88号', 1),
 (4, 6, '王五', '13900000006', '江苏省', '南京市', '鼓楼区', '中山北路66号', 1);

-- 购物车
INSERT INTO cart (user_id, product_id, quantity, selected) VALUES
 (4, 101, 2, 1),
 (4, 103, 1, 1),
 (4, 106, 3, 0),
 (5, 111, 2, 1),
 (6, 109, 1, 1);

-- 订单（覆盖各状态 0/1/2/3/4/5）
INSERT INTO orders (id, order_no, user_id, merchant_id, address_id, receiver, receiver_phone, receiver_address, total_amount, freight, pay_amount, pay_method, status, pay_time, ship_time, finish_time, close_time, remark) VALUES
 (1, '2026111800010001', 4, 1, 1, '张三', '13900000004', '广东省深圳市南山区科技园路1号', 59.70, 0.00, 59.70, 1, 3, '2026-11-18 10:05:00', '2026-11-18 14:00:00', '2026-11-20 16:30:00', NULL, '尽快发货'),
 (2, '2026111900010002', 4, 1, 1, '张三', '13900000004', '广东省深圳市南山区科技园路1号', 39.80, 0.00, 39.80, 2, 2, '2026-11-19 09:12:00', '2026-11-19 18:00:00', NULL, NULL, NULL),
 (3, '2026112000010003', 5, 2, 3, '李四', '13900000005', '浙江省杭州市西湖区文三路88号', 45.60, 0.00, 45.60, 1, 1, '2026-11-20 11:30:00', NULL, NULL, NULL, NULL),
 (4, '2026112100010004', 6, 2, 4, '王五', '13900000006', '江苏省南京市鼓楼区中山北路66号', 22.80, 0.00, 22.80, 1, 0, NULL, NULL, NULL, NULL, '待支付'),
 (5, '2026111700010005', 4, 1, 1, '张三', '13900000004', '广东省深圳市南山区科技园路1号', 15.80, 0.00, 15.80, 1, 4, '2026-11-17 08:00:00', NULL, NULL, '2026-11-17 08:30:00', '超时关闭'),
 (6, '2026111600010006', 5, 2, 3, '李四', '13900000005', '浙江省杭州市西湖区文三路88号', 63.60, 0.00, 63.60, 2, 5, '2026-11-16 15:20:00', '2026-11-17 09:00:00', NULL, NULL, '售后中');

-- 订单明细
INSERT INTO order_item (order_id, product_id, product_name, product_image, price, quantity, subtotal) VALUES
 (1, 101, '赣南脐橙',   '/img/orange1.jpg', 19.90, 2, 39.80),
 (1, 105, '章丘大葱',   NULL,               5.80,  1, 5.80),
 (1, 104, '砀山酥梨',   NULL,               7.50,  2, 15.00),
 (2, 101, '赣南脐橙',   '/img/orange1.jpg', 19.90, 2, 39.80),
 (3, 111, '爱媛果冻橙', '/img/orange3.jpg', 22.80, 2, 45.60),
 (4, 111, '爱媛果冻橙', '/img/orange3.jpg', 22.80, 1, 22.80),
 (5, 108, '海南芒果',   NULL,               15.80, 1, 15.80),
 (6, 113, '阿克苏苹果', NULL,               16.50, 2, 33.00),
 (6, 117, '美国车厘子', NULL,               49.90, 1, 49.90);

-- 支付记录
INSERT INTO payment_record (payment_no, order_id, order_no, user_id, amount, pay_method, status, trade_no, paid_time) VALUES
 ('PAY202611180001', 1, '2026111800010001', 4, 59.70, 1, 1, 'SIM-WX-202611180001', '2026-11-18 10:05:00'),
 ('PAY202611190001', 2, '2026111900010002', 4, 39.80, 2, 1, 'SIM-ALI-202611190001', '2026-11-19 09:12:00'),
 ('PAY202611200001', 3, '2026112000010003', 5, 45.60, 1, 1, 'SIM-WX-202611200001', '2026-11-20 11:30:00'),
 ('PAY202611160001', 6, '2026111600010006', 5, 63.60, 2, 1, 'SIM-ALI-202611160001', '2026-11-16 15:20:00');

-- 物流记录
INSERT INTO logistics (order_id, logistics_no, carrier, status, traces, ship_time, deliver_time) VALUES
 (1, 'SF1234567890', '顺丰速运', 4, '[{"time":"2026-11-18 14:00:00","text":"已揽收"},{"time":"2026-11-20 16:30:00","text":"已签收"}]', '2026-11-18 14:00:00', '2026-11-20 16:30:00'),
 (2, 'YT2345678901', '圆通速递', 2, '[{"time":"2026-11-19 18:00:00","text":"已揽收"},{"time":"2026-11-21 08:00:00","text":"运输中"}]', '2026-11-19 18:00:00', NULL),
 (6, 'SF3456789012', '顺丰速运', 1, '[{"time":"2026-11-17 09:00:00","text":"已揽收"}]', '2026-11-17 09:00:00', NULL);

-- 售后申请
INSERT INTO after_sale (after_sale_no, order_id, order_item_id, user_id, merchant_id, type, reason, description, amount, status, handle_remark) VALUES
 ('AS202611160001', 6, 8, 5, 2, 2, '商品质量问题', '苹果有磕碰损坏', 33.00, 0, NULL),
 ('AS202611180001', 1, 1, 4, 1, 1, '少件', '收到少两个橙子', 39.80, 3, '已补发，处理完成');

-- 投诉
INSERT INTO complaint (complaint_no, user_id, merchant_id, order_id, type, content, status, handle_remark) VALUES
 ('CP202611200001', 4, 1, 1, 1, '收到的脐橙有部分腐烂，要求处理。', 0, NULL),
 ('CP202611190001', 5, 2, 3, 2, '客服回复慢，服务态度一般。', 2, '已联系商家道歉并补偿优惠券');

-- 审核记录
INSERT INTO audit_record (biz_type, biz_id, auditor_id, action, before_status, after_status, remark, audit_time) VALUES
 ('MERCHANT', 1, 1, 'APPROVE', 0, 1, '资质齐全', '2026-01-10 09:30:00'),
 ('MERCHANT', 2, 1, 'APPROVE', 0, 1, '资质齐全', '2026-02-15 10:00:00'),
 ('AFTER_SALE', 2, 1, 'APPROVE', 0, 3, '已补发', '2026-11-19 10:00:00');

-- 库存流水
INSERT INTO inventory_log (product_id, merchant_id, change_type, quantity, before_stock, after_stock, ref_type, ref_id, operator_id, remark) VALUES
 (101, 1, 1, 500, 0, 500, 'MANUAL', NULL, 2, '初始入库'),
 (103, 1, 1, 800, 0, 800, 'MANUAL', NULL, 2, '初始入库'),
 (101, 1, 5, -2, 502, 500, 'ORDER', 1, 4, '下单锁定'),
 (101, 1, 6, 2, 500, 502, 'ORDER', 1, 4, '支付释放锁定'),
 (105, 1, 2, -1, 31, 30, 'ORDER', 1, 4, '销售出库');

-- 推荐记录
INSERT INTO recommendation_log (user_id, product_id, score, source, algorithm_version, scene) VALUES
 (4, 101, 0.9200, 'MIXED', 'v1.0', 'HOME'),
 (4, 103, 0.8500, 'MIXED', 'v1.0', 'HOME'),
 (4, 109, 0.7800, 'CF',    'v1.0', 'HOME'),
 (5, 111, 0.8800, 'HOT',   'v1.0', 'HOME');

-- 重置自增计数
ALTER TABLE sys_user AUTO_INCREMENT=7;
ALTER TABLE merchant AUTO_INCREMENT=4;
ALTER TABLE product AUTO_INCREMENT=121;
ALTER TABLE orders AUTO_INCREMENT=7;
