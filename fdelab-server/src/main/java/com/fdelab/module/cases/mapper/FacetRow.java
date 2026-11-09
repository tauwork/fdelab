package com.fdelab.module.cases.mapper;

import lombok.Data;

/**
 * 聚合查询的临时行，仅供 facets 使用。
 */
@Data
public class FacetRow {

    private String name;
    private String code;
    private Long cnt;
}
