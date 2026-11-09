package com.fdelab.module.cases.vo;

import com.fdelab.module.cases.model.CaseContent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 案例详情：列表字段 + 正文区段 + 关联解析。
 */
public record CaseDetailVO(
        Long id,
        String code,
        String title,
        String subtitle,
        String industry,
        String func,
        String occurYear,
        String partner,
        String clientName,
        String cred,
        String credNote,
        String fdeMode,
        String scale,
        BigDecimal modelPct,
        List<String> tech,
        List<String> ai,
        List<String> mgmt,
        List<CaseContent.Kpi> kpis,
        int metricCount,
        Integer published,
        String pain,
        String goal,
        Map<String, Object> verdict,
        List<List<String>> testimony,
        List<List<String>> timeline,
        List<List<String>> org,
        List<List<String>> metrics,
        List<List<String>> csf,
        List<List<String>> quotes,
        List<List<String>> sources,
        CustomerVO customer,
        FdeRefVO primaryFde,
        List<Object> products
) {

    public record CustomerVO(
            Long id,
            String displayName,
            Integer isMasked
    ) {
    }

    public record FdeRefVO(
            String name,
            String slug,
            String levelCode
    ) {
    }
}
