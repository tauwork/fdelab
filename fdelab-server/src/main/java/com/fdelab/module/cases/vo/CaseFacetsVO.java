package com.fdelab.module.cases.vo;

import java.util.List;

/**
 * 列表页筛选项。计数基于全量已发布数据，不受当前 q / industry / cred 影响。
 */
public record CaseFacetsVO(
        List<IndustryFacet> industries,
        List<CredFacet> creds,
        long total
) {

    public record IndustryFacet(
            String name,
            long count
    ) {
    }

    public record CredFacet(
            String code,
            long count
    ) {
    }
}
