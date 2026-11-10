package com.fdelab.module.cases.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * content_json 的结构。
 * <p>前台只消费 tech / ai / mgmt / kpis / timeline / org / metrics / csf / quotes / sources；
 * kpis[].m 是二期编辑器的技术标注，这里原样保留、不参与渲染。</p>
 * <p>元组顺序（严格按序，不得调换）：</p>
 * <pre>
 * timeline : [阶段, 说明]
 * org      : [角色, 配置/人数, 职责]
 * metrics  : [指标名, 口径定义, 基线, 结果, 时间窗, 指标级可信度A-D, (可选)来源序号]
 * csf      : [维度, 做法, 为什么重要, 缺失的后果]
 * quotes   : [原文, 出处]
 * sources  : [标题, URL, 来源类型]
 * </pre>
 * <p>两条后台约定：</p>
 * <ol>
 *   <li>metrics 第 7 位为来源序号（1-based，指向 sources 下标）；前台只读 row[0..5]，
 *       不填时只存 6 位。</li>
 *   <li>kpis[].m 为本案例 metrics 的数组下标（0-based）；前台不渲染，后台必须维护正确。</li>
 * </ol>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseContent {

    private List<String> tech;
    private List<String> ai;
    private List<String> mgmt;
    private List<Kpi> kpis;
    private List<List<String>> timeline;
    private List<List<String>> org;
    private List<List<String>> metrics;
    private List<List<String>> csf;
    private List<List<String>> quotes;
    private List<List<String>> sources;

    /**
     * 段落状态预留键。后台本期只在保存时原样透传，不生成、不修改、不展示。
     */
    @JsonProperty("_review")
    private Object review;

    /** 把 JSON 里缺失或显式 null 的字段归一成空数组，避免 VO 层空指针。 */
    public void normalize() {
        tech = safeList(tech);
        ai = safeList(ai);
        mgmt = safeList(mgmt);
        kpis = safeList(kpis);
        timeline = safeTuples(timeline);
        org = safeTuples(org);
        metrics = safeTuples(metrics);
        csf = safeTuples(csf);
        quotes = safeTuples(quotes);
        sources = safeTuples(sources);
    }

    private static <T> List<T> safeList(List<T> source) {
        return source == null ? new ArrayList<>() : source;
    }

    private static List<List<String>> safeTuples(List<List<String>> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        List<List<String>> result = new ArrayList<>(source.size());
        for (List<String> row : source) {
            result.add(row == null ? new ArrayList<>() : row);
        }
        return result;
    }

    /**
     * 卡片 KPI：v 短值 / k 短指标名 / r 原文红标 / m 绑定的指标行索引。
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Kpi {
        private String v;
        private String k;
        private Boolean r;
        private Integer m;
    }
}
