# 案例管理后台 · 编码提示词（独立任务书）

> 与 `docs/case-module-coding-prompt.md` 是两份独立文档，本文不替代、不修改它。
> 用法：整份交给编码 Agent，作为一次独立开发任务的输入。
> 权威顺序：`case-module-coding-prompt.md`（数据契约）> 本文（后台规格）> `fdelab-admin-web/src/views/talent/TalentView.vue`（UI 模板）。冲突时按此顺序裁决，并在交付说明里记录。

---

## 0. 任务

给已上线的「标杆案例库」补一套管理后台：录入、编辑、发布案例。

现状：前台 `fdelab-site-web` 的 `/articles` 与 `/articles/:id` 已只读上线；后端 `com.fdelab.module.cases` 已有实体、`CaseContentCodec`、`CaseAssembler` 和公开接口。本次只加**后台写入路径**，不改前台、不改表结构。

技术栈：Spring Boot 3 + MyBatis-Plus + Lombok + Sa-Token；Vue 3 + vue-router + axios + Element Plus。

必读：`docs/case-module-coding-prompt.md`、`docs/case-module-impl-notes.md`、`module/cases/**`、`module/talent/**`、`admin-web` 的 `TalentView.vue` / `api/talent.js` / `api/http.js` / `router/index.js` / `layouts/AdminLayout.vue`、`sql/fde-case.sql`。

---

## 1. 已定决策（不要重新讨论）

| 决策点 | 结论 |
|---|---|
| 编辑页 | 路由级全屏页，单页长表单 + 左侧锚点导航；不用弹窗、不用分步向导 |
| 发布权限 | 仅 ADMIN 可发布。本期不做后端角色校验，前端按 `roleCode === 'ADMIN'` 隐藏发布按钮，后端留 TODO |
| 客户 | 案例内选择 + 内联新建，不做独立客户页 |
| 录入辅助 | 复制现有案例、指标表 TSV 粘贴 |
| JSON 入口 | 不做可编辑的原始 JSON；不做导入导出 |
| 预览 | 只做右侧列表卡片实时预览，不复刻详情页 |
| MCP | 本期不做，但 DTO 形状必须可被后续复用 |

---

## 2. 范围

**做**：`/api/admin/cases` 的 CRUD + 复制 + 发布/下架 + 校验 + meta；`/api/admin/customers` 搜索/新建；后台 `/cases`、`/cases/new`、`/cases/:id/edit` 三个路由与菜单；列表页；9 段编辑页。

**不做**：不改表结构、不加列；不改 `fdelab-site-web`；不改 `/api/public/**`；不改 `sql/*.sql`、`docs/seed*`；不做版本历史、审批流、产品关联、批量导入；不做角色鉴权代码；不动 miniapp。

---

## 3. 数据契约（复用现有结构）

### 3.1 落位

| 表单字段 | 落库 |
|---|---|
| code、title、subtitle、industry、func、occurYear、partner、cred、credNote、fdeMode、scale、modelPct | `fde_case` 同名列 |
| pain、goal | `fde_case.pain` / `goal` |
| customerId | `customer_id` |
| primaryFdeId、contributorIds | `primary_fde_id` / `contributor_ids`（JSON 数字数组） |
| tech、ai、mgmt、kpis、timeline、org、metrics、csf、quotes、sources | 合并进 `content_json`，由 `CaseContent` 整体序列化 |
| verdict | `fde_case.verdict`（JSON 字符串，对象） |
| testimony | `fde_case.testimony`（JSON 字符串，数组） |
| published | 1 发布 / 0 草稿 |

### 3.2 元组顺序（严格按序，不得调换）

```
timeline : [阶段, 说明]
org      : [角色, 配置/人数, 职责]
metrics  : [指标名, 口径定义, 基线, 结果, 时间窗, 指标级可信度A-D, (可选)来源序号]
csf      : [维度, 做法, 为什么重要, 缺失的后果]
quotes   : [原文, 出处]
sources  : [标题, URL, 来源类型]
testimony: [证言原文, 说话人/出处]
```

两条新约定，写进类注释：

1. **`metrics` 第 7 位 = 来源序号**（1-based，指向 `sources` 下标）。前台只读 `row[0..5]`，追加第 7 位不影响渲染；不填就只存 6 位。
2. **`kpis[].m` = 本案例 `metrics` 的数组下标**（0-based）。前台不渲染，但后台必须维护正确（见 §5.3）。

### 3.3 verdict 固定键

`deposit` 能力沉淀在哪一侧 / `modelPct` 模型含量估算（叙述文本，不是 DECIMAL 列）/ `reusable` 可复用资产与产品化率 / `economics` 单位经济学走向 / `survive` 会被下一代模型冲掉吗 / `answer` 一句话结论。

后台按这 6 个键渲染定长表单，不允许增删键。

### 3.4 `_review` 预留

`content_json` 里可存在 `_review` 键（段落状态），`CaseContent` 已忽略未知字段。**本期只在保存时原样透传，不生成、不修改、不展示。**

---

## 4. 后端

### 4.1 包结构（命名空间已预留）

```
com.fdelab.module.cases.admin
├── controller/CaseAdminController.java
├── controller/CustomerAdminController.java
├── dto/CaseForm.java
├── dto/CustomerCreateRequest.java
├── service/CaseAdminService.java
├── service/CustomerAdminService.java
├── vo/CaseAdminVO.java
├── vo/CaseAdminListItemVO.java
├── vo/CaseMetaVO.java
└── vo/CaseValidationVO.java
```

### 4.2 `CaseForm`（前后端唯一形状）

```java
public record CaseForm(
    String code, String title, String subtitle, String industry, String func,
    String occurYear, String partner, String cred, String credNote,
    String fdeMode, String scale, BigDecimal modelPct,
    Long customerId, CustomerInput newCustomer,
    String pain, String goal,
    List<String> tech, List<String> ai, List<String> mgmt,
    List<KpiRow> kpis, List<TimelineRow> timeline, List<OrgRow> org,
    List<MetricRow> metrics, List<CsfRow> csf, List<TestimonyRow> testimony,
    List<QuoteRow> quotes, List<SourceRow> sources,
    VerdictInput verdict,
    Long primaryFdeId, List<Long> contributorIds, Integer published
) {
    record CustomerInput(String displayName, Integer isMasked) {}
    record KpiRow(String v, String k, Boolean r, Integer m) {}
    record TimelineRow(String stage, String note) {}
    record OrgRow(String role, String headcount, String duty) {}
    record MetricRow(String name, String definition, String baseline, String result,
                     String window, String cred, Integer sourceRef) {}
    record CsfRow(String dimension, String practice, String reason, String consequence) {}
    record TestimonyRow(String quote, String attribution) {}
    record QuoteRow(String text, String source) {}
    record SourceRow(String title, String url, String type) {}
    record VerdictInput(String deposit, String modelPct, String reusable,
                        String economics, String survive, String answer) {}
}
```

列表字段为 null 时按空数组处理；前端永远拿不到元组数组。

### 4.3 接口

统一前缀 `/api/admin`，返回 `ApiResult<T>`，异常用 `BizException(code, message)`。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/cases` | `page&size&q&industry&cred&published` → `PageData<CaseAdminListItemVO>` |
| GET | `/cases/{id}` | `CaseAdminVO` |
| POST | `/cases` | 建草稿 |
| PUT | `/cases/{id}` | 保存 |
| DELETE | `/cases/{id}` | 软删 |
| POST | `/cases/{id}/clone` | 复制为新草稿 |
| POST | `/cases/{id}/publish` | 校验后置 1 |
| POST | `/cases/{id}/unpublish` | 置 0 |
| POST | `/cases/validate` | 返回校验结果，不落库 |
| GET | `/cases/meta` | 行业、职能、标签词表、下一个编号、cred 选项 |
| GET | `/customers` | `keyword` 模糊搜索 |
| POST | `/customers` | `{displayName, isMasked}` |

- `CaseAdminVO = { id, form, customerName, customerMasked, createTime, updateTime }`。前端 API 层扁平化成 `{...form, id}`，页面只认扁平字段。
- `CaseMetaVO = { nextCode, industries[], funcs[], tags{tech,ai,mgmt}, creds[{code,label,hint}] }`。词表从**全部未删除案例**（含草稿）聚合，行业按出现次数降序。
- 列表项 = `{ id, code, title, industry, customerName, cred, published, metricCount, updateTime }`。

### 4.4 业务规则

1. 新建时 `code` 为空则生成 `CASE-` + 三位补零（现有 001/002，下一条 003）。`DuplicateKeyException` 重试一次，仍失败抛「编号冲突，请重试」。
2. `PUT` 忽略请求里的 `code`，一律用库里的值。
3. `POST` 一律写 `published = 0`，忽略请求值。
4. `PUT` 不改 `published`。
5. 发布：先跑 §6 校验，有 error 抛 `BizException(422, "错误1；错误2")`，不落库。
6. 复制：拷贝除 id/code/时间戳外全部字段，`title` 追加「（副本）」，`published = 0`，重生成 code，`customer_id` 保留。
7. 客户内联新建：`newCustomer` 非空先建并回填 `customerId`；同名（完全相等且未删除）直接复用；code 生成 `CUST-` + 三位补零。
8. 序列化只走 `CaseContentCodec.writeContent` / `writeJson`，禁止手拼 JSON。

---

## 5. 前端

### 5.1 路由与菜单

```
/cases            列表
/cases/new        新建
/cases/:id/edit   编辑
```

`AdminLayout` 菜单加「案例」（放在「人才」之后），图标用 `Files` 或 `Document`。

### 5.2 列表页

搜索框（标题/交付方）+ 行业下拉 + 可信度 chips + 状态下拉（全部/已发布/草稿）+ 新建按钮。表格列：编号、标题、客户、行业、可信度、指标数、状态、更新时间、操作（编辑 / 预览 / 复制 / 上下架 / 删除）。默认按更新时间倒序。分页沿用 `el-pagination` 的 `total, prev, pager, next`。

- 「复制」调 `/clone` 后跳转到新草稿的编辑页。
- 「预览」新窗口打开站点 `http://localhost:5174/articles/{code}`，仅已发布可点。
- 删除前 `ElMessageBox.confirm`。

### 5.3 编辑页

**布局**：顶部 sticky 操作条（左：编号 + 保存状态；右：预览 / 保存草稿 / 发布）；左侧 200px 锚点导航（9 项，圆点三态：已填 / 有软提示 / 有阻断）；中间表单；右侧 280px 卡片预览（`position: sticky`）。窄屏（<1280px）隐藏右侧预览。

**9 个分组**（顺序即阅读顺序，锚点 id 与校验 `section` 字段一致）：

| id | 分组 | 控件要点 |
|---|---|---|
| basic | 基本信息 | 编号（只读）、标题、副标题、客户选择器、交付方、行业（可输入下拉）、职能、年份、规模、FDE 模式、模型含量（数字 + 0–100 校验，保留一位小数） |
| credibility | 可信度与标签 | cred 分段选择 A–D + 说明；credNote 拆「主标题」「副标题」两个输入，保存拼 `主（副）`，回显用全角括号拆分；tech/ai/mgmt 用多选可新建标签 |
| narrative | 痛点与目标 | 两个 textarea，显示字数 |
| timeline | 推进路径 | 卡行 `[阶段, 说明]`，可增删拖拽 |
| org | 双方分工 | 卡行 `[角色, 配置, 职责]` |
| effects | 业务效果 | 指标表 + KPI 卡 + 证言卡行，见下 |
| csf | 关键成功要素 | 卡行 `[维度, 做法, 原因, 缺失后果]` |
| verdict | 能力归属检验 | 5 个定长输入 + 结论 textarea |
| sources | 引用与来源 | quotes 卡行 `[原文, 出处]` + sources 卡行 `[标题, URL, 类型]` |

**业务效果区三个必须做对的交互：**

1. **指标表**：7 列内联编辑（指标名、口径、基线、结果、时间窗、可信度 select A–D、来源序号 select）。行可上移/下移/删除。支持「从 Excel 粘贴」：弹窗 textarea，解析 TSV，六列映射；首行若为「指标名/指标」等表头则跳过；列数不足补空串，多余列忽略。
2. **KPI 卡**：每张卡 `v` 短值 / `k` 指标名 / `r` 红标开关 / `m` 关联指标下拉（选项显示 `#序号 指标名`，可选「不关联」）。提供「从指标行生成」：选中一条指标后自动带出 `v`（结果列）、`k`（指标名）、`m`（下标），只让人决定红标。
3. **下标重映射**：指标行**删除或排序后**，必须同步重算所有 `kpis[].m`：原下标等于被删行的 KPI 置空；大于被删行的减一；排序时按旧下标到新下标的映射改写。这是本次最容易漏的 bug，必须写单测或用控制台断言验证。

**状态与保存**：

- 新建先进 `/cases/new`，表单为空；首次保存成功后 `router.replace` 到 `/cases/{id}/edit`。
- 已有 id 时每 60 秒静默保存一次；本地每 10 秒把 diff 存 `localStorage`，异常刷新时提示恢复。新建未保存前只做本地暂存。
- 保存成功更新顶部「保存于 HH:mm」；失败保留表单并 toast。
- 发布按钮：`roleCode !== 'ADMIN'` 时禁用并提示「仅管理员可发布」；点击先调 `/validate`，有 error 时按 `section` 高亮左侧圆点并滚动到第一处，同时 `ElMessage.error` 首条。

**右侧预览**：按 `CaseListItemVO` 的卡片结构渲染（方块、标题、可信度、副标题、meta、痛点两行、行业 chip、KPI、底栏指标数），字段变化即时更新。不做详情页复刻。

---

## 6. 校验规则（前端与后端一致）

**阻断（error）**

| section | 规则 |
|---|---|
| basic | title 非空且 ≤200；industry 非空；customerId 或 newCustomer.displayName 至少一项 |
| basic | modelPct 在 0–100，最多一位小数 |
| credibility | cred ∈ A/B/C/D |
| narrative | pain、goal 非空 |
| effects | metrics 至少 1 条；每条 name 非空；每条 cred ∈ A/B/C/D |
| effects | sourceRef 若填，必须落在 1..sources.length |
| sources | sources 至少 1 条，且 url 以 http:// 或 https:// 开头 |
| sources | cred 为 A 或 B 时，testimony 至少 1 条非空，**或** sources 中存在类型含「一手」「官方」的条目 |
| basic | code 唯一（仅新建） |

**软提示（warning，不阻断）**

- 指标缺基线或时间窗
- KPI 未关联指标行
- 标题超过 40 字（列表卡片会截断）
- 来源域名与交付方明显不匹配

返回结构：`{ ok, errors: [{section, field, message}], warnings: [...] }`。发布时把 error 的 message 用「；」拼接抛 422。

---

## 7. 禁止事项

- 不得修改 `fde_case` / `fde_customer` 表结构，不得加列
- 不得改前台 `fdelab-site-web` 与 `/api/public/**`
- 不得修改 `docs/case-module-coding-prompt.md`、`docs/seed*`、`sql/*.sql`
- 不得在 Service 里手拼 JSON 字符串，一律走 `CaseContentCodec`
- 不得把雪花 `id` 暴露在前台可见位置；后台内部用 id 可以
- 不得给编辑页做成弹窗或分步向导
- 不得新增角色鉴权代码（本期不做），只在发布按钮做前端隐藏 + 后端 TODO

---

## 8. 验收

**后端**（先登录拿 token：`POST /api/auth/login {"username":"admin","password":"admin123"}`，后续带 `Authorization: Bearer <token>`）

1. `POST /api/admin/cases` 传最小字段 → 返回 `code=CASE-003`、`published=0`
2. `GET /api/admin/cases?published=0` → 能查到这条，`metricCount=0`
3. `POST /api/admin/cases/{id}/publish` → 返回 422 且 message 含「指标」或「来源」
4. 补齐指标 1 条 + 来源 1 条 + 痛点目标 + 可信度 → 再发布 → 成功
5. `GET /api/public/cases/CASE-003` → 前台可读，元组顺序正确
6. `POST /api/admin/cases/{id}/clone` → 新 code，title 带「（副本）」，`published=0`
7. `DELETE` → 列表不再返回，`GET /api/public/cases/CASE-003` 返回 404

**前端**

1. `npm run build` 通过；`/cases` 列表能筛出草稿
2. 编辑页 9 个锚点可跳转，圆点随校验变化
3. 指标表粘贴 3 行 TSV 能正确入表；删掉第 1 行后，KPI 的 `m` 从 1 变 0
4. credNote 填「A 级（客户具名）」保存后，编辑页回显两个框分别是「A 级」「客户具名」
5. 非 ADMIN 账号看不到可点的发布按钮
6. 右侧卡片随标题、KPI、行业实时变化

---

## 9. 交付物

新增（后端）：§4.1 列出的 10 个文件。
新增（前端）：`src/api/case.js`、`src/api/customer.js`、`src/views/case/CaseListView.vue`、`src/views/case/CaseEditView.vue`、`src/views/case/components/`（指标表、KPI 编辑器、粘贴弹窗、卡行组件）。
修改：`src/router/index.js`、`src/layouts/AdminLayout.vue`。
交付说明里写明：未改表结构、未改前台、未做鉴权，以及 §6 校验规则的实际落地位置。
