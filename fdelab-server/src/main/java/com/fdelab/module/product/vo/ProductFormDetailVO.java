package com.fdelab.module.product.vo;

import java.util.List;

/**
 * 运作详情 = 列表项 + 交付物 / 成功判据 / 参照案例。
 */
public record ProductFormDetailVO(
        String code,
        String no,
        String name,
        String label,
        String desc,
        String fit,
        String cycle,
        String pricing,
        List<String> deliverables,
        String success,
        List<CaseRefVO> caseRefs
) {
}
