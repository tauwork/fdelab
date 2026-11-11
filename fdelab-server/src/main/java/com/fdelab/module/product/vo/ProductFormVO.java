package com.fdelab.module.product.vo;

/**
 * 服务页运作卡（列表项）。条目计数由前端从条目接口自行统计，不在运作接口返回。
 */
public record ProductFormVO(
        String code,
        String no,
        String name,
        String label,
        String desc,
        String fit,
        String cycle,
        String pricing
) {
}
