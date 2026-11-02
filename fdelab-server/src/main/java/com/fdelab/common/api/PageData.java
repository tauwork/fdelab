package com.fdelab.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public record PageData<T>(long total, long page, long size, List<T> records) {

    public static <E, T> PageData<T> of(Page<E> page, List<T> records) {
        return new PageData<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }
}
