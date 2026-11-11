-- ============================================================
-- dev only，不用于生产环境
-- 产品与服务 · 调试造数（内容来源 demo/product-forms-v15.html，仅产品侧 6 条）
-- 依据：docs/product-service-impl-plan.md §6 / §7
-- 正式文案由运营在管理端录入，本文件只用于本地比对原型
-- 幂等：INSERT ... SELECT ... WHERE NOT EXISTS（按 code 判重）
-- ============================================================
USE fdelab;

-- 运作层：4 行（label 拆 form + weight，caseLink → case_ids 真实 id，查不到写 []）
INSERT INTO fde_product_form (id, code, name, form, weight, sort_order, fit, description, deliverables, cycle, pricing, success, case_ids, customer_ids, published, deleted)
SELECT 4301, 'F1', '方案 + 验证器（评测体系设计）', '产品运作 ①', '最轻 · 不改变交付模式', 1,
       '所有咨询机构 / 甲方，可立即试点',
       '交付方案时同时交付「如何衡量这个方案成功」的评测标准。把咨询从「卖判断」推向「卖可验证的判断」——第一步不必伤筋动骨：不改组织、不改收费。',
       JSON_ARRAY('业务场景评测标准（Eval 规格）', '「什么叫对」的验收判据集', '验证器定义文档（可版本化、可审计）', '团队能力内化路径说明'),
       '2–4 周', '按交付物计价（非人天）',
       '交付结束时，客户的业务判断已变成可复用的评测标准——而不是只多了一堆 prompt 和胶水代码。',
       COALESCE((SELECT JSON_ARRAY(id) FROM fde_case WHERE code = 'CASE-001' AND deleted = 0 LIMIT 1), JSON_ARRAY()),
       NULL, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_form WHERE code = 'F1');

INSERT INTO fde_product_form (id, code, name, form, weight, sort_order, fit, description, deliverables, cycle, pricing, success, case_ids, customer_ids, published, deleted)
SELECT 4302, 'F2', '诊断 + POC 加速', '产品运作 ②', '中等 · 需少量工程资源', 2,
       '有技术合作伙伴的机构 / 已启动 AI 试点的甲方',
       '咨询方做诊断，FDE 在 2–4 周内把 1–2 个最高价值场景做到生产可用。专选「有唯一裁判」的场景先行：芯片验证、代码、风控规则、合规文档、质检类。',
       JSON_ARRAY('1–2 个生产可用场景', '场景验证器 + eval 集', '根因分析报告（POC 失败原因诊断）', '规模化可行性判断'),
       '2–4 周', '固定范围 + 结果奖金',
       '跨过「POC 死亡谷」：不是又一个 Demo，而是跑在生产里、有指标可算的链路。',
       COALESCE((SELECT JSON_ARRAY(id) FROM fde_case WHERE code = 'CASE-001' AND deleted = 0 LIMIT 1), JSON_ARRAY()),
       NULL, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_form WHERE code = 'F2');

INSERT INTO fde_product_form (id, code, name, form, weight, sort_order, fit, description, deliverables, cycle, pricing, success, case_ids, customer_ids, published, deleted)
SELECT 4303, 'F3', '客户 AI CoE 共建', '产品运作 ③', '较重 · 周期 3–6 个月', 3,
       '有长期大客户关系的机构 / 集团型甲方',
       '帮客户建 AI 卓越中心（治理 + 评测 + 技能包分发），FDE 打通首批场景后移交。对标 OpenAI 案例的客户 CoE：集中管控 skills 分发与版本，让第二个团队 1 天上线。',
       JSON_ARRAY('CoE 组织与治理章程', '技能包（Skills）分发与版本机制', '抽象触发阈值设计（如 3 家客户规则）', '首批场景打通 + 移交培训'),
       '3–6 个月', '按里程碑 + 能力订阅',
       '换一个团队 / 场景重做，1 天而不是 2 个月；模型升级后，客户是受益而不是重做。',
       COALESCE((SELECT JSON_ARRAY(id) FROM fde_case WHERE code = 'CASE-002' AND deleted = 0 LIMIT 1), JSON_ARRAY()),
       NULL, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_form WHERE code = 'F3');

INSERT INTO fde_product_form (id, code, name, form, weight, sort_order, fit, description, deliverables, cycle, pricing, success, case_ids, customer_ids, published, deleted)
SELECT 4304, 'F4', 'FDE 人才训练与认证（CFDE）', '能力线', '不依赖项目 · 轻启动 · 持续性收入', 4,
       '咨询机构顾问转型 / 甲方工程团队升级',
       'TIIDA（国际数据与人工智能研究院）CFDE 三级认证体系：CFDE-A 助理 / CFDE-P 专业 / CFDE-M 大师。训练目标不是人人会写代码，而是有人能把业务判断写成评测标准、能判断模型的能力边界。',
       JSON_ARRAY('分级训练课程（A/P/M）', '认证考试与评级', '人才库入库与推荐', '「顾问 + FDE」混编团队设计咨询'),
       '4–12 周 / 人', '按人头认证 + 企业内训包',
       '组织里出现「工程化翻译」层：能把一号位的判断翻译成机器可执行、可验证的标准的人。',
       JSON_ARRAY(),
       NULL, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_form WHERE code = 'F4');

-- 条目层：6 行（v15 PRODUCT_IDS 全集；服务侧条目不落地，详见方案 §2）
-- detail 键名用方案 §2.3.1 候选名；evidence.tag 保留 v15 原文，不归类
INSERT INTO fde_product_item (id, code, product_id, kind, name, status, summary, cycle, pricing, detail, sort_order, published, deleted)
SELECT 4402, 'A2', (SELECT id FROM fde_product_form WHERE code = 'F1' LIMIT 1), 'm', '交付模板库 + 验证方法手册', 'on',
       '把交付时用的东西交给你：Eval 规格怎么写、判据集怎么建、验收单怎么签。哪一步不能省，都写明。',
       '1 周内开通', '年度授权，按机构规模分档。',
       JSON_OBJECT(
         'fit', '每次交付都从零起草，质量看人。换个人做，水准就掉一截。',
         'cycleNote', '授权后即开，含一次线上答疑。',
         'priceNote', '授权不等于可转授权，转授权另议。',
         'success', '你的顾问照模板交完一单，客户没再问「这东西怎么算成」。',
         'sign', '以首单交付物抽检通过为准。',
         'pit', '最容易踩的坑：只拿模板不派人受训。模板是给懂方法的人用的，不懂的人会抄错地方。',
         'deliverables', JSON_ARRAY('Eval 规格模板（含三类场景样例）', '判据集构建清单', '验收单据与双签流程模板', '常见失败模式手册'),
         'notdo', JSON_ARRAY('不授商标与品牌使用权', '不含转授权', '不承诺模板覆盖所有行业'),
         'evidence', JSON_OBJECT('tag', '方法论自持', 'kpis', JSON_ARRAY(JSON_ARRAY('四件套', '模板结构'), JSON_ARRAY('抽检', '验收方式')), 'note', '模板库随交付迭代，版本号可查。')
       ),
       2, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_item WHERE code = 'A2');

INSERT INTO fde_product_item (id, code, product_id, kind, name, status, summary, cycle, pricing, detail, sort_order, published, deleted)
SELECT 4404, 'B1', (SELECT id FROM fde_product_form WHERE code = 'F2' LIMIT 1), 'de', '验证工程师 · 数字员工', 'on',
       '干验证工程师的活：写定向测试、追失效。原来按周算，现在按小时算，人只审。',
       '2–4 周／场景', '按交付的东西算，不按人天。',
       JSON_OBJECT(
         'fit', '验证环节占掉大半工作量，资深工程师的经验散在少数人脑子里，没有棋盘也没有裁判。',
         'cycleNote', '自数据与权限就绪起算。',
         'priceNote', '范围先定死，范围外走变更。报价单上没有「人天」这两个字。',
         'success', '失效定位从数周压到数小时，基线还能复算。',
         'sign', '你的验证负责人出数核验，两边签字。',
         'pit', '最容易踩的坑：先扩场景再建验证器。评测先行，先定义「什么叫对」，才谈迭代。',
         'deliverables', JSON_ARRAY('一条能上生产的验证流程', '场景验证器 + 评测集', '人和它怎么分工的审核规程', '给第二个团队看的复用说明'),
         'notdo', JSON_ARRAY('不含验证工具链整体替换', '不含数据治理', '不接拒绝共建评测的场景'),
         'evidence', JSON_OBJECT('tag', '别人做的 · 公开案例', 'kpis', JSON_ARRAY(JSON_ARRAY('-48%', '定向测试生成耗时'), JSON_ARRAY('数小时', '失效定位（原为数周）')), 'note', '来源：OpenAI FDE × 大型半导体客户，证据等级 C。不是我们做的。'),
         'source', JSON_OBJECT('origin', '半导体验证场景那套', 'adapt', 'b', 'open', '2–4 周', 'runs', '3 个客户', 'note', '三个验证团队用过。判据集和审核流程按客户情况小改，底下的东西不动。你这边要改多少，看完流程才定。')
       ),
       4, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_item WHERE code = 'B1');

INSERT INTO fde_product_item (id, code, product_id, kind, name, status, summary, cycle, pricing, detail, sort_order, published, deleted)
SELECT 4405, 'B2', (SELECT id FROM fde_product_form WHERE code = 'F2' LIMIT 1), 'ag', '测试用例生成 Agent', 'on',
       '按你定好的「什么叫对」批量写用例。写完能查、能重跑。不是让模型自由发挥。',
       '2–3 周', '按交付的东西算。',
       JSON_OBJECT(
         'fit', '用例靠人写，覆盖靠运气，谁也说不清这轮测试算不算测到位了。',
         'cycleNote', '含与代码仓、CI 的对接。',
         'priceNote', '含一次部署与一轮调优，超出范围走变更。',
         'success', '每个用例都能追溯到它验证的标准条目。',
         'sign', '抽样审计通过率达标，两边签字。',
         'pit', '最容易踩的坑：没有验证器就先上生成。生成的用例越多，浪费越快。',
         'deliverables', JSON_ARRAY('Agent 部署包（接入你的代码仓）', '用例质量判据集', '生成结果审计日志', '使用内化说明'),
         'notdo', JSON_ARRAY('不含 CI/CD 平台改造', '不含测试管理工具替换', '不做无判据的「全自动测试」承诺'),
         'evidence', JSON_OBJECT('tag', '别人做的 · 公开案例', 'kpis', JSON_ARRAY(JSON_ARRAY('-48%', '用例生成耗时（标杆）')), 'note', '来源同 B1。我们的实收指标待客户授权。')
       ),
       5, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_item WHERE code = 'B2');

INSERT INTO fde_product_item (id, code, product_id, kind, name, status, summary, cycle, pricing, detail, sort_order, published, deleted)
SELECT 4406, 'B3', (SELECT id FROM fde_product_form WHERE code = 'F2' LIMIT 1), 'de', '合规文档审阅员 · 数字员工', 'beta',
       '拿你的条款库逐条对长文档。每条意见都带出处，人只核例外的那几处。',
       '4–6 周', '按里程碑收。',
       JSON_OBJECT(
         'fit', '合规文档审阅靠人海，看漏一条的代价是整个项目。',
         'cycleNote', '含条款库冷启动。',
         'priceNote', '条款库、部署、试运行三段分别验收。',
         'success', '意见能溯源到条款原文，例外项的漏检率有基线可比。',
         'sign', '抽样比对，结果过了再签。',
         'pit', '最容易踩的坑：条款库不整理就上线。它审得准不准，取决于你喂的条款有多干净。',
         'deliverables', JSON_ARRAY('条款库结构化整理', '审阅数字员工部署', '意见出处标注规范', '例外升级规程'),
         'notdo', JSON_ARRAY('不含法律意见，输出不构成法律建议', '不含条款库内容代写', '不做无人工复核的直发结论'),
         'evidence', JSON_OBJECT('tag', '内测中 · 还没数', 'kpis', JSON_ARRAY(JSON_ARRAY('2 个客户', '原场景验证')), 'note', '换行业后是首批移植，指标待实收。'),
         'source', JSON_OBJECT('origin', '金融合规审阅那套', 'adapt', 'b', 'open', '4–6 周', 'runs', '2 个客户', 'note', '在金融的条款审阅上跑过。换行业最麻烦的是条款库要从头理一遍，这也是它只算「小改」、不算「直接能用」的原因。')
       ),
       6, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_item WHERE code = 'B3');

INSERT INTO fde_product_item (id, code, product_id, kind, name, status, summary, cycle, pricing, detail, sort_order, published, deleted)
SELECT 4407, 'B4', (SELECT id FROM fde_product_form WHERE code = 'F2' LIMIT 1), 'de', '设备巡检报告员 · 数字员工', 'beta',
       '按你的巡检标准出报告，拿不准的转给人。每条结论都能翻回是哪条标准判的。',
       '6–8 周', '按里程碑收。',
       JSON_OBJECT(
         'fit', '巡检靠老师傅走线，报告靠手抄，异常判读标准因人而异。',
         'cycleNote', '含判据重建与数据对接的等待期。',
         'priceNote', '判据、部署、试运行三段分别验收。',
         'success', '同一批样本，它和资深工程师的判读一致率达标，报告逐条可溯源。',
         'sign', '试运行对比报告出来，一致率达标再签。',
         'pit', '最容易踩的坑：把原行业的判据直接搬过来用。巡检判据是场景知识，不是通用模板。',
         'deliverables', JSON_ARRAY('巡检判据集共建', '巡检数字员工部署', '异常升级与人工确认规程', '试运行对比报告'),
         'notdo', JSON_ARRAY('不含传感器补装', '不含巡检工单系统替换', '不做无抽检机制的自动放行'),
         'evidence', JSON_OBJECT('tag', '内测中 · 还没数', 'kpis', JSON_ARRAY(JSON_ARRAY('2 个客户', '原场景验证')), 'note', '产线场景是我方首批移植，指标待实收。'),
         'source', JSON_OBJECT('origin', '装备运维巡检那套', 'adapt', 'c', 'open', '6–8 周', 'runs', '2 个客户', 'note', '供应商管它叫「巡检 Agent」，我们按数字员工摆。原先跑在装备运维，挪到产线得把巡检判据重做一遍，所以标「得重做」。多久能接上，看判据什么时候定完。')
       ),
       7, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_item WHERE code = 'B4');

INSERT INTO fde_product_item (id, code, product_id, kind, name, status, summary, cycle, pricing, detail, sort_order, published, deleted)
SELECT 4408, 'B5', (SELECT id FROM fde_product_form WHERE code = 'F2' LIMIT 1), 'sa', '经营周报生成台 · SaaS', 'on',
       '把散在各系统里的数拼成一张周报。每个数字都能查回它从哪张表来。',
       '1–2 周', '按交付的东西算，可选年度订阅。',
       JSON_OBJECT(
         'fit', '周报靠人拼 Excel，口径每部门一套，数字对不上没人说得清。',
         'cycleNote', '自数据源权限就绪起算。',
         'priceNote', '订阅按周期固定金额，不随用量浮动。',
         'success', '周报口径全公司唯一，任何数字可回溯到数据源与生成时间。',
         'sign', '当场对一遍口径，对得上就签。',
         'pit', '最容易踩的坑：先接数据再定口径。口径没统一，它只是把各套口径生成得更快。',
         'deliverables', JSON_ARRAY('数据源对接与口径配置', '周报模板与生成任务', '版本对比与溯源机制', '使用移交说明'),
         'notdo', JSON_ARRAY('不含数据仓库建设', '不含 BI 平台替换', '不做无口径确认的自动发布'),
         'evidence', JSON_OBJECT('tag', '移植来的 · 有实收', 'kpis', JSON_ARRAY(JSON_ARRAY('5 个客户', '原场景验证'), JSON_ARRAY('唯一口径', '可回溯')), 'note', '移植验证记录由供应商盘点表核验后更新。'),
         'source', JSON_OBJECT('origin', '集团经营分析那套', 'adapt', 'a', 'open', '1–2 周', 'runs', '5 个客户', 'note', '供应商管它叫「报表 Agent」，其实没人给它签字，就是个软件。模板改改就能用，接数据源是唯一要动手的地方。')
       ),
       8, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fde_product_item WHERE code = 'B5');
