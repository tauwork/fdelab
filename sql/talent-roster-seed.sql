USE fdelab;

-- ============================================================
-- FDE 名录 4 位初始数据（以线上 tauwork.cn/fdelab/assets/app.js 的 FDES 为准）
-- slug 与 TalentSlugs.fromName() 输出一致：shengong / lingong / chengong / zhougong
-- 可重复执行；已存在同 slug 时跳过。
-- ============================================================

INSERT INTO fde_talent (
    id, name, slug, headline, summary, level_code, years, case_count,
    like_count, published, profile_json, deleted
)
SELECT 203,
       '沈工',
       'shengong',
       '半导体 / 芯片验证方向 · 首席 FDE',
       '前 EDA 公司验证架构师。 CASE-001 同类场景（芯片验证 AI 原生化）的现场负责人，擅长「无 ground truth」场景下的评测体系冷启动：先与客户工程师共建 Eval，再谈能力建设。名言：交付结束时，客户手里留下的应该是 Eval，不是代码。',
       'M',
       12,
       2,
       0,
       1,
       JSON_OBJECT(
           'badges', JSON_ARRAY(),
           'identityTags', JSON_ARRAY(),
           'industryTags', JSON_ARRAY('半导体/集成电路', '电子制造'),
           'domains', JSON_ARRAY(),
           'roles', JSON_ARRAY(),
           'experiences', JSON_ARRAY(),
           'services', JSON_ARRAY(),
           'collaborations', JSON_ARRAY(),
           'cases', JSON_ARRAY(
               JSON_OBJECT(
                   'title', '某大型半导体客户 · 芯片验证 AI 原生化',
                   'role', '首席 FDE',
                   'delivery', '10+ 用例 3–6 个月全部上生产；定向测试生成省约 48% 时间'
               ),
               JSON_OBJECT(
                   'title', '某晶圆厂 · 良率异常根因分析',
                   'role', 'FDE 负责人',
                   'delivery', '失效分析周期从数周压缩到数小时'
               )
           ),
           'skills', JSON_ARRAY('验证器/评测设计', 'Agent 编排', '芯片领域知识', 'Eval 冷启动', '客户 CoE 交接'),
           'radar', JSON_ARRAY(9, 8, 10, 8, 9)
       ),
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_talent WHERE slug = 'shengong');

INSERT INTO fde_talent (
    id, name, slug, headline, summary, level_code, years, case_count,
    like_count, published, profile_json, deleted
)
SELECT 204,
       '林工',
       'lingong',
       '制造 / 供应链方向 · 资深 FDE',
       '前航空 MRO 数字化负责人，参与过 CASE-002 同类场景（本体论建模 + 预测性维护）。擅长复杂装备制造业的「先纵向打通一条链路」打法：第一仗必须打在能算清账的完整业务链路上。',
       'P',
       10,
       2,
       0,
       1,
       JSON_OBJECT(
           'badges', JSON_ARRAY(),
           'identityTags', JSON_ARRAY(),
           'industryTags', JSON_ARRAY('制造业', '航空航天', '能源装备'),
           'domains', JSON_ARRAY(),
           'roles', JSON_ARRAY(),
           'experiences', JSON_ARRAY(),
           'services', JSON_ARRAY(),
           'collaborations', JSON_ARRAY(),
           'cases', JSON_ARRAY(
               JSON_OBJECT(
                   'title', '某制造集团 · 产能爬坡数据打通',
                   'role', '驻场 FDE',
                   'delivery', '对标 Airbus 三步法：先理流程、再整数据、最后自动化'
               ),
               JSON_OBJECT(
                   'title', '某能源装备企业 · 预测性维护',
                   'role', 'FDE 负责人',
                   'delivery', '对标 Skywise：本体建模 + 故障模式库 + 提前预警'
               )
           ),
           'skills', JSON_ARRAY('本体论建模', '数据管道', '预测性维护', '现场推进', '多方数据治理'),
           'radar', JSON_ARRAY(8, 9, 7, 8, 9)
       ),
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_talent WHERE slug = 'lingong');

INSERT INTO fde_talent (
    id, name, slug, headline, summary, level_code, years, case_count,
    like_count, published, profile_json, deleted
)
SELECT 205,
       '陈工',
       'chengong',
       '金融风控方向 · 资深 FDE',
       '前银行风控模型验证负责人。金融是「反馈延迟、信号稀疏」的高难度场景，专长是把强监管行业的合规要求转译成机器可执行的验证标准——恰好是「验证器极难定义」场景的方法论担当。',
       'P',
       8,
       2,
       0,
       1,
       JSON_OBJECT(
           'badges', JSON_ARRAY(),
           'identityTags', JSON_ARRAY(),
           'industryTags', JSON_ARRAY('金融保险', '专业服务'),
           'domains', JSON_ARRAY(),
           'roles', JSON_ARRAY(),
           'experiences', JSON_ARRAY(),
           'services', JSON_ARRAY(),
           'collaborations', JSON_ARRAY(),
           'cases', JSON_ARRAY(
               JSON_OBJECT(
                   'title', '某银行 · 风控规则引擎 AI 化',
                   'role', 'FDE',
                   'delivery', '规则可解释性与合规审计并行设计'
               ),
               JSON_OBJECT(
                   'title', '某保险公司 · 理赔文档智能审核',
                   'role', '评测负责人',
                   'delivery', '人工审核比例与准确率的平衡设计'
               )
           ),
           'skills', JSON_ARRAY('合规与验证器', '文档理解', '风控领域知识', '审计留痕设计'),
           'radar', JSON_ARRAY(7, 9, 8, 7, 8)
       ),
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_talent WHERE slug = 'chengong');

INSERT INTO fde_talent (
    id, name, slug, headline, summary, level_code, years, case_count,
    like_count, published, profile_json, deleted
)
SELECT 206,
       '周工',
       'zhougong',
       '通才型 FDE · 部署工程师',
       '转型范式样本：入职时从未做过客户行业，靠「嵌入 + 与客户工程师共建 eval」补齐领域知识——对应 OpenAI FDE「通才 + 领域专家混编」里的通才位。执行 connector / MCP 集成层与新场景部署。',
       'A',
       3,
       1,
       0,
       1,
       JSON_OBJECT(
           'badges', JSON_ARRAY(),
           'identityTags', JSON_ARRAY(),
           'industryTags', JSON_ARRAY('跨行业（通才）'),
           'domains', JSON_ARRAY(),
           'roles', JSON_ARRAY(),
           'experiences', JSON_ARRAY(),
           'services', JSON_ARRAY(),
           'collaborations', JSON_ARRAY(),
           'cases', JSON_ARRAY(
               JSON_OBJECT(
                   'title', '跨场景 · 集成层部署',
                   'role', '部署工程师',
                   'delivery', '第二团队 1 天上线类任务：换 connector / MCP server + 新建 eval'
               )
           ),
           'skills', JSON_ARRAY('MCP/工具集成', '快速领域补学', '部署执行', 'Codex/Skills 运维'),
           'radar', JSON_ARRAY(7, 6, 6, 6, 8)
       ),
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_talent WHERE slug = 'zhougong');
