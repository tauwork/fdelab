package com.fdelab.module.talent.vo;

import java.util.List;

/**
 * 人才详情页「代表案例」卡片。
 * <p>title 已按「标题：副标题」组装，delivery 已合并红标 KPI 与案例结论，
 * 前端只做渲染，不再拼业务文案。</p>
 */
public record TalentCaseCardVO(
        Long id,
        String code,
        String title,
        String subtitle,
        String industry,
        String pain,
        List<String> points,
        String delivery,
        String cred,
        String credNote,
        String url
) {
}
