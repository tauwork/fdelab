package com.fdelab.module.product.admin.vo;

import java.time.LocalDateTime;

/**
 * 服务运作列表行。itemCount 是挂在它下面的产品条目数，用于删除前的归属提示。
 */
public record ProductFormAdminListItemVO(
        Long id,
        String code,
        String name,
        String form,
        String weight,
        Integer sortOrder,
        Integer itemCount,
        Integer published,
        LocalDateTime updateTime
) {
}
