package com.fdelab.module.cases.admin.vo;

import java.time.LocalDateTime;

/**
 * 后台案例列表项。
 */
public record CaseAdminListItemVO(
        Long id,
        String code,
        String title,
        String industry,
        String customerName,
        String cred,
        Integer published,
        int metricCount,
        LocalDateTime updateTime
) {
}
