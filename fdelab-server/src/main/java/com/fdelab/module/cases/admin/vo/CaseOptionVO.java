package com.fdelab.module.cases.admin.vo;

/** 后台人才关联选择器的案例选项，包含草稿。 */
public record CaseOptionVO(
        Long id,
        String code,
        String title,
        String industry,
        Integer published
) {
}
