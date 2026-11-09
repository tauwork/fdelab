package com.fdelab.module.cases.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * content_json 的结构。
 * <p>前台只消费 tech / ai / mgmt / kpis / timeline / org / metrics / csf / quotes / sources；
 * kpis[].m 是二期编辑器的技术标注，这里原样保留、不参与渲染。</p>
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
