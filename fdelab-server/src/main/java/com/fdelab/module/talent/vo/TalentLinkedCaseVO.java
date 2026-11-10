package com.fdelab.module.talent.vo;

/**
 * 后台人才关联案例列表项，包含未发布案例。
 */
public record TalentLinkedCaseVO(
        Long id,
        String code,
        String title,
        String industry,
        Integer published,
        Integer sortNo
) {
}
