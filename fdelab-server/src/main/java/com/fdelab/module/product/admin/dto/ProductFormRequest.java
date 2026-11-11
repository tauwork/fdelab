package com.fdelab.module.product.admin.dto;

import java.util.List;

/**
 * 服务运作（fde_product_form）后台表单。
 * <p>业务码由服务端生成，不允许前端传入；卡片必填：name / form / description / cycle / pricing；
 * weight / fit / deliverables / success / caseIds 属详情内容，允许后补。</p>
 * <p>published 不在表单内：新建与保存一律回到待复核，发布只能走 publish 接口。
 * customer_ids 一期不展示，后台也不读写（更新时沿用实体原值）。</p>
 */
public record ProductFormRequest(
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
        List<Long> caseIds
) {
}
