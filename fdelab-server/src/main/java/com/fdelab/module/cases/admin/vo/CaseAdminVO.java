package com.fdelab.module.cases.admin.vo;

import com.fdelab.module.cases.admin.dto.CaseForm;

import java.time.LocalDateTime;

/**
 * 后台案例详情。前端 API 层会扁平化成 {@code {...form, id}}，页面只认扁平字段。
 */
public record CaseAdminVO(
        Long id,
        CaseForm form,
        String customerName,
        Integer customerMasked,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
