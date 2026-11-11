package com.fdelab.module.product.admin.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务运作编辑抽屉回填。deliverables / caseIds 由 JSON 列解析成数组，前端不再处理 JSON 文本。
 */
public record ProductFormAdminVO(
        Long id,
        String code,
        String name,
        String form,
        String weight,
        Integer sortOrder,
        String fit,
        String description,
        List<String> deliverables,
        String cycle,
        String pricing,
        String success,
        List<Long> caseIds,
        Integer published,
        LocalDateTime updateTime
) {
}
