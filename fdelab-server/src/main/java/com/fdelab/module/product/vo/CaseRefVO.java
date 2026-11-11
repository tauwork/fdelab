package com.fdelab.module.product.vo;

import java.util.List;

/**
 * 参照案例卡：由 fde_product_form.case_ids 解析 fde_case 得到，不复制案例内容。
 * <p>case_ids 为空时返回空数组，禁止用第一条顶替。</p>
 */
public record CaseRefVO(
        Long id,
        String code,
        String title,
        String subtitle,
        String cred,
        List<KpiVO> kpis
) {

    /** hard 对应案例 content_json 里 kpis[].r（原文红标） */
    public record KpiVO(
            String v,
            String k,
            boolean hard
    ) {
    }
}
