package com.fdelab.module.product.admin.vo;

import java.time.LocalDateTime;

/**
 * 编辑抽屉回填。detail 按原样对象返回，前端负责格式化展示。
 */
public record ItemAdminVO(
        Long id,
        String code,
        Long productId,
        String kind,
        String name,
        String status,
        String summary,
        String cycle,
        String pricing,
        Object detail,
        Integer sortOrder,
        Integer published,
        LocalDateTime updateTime
) {
}
