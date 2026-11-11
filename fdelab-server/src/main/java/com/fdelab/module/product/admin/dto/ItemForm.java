package com.fdelab.module.product.admin.dto;

/**
 * 条目管理表单。除这 8 项内容必填外，code 属系统必填（唯一业务码，新建时必须给）。
 * detail 接受 JSON 对象或 JSON 文本，保存前统一校验。
 */
public record ItemForm(
        String code,
        Long productId,
        String kind,
        String name,
        String status,
        String summary,
        String cycle,
        String pricing,
        Object detail
) {
}
