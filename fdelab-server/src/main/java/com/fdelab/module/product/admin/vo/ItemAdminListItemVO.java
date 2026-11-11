package com.fdelab.module.product.admin.vo;

import java.time.LocalDateTime;

public record ItemAdminListItemVO(
        Long id,
        String code,
        String name,
        String kind,
        String status,
        Integer published,
        LocalDateTime updateTime
) {
}
