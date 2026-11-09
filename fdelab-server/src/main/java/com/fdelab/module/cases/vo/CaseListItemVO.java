package com.fdelab.module.cases.vo;

import com.fdelab.module.cases.model.CaseContent;

import java.math.BigDecimal;
import java.util.List;

/**
 * 列表卡片 / 表格行。
 * <p>id 仅供内部使用，前端不得展示；对外标识一律用 code。</p>
 */
public record CaseListItemVO(
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
        /** 卡片 bio 原文（两行截断），任务书 §5 第 4 项；与详情 VO 同源同字段，不可省略 */
        String pain
) {
}
