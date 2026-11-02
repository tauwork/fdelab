USE fdelab;

CREATE TABLE IF NOT EXISTS fde_talent (
    id            BIGINT        NOT NULL,
    name          VARCHAR(64)   NOT NULL,
    slug          VARCHAR(64)   NOT NULL,
    headline      VARCHAR(200)  NOT NULL,
    summary       VARCHAR(500)  NULL,
    avatar_url    VARCHAR(500)  NULL,
    phone         VARCHAR(32)   NULL,
    wechat        VARCHAR(64)   NULL,
    website       VARCHAR(200)  NULL,
    website_label VARCHAR(64)   NULL,
    location      VARCHAR(120)  NULL,
    like_count    INT           NOT NULL DEFAULT 0,
    published     TINYINT       NOT NULL DEFAULT 1 COMMENT '1发布 0下架',
    profile_json  JSON          NULL,
    deleted       TINYINT       NOT NULL DEFAULT 0,
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fde_talent_slug (slug),
    KEY idx_fde_talent_like (like_count, published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fde_talent_like (
    id          BIGINT      NOT NULL,
    talent_id   BIGINT      NOT NULL,
    visitor_key VARCHAR(64) NOT NULL,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fde_talent_like (talent_id, visitor_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO fde_talent (
    id, name, slug, headline, summary, phone, wechat, website, website_label, location,
    like_count, published, profile_json, deleted
)
SELECT 201,
       '蒋利文',
       'jiangliwen',
       '企业数智化转型导师 · FDE 智研社主理人',
       'FDE · AI 落地最后一公里。从战略规划走到现场交付。',
       '15818581995',
       '蒋利文 Levin',
       'https://tauwork.ai',
       'Tauwork.ai',
       '常驻广东 · 深圳（老家湖南人）',
       18,
       1,
       JSON_OBJECT(
           'badges', JSON_ARRAY('FDE · AI 落地最后一公里', 'AI 战略增长操作系统'),
           'identityTags', JSON_ARRAY('企业数智化转型导师', '连续创业者', 'CTO / CIO 出身', '实战讲师', 'FDE 交付工程师'),
           'industryTags', JSON_ARRAY('离散制造', '公用事业', '跨境企业服务', '教育培训', '互联网大厂'),
           'roles', JSON_ARRAY(
               JSON_OBJECT('org', 'Tauwork 韬工智能', 'title', '联合创始人', 'description', 'Agent 集成工厂'),
               JSON_OBJECT('org', '数智化转型实战讲师顾问', 'title', '企业培训 + 顾问陪跑', 'description', '从战略规划到落地交付'),
               JSON_OBJECT('org', 'FDE 智研社', 'title', '主理人', 'description', '聚焦「AI 落地的最后一公里」')
           ),
           'experiences', JSON_ARRAY(
               JSON_OBJECT('title', '大厂历练', 'description', '先后任职于华为、腾讯、网易，深耕企业级产品与技术体系'),
               JSON_OBJECT('title', '企业数字化操盘', 'description', '曾任泽宝、德必等企业 CTO / CIO，主导产品与信息化体系建设'),
               JSON_OBJECT('title', '创业当下', 'description', '连续创业者，当前联合创立 Tauwork 韬工智能，打造 Agent 集成工厂')
           ),
           'services', JSON_ARRAY(
               JSON_OBJECT('title', 'AI 战略课程与企业工作坊', 'description', '高管战略增长课 · 组织进化课 · 骨干 Agent 实战课（半天至两天）'),
               JSON_OBJECT('title', 'AI FDE 陪跑营', 'description', '12 周制企业 AI 骨干陪跑：业务翻译 → 技术编排 → 落地工程 → 结果治理'),
               JSON_OBJECT('title', '企业 AI 转型方案规划', 'description', 'AI 转型路线图与系统选型：MES / ERP / PLM 复用存量、少定制、可落地'),
               JSON_OBJECT('title', '战略执行数字化系统', 'description', '自研 ABOS 战略系统：战略地图 · BSC 计分卡 · 风控推演一体化')
           ),
           'collaborations', JSON_ARRAY(
               JSON_OBJECT('title', '企业 AI 落地陪跑机会', 'description', '愿意做第一批 FDE 陪跑共创企业，用真实场景打磨方法论'),
               JSON_OBJECT('title', '课程与培训合作渠道', 'description', '培训机构、商会、产业园区联合开课与获客分成'),
               JSON_OBJECT('title', '咨询 Agent 开发伙伴', 'description', '与咨询伙伴们共同打造 Agent，更好地服务客户')
           ),
           'cases', JSON_ARRAY(
               JSON_OBJECT(
                   'industry', '制造业 · AI 转型方案规划',
                   'title', '精密传感器制造企业的数字化与 AI 路线抉择',
                   'portrait', '客户画像：华南某精密传感器制造企业 · 约 200 人 · 年产值 8000 万+ · 按 IATF16949 体系执行 · 多品种小批量',
                   'points', JSON_ARRAY(
                       '先盘存量再谈方案：ERP 已在用、PLM 试跑暂停、精益有外部顾问，把「不推倒重来」定成前提',
                       '两条路线摆到桌面：总包复用存量 ERP + 独立 MES 集成 vs 一体化平台，分水岭是愿不愿动 ERP',
                       '100–200 万投资区间做分期规划，标出 POC 验证项与行业参考 ROI 区间（非承诺值）'
                   ),
                   'delivery', '两版董事会级方案汇报（19 页总包版 / 18 页一体化版）+ 选型决策清单；现场对标同行标杆智能产线后，客户确认推进方向'
               ),
               JSON_OBJECT(
                   'industry', '自研系统 · 战略执行',
                   'title', 'ABOS 战略执行系统：把战略地图变成可计算的系统',
                   'portrait', '解决的场景：战略与绩效两张皮、目标无法追溯、复盘靠拍脑袋的中小企业与服务平台',
                   'points', JSON_ARRAY(
                       '三层结构：战略地图（本体图谱）+ BSC 计分卡（视角与因果边）+ 风控推演（风险边、敞口回填、异动时间线）',
                       'KPI 指标管道：linear / threshold 两种打分口径、期间锁定与证据链，绩效结果可追溯',
                       '复盘与校准闭环：推演 P50 与实际偏差自动生成卡，校准建议回写关系弹性系数并留审计'
                   ),
                   'delivery', '已发布 v0.1.0（M1–M5），55 项自动化测试通过；作为 Tauwork 战略数字化方向的自研产品底座持续迭代'
               )
           )
       ),
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_talent WHERE slug = 'jiangliwen');

INSERT INTO fde_talent (
    id, name, slug, headline, summary, location, like_count, published, profile_json, deleted
)
SELECT 202,
       '周衡',
       'zhouheng',
       '制造现场 AI 交付工程师',
       '示例档案，用来演示人才按点赞排序。可在后台修改或下架。',
       '常驻江苏 · 苏州',
       5,
       1,
       JSON_OBJECT(
           'badges', JSON_ARRAY('FDE · 现场交付'),
           'identityTags', JSON_ARRAY('FDE 交付工程师', '实战讲师'),
           'industryTags', JSON_ARRAY('离散制造'),
           'roles', JSON_ARRAY(
               JSON_OBJECT('org', 'FDELab 示例', 'title', '交付工程师', 'description', '把车间问题翻译成可运行的 Agent 流程')
           ),
           'experiences', JSON_ARRAY(
               JSON_OBJECT('title', '现场实施', 'description', '在多品种小批量车间做过报工、质检和异常闭环的试点')
           ),
           'services', JSON_ARRAY(
               JSON_OBJECT('title', '车间 Agent 试点', 'description', '两周内选定一个班组场景，跑通采集、判断和回写')
           ),
           'collaborations', JSON_ARRAY(
               JSON_OBJECT('title', '寻找试点车间', 'description', '希望和愿意开放一个班组数据的工厂一起做第一版')
           ),
           'cases', JSON_ARRAY(
               JSON_OBJECT(
                   'industry', '离散制造 · 质检',
                   'title', '注塑车间的首件确认',
                   'portrait', '示例案例：一家 80 人的注塑厂，首件确认靠纸质记录',
                   'points', JSON_ARRAY('把首件照片和工艺参数收成一张卡', '异常项直接推给班组长'),
                   'delivery', '试点班组两周内停用纸质首件单'
               )
           )
       ),
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_talent WHERE slug = 'zhouheng');
