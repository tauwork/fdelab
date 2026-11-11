-- ============================================================
-- 产品与服务 · 结构 DDL（环境必备，跑一次）
-- 依据：docs/product-service-impl-plan.md §2.4（逐字抄，不改类型长度）
-- 说明：dev 造数见 sql/product-service-dev.sql（dev only，不进生产）
-- 幂等：CREATE TABLE IF NOT EXISTS
-- ============================================================
USE fdelab;

-- 运作层：4 行
CREATE TABLE IF NOT EXISTS fde_product_form (
  id            BIGINT       NOT NULL,
  code          VARCHAR(32)  NOT NULL COMMENT 'F1~F4',
  name          VARCHAR(200) NOT NULL,
  form          VARCHAR(64)  NULL COMMENT '产品运作 ①',
  weight        VARCHAR(32)  NULL COMMENT '最轻 · 不改变交付模式',
  sort_order    SMALLINT     NULL,
  fit           VARCHAR(300) NULL COMMENT '适合谁',
  description   TEXT         NULL,
  deliverables  JSON         NULL COMMENT '交付物清单（有序数组）',
  cycle         VARCHAR(64)  NULL,
  pricing       VARCHAR(64)  NULL,
  success       TEXT         NULL,
  case_ids      JSON         NULL COMMENT '参照案例 id 数组',
  customer_ids  JSON         NULL COMMENT '已购客户 id 数组（一期不展示）',
  published     TINYINT      NOT NULL DEFAULT 1,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_fde_product_form_code (code),
  KEY idx_fde_product_form_order (sort_order),
  KEY idx_fde_product_form_pub (published, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品运作（4 条）';

-- 条目层：产品条目（v15 PRODUCT_IDS 全集，6 行）
-- 详情内容不进真列：详情页尚未设计，全部进 detail JSON（键名待定，见 §2.3.1）
CREATE TABLE IF NOT EXISTS fde_product_item (
  id            BIGINT       NOT NULL,
  code          VARCHAR(32)  NOT NULL,
  product_id    BIGINT       NULL COMMENT '→ fde_product_form.id',
  kind          VARCHAR(16)  NOT NULL COMMENT 'm/de/ag/sa/vs/tr/pk',
  name          VARCHAR(120) NOT NULL,
  status        VARCHAR(16)  NOT NULL COMMENT 'on/beta/dev',
  summary       VARCHAR(500) NULL,
  cycle         VARCHAR(64)  NULL,
  pricing       VARCHAR(64)  NULL,
  detail        JSON         NULL COMMENT '详情页内容，键名由详情页设计定稿后登记；后端只校验合法 JSON',
  sort_order    SMALLINT     NULL,
  published     TINYINT      NOT NULL DEFAULT 1,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_fde_product_item_code (code),
  KEY idx_item_product (product_id, sort_order),
  KEY idx_item_filter (kind, published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品条目';
