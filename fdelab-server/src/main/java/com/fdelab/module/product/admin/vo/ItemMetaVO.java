package com.fdelab.module.product.admin.vo;

import java.util.List;

public record ItemMetaVO(
        List<Option> kinds,
        List<Option> statuses,
        List<ProductOption> products
) {

    public record Option(String value, String label) {
    }

    public record ProductOption(Long id, String code, String name) {
    }
}
