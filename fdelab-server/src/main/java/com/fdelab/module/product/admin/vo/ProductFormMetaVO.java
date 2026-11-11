package com.fdelab.module.product.admin.vo;

import java.util.List;

/**
 * 服务运作表单词表：参照案例选择器（含草稿，公开页会过滤未发布案例）。
 */
public record ProductFormMetaVO(List<CaseOption> cases) {

    public record CaseOption(Long id, String code, String title, Integer published) {
    }
}
