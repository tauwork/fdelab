package com.fdelab.module.product.admin.dto;

/**
 * 产品管理表单。业务码由服务端生成，不允许前端传入；除内容必填项外，
 * detail 接受 JSON 对象或 JSON 文本，保存前统一校验。
 */
public record ItemForm(
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
