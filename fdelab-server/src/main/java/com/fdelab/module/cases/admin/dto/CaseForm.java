package com.fdelab.module.cases.admin.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 案例管理后台的前后端唯一表单形状。
 *
 * <p>元组顺序（严格按序，不得调换）：</p>
 * <pre>
 * timeline : [阶段, 说明]
 * org      : [角色, 配置/人数, 职责]
 * metrics  : [指标名, 口径定义, 基线, 结果, 时间窗, 指标级可信度A-D, (可选)来源序号]
 * csf      : [维度, 做法, 为什么重要, 缺失的后果]
 * quotes   : [原文, 出处]
 * sources  : [标题, URL, 来源类型]
 * testimony: [证言原文, 说话人/出处]
 * </pre>
 *
 * <p>两条约定：</p>
 * <ol>
 *   <li>metrics 第 7 位为来源序号（1-based，指向 sources 下标）；前台只读 row[0..5]，
 *       不填时只存 6 位。</li>
 *   <li>kpis[].m 为本案例 metrics 的数组下标（0-based）；前台不渲染，后台必须维护正确。</li>
 * </ol>
 *
 * <p>列表字段为 null 时按空数组处理；前端永远拿不到元组数组。</p>
 */
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

    public record CustomerInput(String displayName, Integer isMasked) {
    }

    public record KpiRow(String v, String k, Boolean r, Integer m) {
    }

    public record TimelineRow(String stage, String note) {
    }

    public record OrgRow(String role, String headcount, String duty) {
    }

    public record MetricRow(String name, String definition, String baseline, String result,
                            String window, String cred, Integer sourceRef) {
    }

    public record CsfRow(String dimension, String practice, String reason, String consequence) {
    }

    public record TestimonyRow(String quote, String attribution) {
    }

    public record QuoteRow(String text, String source) {
    }

    public record SourceRow(String title, String url, String type) {
    }

    public record VerdictInput(String deposit, String modelPct, String reusable,
                               String economics, String survive, String answer) {
    }
}
