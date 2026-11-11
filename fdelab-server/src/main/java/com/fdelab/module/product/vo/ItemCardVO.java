package com.fdelab.module.product.vo;

/**
 * 条目列表卡片：只取自真列，source 取自 detail.source（无该键时省略）。
 */
public record ItemCardVO(
        String code,
        String kind,
        String kindLabel,
        String status,
        String statusLabel,
        String name,
        String summary,
        String cycle,
        String pricing,
        SourceVO source
) {

    /**
     * 移植说明，供产品卡 srcbar 渲染。
     * <p>adaptLabel 与 kindLabel / statusLabel 同口径，在后端翻译。</p>
     */
    public record SourceVO(
            String origin,
            String adapt,
            String adaptLabel,
            String open,
            String runs
    ) {
    }
}
