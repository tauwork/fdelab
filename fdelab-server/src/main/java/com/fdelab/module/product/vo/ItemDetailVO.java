package com.fdelab.module.product.vo;

import java.util.Map;

/**
 * 条目详情：真列 + detail 原样透传。
 * <p>detail 里的键不展开成 VO 字段——条目详情页尚未设计，键名由设计定稿。
 * 前端占位页不渲染 detail。</p>
 */
public record ItemDetailVO(
        String code,
        String kind,
        String kindLabel,
        String status,
        String statusLabel,
        String name,
        String summary,
        String cycle,
        String pricing,
        ProductRefVO product,
        Map<String, Object> detail
) {

    public record ProductRefVO(
            String code,
            String name
    ) {
    }
}
