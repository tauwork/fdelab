-- ============================================================
-- FDE 标杆案例库一期建表 + 种子数据
-- 依据：docs/database-design-phase1.md 3.2 / 3.3
-- 种子：docs/seed-cases.json（脚本产物，入库逐字读取）
-- 注意：客户 code（CUST-001 / CUST-002）为示例值，编号规则待业务确认。
-- 幂等：CREATE TABLE IF NOT EXISTS + INSERT ... ON DUPLICATE KEY UPDATE
-- ============================================================
USE fdelab;

CREATE TABLE IF NOT EXISTS fde_customer (
    id              BIGINT       NOT NULL,
    code            VARCHAR(32)  NOT NULL,
    display_name    VARCHAR(200) NOT NULL,
    is_masked       TINYINT      NOT NULL DEFAULT 0 COMMENT '1 脱敏 / 0 具名',
    primary_fde_id  BIGINT       NULL,
    service_fde_ids JSON         NULL,
    published       TINYINT      NOT NULL DEFAULT 1 COMMENT '1发布 0下架',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fde_customer_code (code),
    KEY idx_fde_customer_masked (is_masked, deleted),
    KEY idx_fde_customer_primary_fde (primary_fde_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fde_case (
    id              BIGINT       NOT NULL,
    code            VARCHAR(32)  NOT NULL,
    title           VARCHAR(200) NOT NULL,
    subtitle        VARCHAR(300) NULL,
    industry        VARCHAR(64)  NULL,
    func            VARCHAR(64)  NULL,
    occur_year      VARCHAR(32)  NULL COMMENT '区间字符串，排序取前 4 位起始年',
    partner         VARCHAR(200) NULL COMMENT '交付方 / FDE 主体，纯文本',
    cred            VARCHAR(8)   NULL COMMENT 'A/B/C/D',
    cred_note       VARCHAR(500) NULL,
    fde_mode        VARCHAR(64)  NULL,
    scale           VARCHAR(64)  NULL,
    model_pct       DECIMAL(4,1) NULL COMMENT '模型含量，保留一位小数；不可用 TINYINT（57.5 会被存成 58）',
    pain            TEXT         NULL,
    goal            TEXT         NULL,
    verdict         TEXT         NULL COMMENT 'JSON 字符串（线上是对象）',
    testimony       TEXT         NULL COMMENT 'JSON 字符串（线上是数组）',
    content_json    JSON         NULL,
    primary_fde_id  BIGINT       NULL,
    contributor_ids JSON         NULL,
    customer_id     BIGINT       NULL,
    published       TINYINT      NOT NULL DEFAULT 1 COMMENT '1发布 0下架',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fde_case_code (code),
    KEY idx_fde_case_pub (published, deleted),
    KEY idx_fde_case_customer (customer_id),
    KEY idx_fde_case_primary_fde (primary_fde_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 客户种子（code 为示例值，待业务确认）
INSERT INTO fde_customer (id, code, display_name, is_masked, published, deleted)
VALUES
    (4001, 'CUST-001', '大型半导体客户（官方未具名）', 1, 1, 0),
    (4002, 'CUST-002', 'Airbus（具名）', 0, 1, 0)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    is_masked = VALUES(is_masked),
    published = VALUES(published),
    deleted = VALUES(deleted),
    update_time = CURRENT_TIMESTAMP;

-- 案例种子（逐字取 docs/seed-cases.json；kpis 含技术标注 m，前台忽略）
INSERT INTO fde_case (id, code, title, subtitle, industry, func, occur_year, partner, cred, cred_note, fde_mode, scale, model_pct, pain, goal, verdict, testimony, content_json, primary_fde_id, contributor_ids, customer_id, published, deleted)
VALUES
  (
    4101,
    'CASE-001',
    'OpenAI FDE × 大型半导体客户',
    '芯片验证流程 AI 原生化',
    '半导体/集成电路',
    '芯片研发 · 设计验证',
    '2025–2026',
    'OpenAI Forward Deployed Engineering',
    'C',
    '厂商侧匿名（具名高管署名发布，接近 B）',
    '驻场嵌入式',
    'L:50–200 人月',
    57.5,
    '验证环节占芯片项目工作量约 70%。定向测试生成依赖资深工程师经验，失效调试以「周」计，架构知识散落在少数专家脑子里——没有棋盘、没有裁判、没有指南针。',
    '把验证环节从「人驱动」改造为 AI 原生的可并行、可度量流程；建成可跨团队复用的 harness，而非一次性定制。',
    '{"deposit":"双侧，客户侧沉淀的是评测体系（Eval）","modelPct":"中高 ≈57.5%","reusable":"高：90% 定制代码删除、第二团队 1 天上线","economics":"人天 → 混合 → token，路径已跑通一段","survive":"部分会被冲掉，但客户侧的 Eval 不会","answer":"早期形态会被替代、也该被替代——那 90% 被删掉的定制代码正是「用发动机更高效地养马」的部分。但真正留下来的不是代码，是 Eval：「什么叫对」的业务判断长在客户侧，随模型升级而增值。把交付物定义为「验证器／评测体系设计」而非驻场写代码，是与基模解耦的正解。"}',
    NULL,
    '{"tech":["Agent 编排","代码生成","评测与验证器","MCP/工具集成","可观测性"],"ai":["流程自动化","决策辅助","知识问答"],"mgmt":["一号位牵头","人机协同与人工审核","能力内化","AI CoE"],"kpis":[{"v":"-48%","k":"定向测试生成耗时","r":true,"m":0},{"v":"+22%","k":"Kernel 性能（中位）","m":1},{"v":"-90%","k":"定制代码量","r":true,"m":4},{"v":"1 天","k":"第二团队部署周期","m":5}],"timeline":[["① 选定高价值用例","并行推进 10+ 个验证场景"],["② FDE 嵌入客户团队，共建 Eval","评测先行：先定义「什么叫对」才能迭代"],["③ 定制 harness（学费期）","各用例自建终端 UI / CLI"],["④ 生产部署 + 早期采用","3–6 个月内全部上线"],["⑤ 迁移 Codex 共享 harness","定制应用降级为 plugin / skill"],["⑥ 删除约 90% 定制代码","自建 UI / CLI 全部退役"],["⑦ 扩展到新团队","换 connector / MCP server + 新建 eval"]],"org":[["OpenAI FDE","~15 人","驻场嵌入、写代码、建 eval、推上线"],["客户验证工程师","团队级","提供领域知识、共建 eval、审核输出"],["Product 团队","穿插","把可复用模式产品化"],["Research","按需","补模型能力缺口"]],"metrics":[["定向测试生成耗时","完成一次定向测试生成所需工时","基线 100%","节省 ~48%","上线后 3–6 个月","C"],["Kernel 性能","优化前后性能基准，取各用例中位数","基线","中位 +22%","同上","C"],["调试周期","从失效发生到定位根因的时长","数周","数小时（压缩 1–2 个数量级）","同上","C"],["用例生产化率","达生产状态用例 / 总用例","0","10+/10+ 全部上线","3–6 个月","C"],["定制代码量","单项目定制代码行数","基线 100%","删除 ~90%","Codex 重构后","C"],["跨团队推广周期","从启动到新团队可用","首次 2 个月","第二次 1 天（缩短约 98%）","2026 年 8 月","C"]],"csf":[["组织","FDE 与客户工程师同场办公式嵌入","隐性知识只能通过共处传递","需求层层翻译，做出来不对症"],["组织","通才 + 领域专家混编","通才贡献跨行业模式识别，专家保证判断可靠","纯通才不可靠；纯专家无法抽象"],["技术","Eval 先行——建能力前先建评测","无 ground truth，先定义「什么叫对」才能迭代验收","靠主观 Demo 判断，上线即下线"],["技术","统一 harness + MCP/Connector 解耦","领域逻辑与集成层分离，换环境只换连接器","每个团队重写一遍，退回外包"],["流程","从第一天瞄准结果而非 Demo","亮眼的 Demo 容易，扛生产的系统很难","止步 POC，沦为情绪价值"],["流程","10+ 用例并行后收敛共性再抽象","先发散找到真共性，再抽象才准确","过早抽象→抽象错；永不抽象→外包"],["过程","FDE 主动补齐领域知识","不懂业务就无法质疑客户的错误假设","沦为需求实现机"],["过程","双回流：问题→Product + Research","单靠 FDE 无法突破模型能力天花板","遇天花板就卡死，只能降需求"],["依赖","客户 CoE 承接 skills 管控分发","改进需集中下发，否则重复造轮子","能力散落，第二个团队仍从零开始"],["依赖","客户工程师投入时间共建 eval","eval 只能由业务专家定义，质量决定一切","无 eval→无法度量→无法证明价值→被砍"]],"quotes":[["Start with the outcome, do what it takes to succeed, then follow with scale.","Colin Jarvis，OpenAI FDE 负责人"],["如果我们每隔几个月就让自己「失业」一次，说明 FDE 团队在好好干活。","Colin Jarvis"],["Did you guys work as chip designers before coming here?","客户 Chief Hardware Officer 的反问"]],"sources":[["What FDE means at OpenAI — Colin Jarvis, 2026-10-22","https://openai.com/index/forward-deployed-engineering-means/","厂商官方（一手）"],["On the frontlines of AI deployment — The OpenAI Deployment Company","https://openai.com/business/the-openai-deployment-company","厂商官方（一手）"]]}',
    NULL,
    NULL,
    4001,
    1,
    0
  ),
  (
    4102,
    'CASE-002',
    'Palantir × Airbus',
    'A350 产能爬坡与 Skywise 平台化',
    '制造业(航空航天)',
    '生产制造 · 供应链 · 维修运营',
    '2015 → 2026（跨度 10 年+）',
    'Palantir Technologies（FDE 内部代号 Delta）',
    'B',
    '厂商具名 + Airbus 官方双向确认（部分指标 A）',
    '驻场嵌入式',
    'XL:>200 人月',
    25.0,
    '2015 年末 A350 产能爬坡危机，延误面临数亿美元违约风险。一架 A350 由 500 万个零件、4 国 8+ 工厂、数百团队协同建造；生产数据散落在 25+ 个孤立系统，工程师靠 Excel 和电话确认一个机翼吊架的到货状态。',
    '在不牺牲质量的前提下完成产能爬坡；把约 3 年的爬坡周期显著压缩；建立可复用数据资产。',
    '{"deposit":"双侧，Airbus 侧沉淀的是本体论与数据网络","modelPct":"低 ≈25%（2015 年 LLM 尚未出现）","reusable":"极高：一次工厂驻场长成行业级平台，独立为 750 人子公司","economics":"已跨过人天依赖：软件级毛利率约 84%","survive":"基本不会：壁垒是数据网络效应 + 适航合规 + 多方信任","answer":"这是「被质疑者最难反驳」的一类——模型含量只有 25%，近乎高端定制，一做十年，结果却长成了 Skywise。关键不在于写了多少代码，而在于沉淀物长成了什么形状：不是定制脚本，而是 Ontology + 多方数据网络 + 权限治理体系。可抄两条规则：①「3 家客户」抽象触发阈值；②先纵向打通一条完整业务链路，再横向扩展。"}',
    '[["「我们每天至少避免一架飞机停场，主要靠预测性告警。」","Allegiant Air · 维修运营规划与分析总监"],["某航司 AOG 减少 20 次/年；月度报告从 8 人 3 周 → 1 人不到 1 天；零件追踪 10h/89 个 → 不到 1h/3,566 个。","Airbus 官方宣传册（2019）"]]',
    '{"tech":["数据管道/本体建模","数字孪生/仿真","安全合规(SSO/VPC/RBAC)","可观测性","实时/边缘数据"],"ai":["预测分析","异常检测","决策辅助","流程自动化"],"mgmt":["一号位牵头","变更管理/采用率","数据主权/合规审查","供应商治理"],"kpis":[{"v":"+33%","k":"A350 交付速度","r":true,"m":0},{"v":"12,000","k":"连接飞机数（49% 机队）","m":2},{"v":"50,000+","k":"平台用户（Airbus 官方）","m":3},{"v":"14 天","k":"故障预警提前量","m":5}],"timeline":[["① 工程师视角梳理业务流程","先拆流程，再谈工具"],["② 整合数据、统一接口、设权限","建立本体论（Ontology）：飞机/零件/工位/班组为互相关联对象"],["③ 定制界面 + 自动化","2015→2016：交付速度 +33%"],["④ 横向扩展（2016）","延伸至排程、供应链、财务"],["⑤ 平台化（2017）","发布 Skywise 开放平台"],["⑥ 3 家客户踩同一坑 → 抽象成组件","「从泥泞到高速公路」，下一个客户直接上高速"],["⑦ 生态化（2026-04）","与 Navblue 合并，750 人数字航空子公司"]],"org":[["FDE（Delta）","常驻现场","深入工厂与制造人员并肩工作"],["部署战略家（Echo）","Palantir","摸清利益关系与未言明的优先级——「客户问题的产品经理」"],["制造/供应链/质量团队","Airbus","提供领域知识、共同定义数据对象"],["核心产品团队","Palantir 后方","监控全球 FDE 重复踩的坑，负责抽象与平台化"]],"metrics":[["A350 交付速度","单位时间交付架数","基线 100%","+33%","2015→2016","B"],["产能爬坡周期","达目标产能所需时长","~3 年","不到 2 年","同上","B"],["平台连接飞机数","Skywise 接入在役飞机","0（2017）","~12,000 架（占机队 49%）","2026-02","A"],["平台用户数","Skywise 全球用户","0","50,000+","2026-02","A"],["识别出的年化节省","平台识别的年度节省空间（估算）","—","~$1.7B/年（识别值，非已实现）","未标注","B"],["故障预警提前量","相对阈值告警的提前天数","0 天","平均 14 天","2026","C"]],"csf":[["组织","Echo-Delta 双人组","「说得清的痛点」往往不是真痛点，需两种能力分头作战","只派工程师→不对症；只派顾问→停在 PPT"],["组织","FDE 招聘标准对齐核心研发","现场要啃没有接口的老系统","FDE 沦为二线实施，降级为外包"],["组织","客户侧一号位数字官牵头","打通 25+ 孤立系统本质是权力重构","部门各守数据，集成无限期拖延"],["技术","本体论重构而非数据集成","业务实体定义成互相关联对象，才能做传导分析","只做数据湖：无语义，仍是报表"],["技术","数据隔离与细粒度权限为第一约束","竞争航司同处一平台，无法证明隔离就没人上车","平台无人敢用，网络效应归零"],["流程","三步法顺序不可颠倒","先理流程→再整数据与权限→最后定制界面","一上来就建模：无业务语义，无法验收"],["流程","「3 家客户」抽象触发规则","把「何时从定制转向平台」变成可执行阈值","无规则：要么永远定制＝外包，要么过早抽象＝抽象错"],["过程","工程师长期泡在现场","制造知识是隐性的，不在文档里","远程接需求：信息失真，无法上线"],["过程","先纵向打通一条链路再横向扩展","先在一条链路跑通验证价值，再复制","一上来铺全公司：无处证明价值，被砍"],["依赖","客户愿共享数据换能力","平台价值随接入规模指数增长","数据供给不足，冷启动失败"],["依赖","强监管环境反成护城河","高门槛挡住通用竞品，建成后难以替换","门槛低的场景被通用工具快速抹平"]],"quotes":[["Nobody would have believed that we could have had that impact so fast in the aviation industry.","Marc Fontaine，Airbus 前数字转型官"],["「从泥泞到高速公路」——前线 FDE 先铺碎石路，后方产品团队发现 3 家以上客户踩同一个坑时，把定制逻辑抽象成平台通用组件。","Palantir 内部方法论"]],"sources":[["Airbus + Palantir — palantir.com/impact/airbus","https://www.palantir.com/impact/airbus/","厂商官方（一手，具名）"],["Palantir Supply Chain Solutions","https://palantir.com/offerings/supply-chain","厂商官方（一手）"],["Airbus Skywise 官方宣传册（2019）","https://aircraft.airbus.com/sites/g/files/jlcbta126/files/6baf9195385c253a1553f6e2a656f92b_Skywise-brochure-2019.pdf","客户官方（一手）"],["Airbus 整合数字业务单元（2026-04）","https://aeronauticsmagazine.com/news/civil-aviation/one-platform-to-rule-them-all-airbus-consolidates-digital-units-in-major-aviation-tech-overhaul","行业媒体（二手）"]]}',
    NULL,
    NULL,
    4002,
    1,
    0
  )
ON DUPLICATE KEY UPDATE
    code = VALUES(code),
    title = VALUES(title),
    subtitle = VALUES(subtitle),
    industry = VALUES(industry),
    func = VALUES(func),
    occur_year = VALUES(occur_year),
    partner = VALUES(partner),
    cred = VALUES(cred),
    cred_note = VALUES(cred_note),
    fde_mode = VALUES(fde_mode),
    scale = VALUES(scale),
    model_pct = VALUES(model_pct),
    pain = VALUES(pain),
    goal = VALUES(goal),
    verdict = VALUES(verdict),
    testimony = VALUES(testimony),
    content_json = VALUES(content_json),
    customer_id = VALUES(customer_id),
    published = VALUES(published),
    deleted = VALUES(deleted),
    update_time = CURRENT_TIMESTAMP;
