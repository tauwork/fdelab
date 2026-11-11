package com.fdelab.module.product.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 产品与服务枚举的中文唯一来源。前端不留副本。
 */
public final class ProductMeta {

    private static final Map<String, String> KIND_LABELS;
    private static final Map<String, String> STATUS_LABELS;
    private static final Map<String, String> ADAPT_LABELS;

    static {
        Map<String, String> kinds = new LinkedHashMap<>();
        kinds.put("m", "方法论");
        kinds.put("de", "数字员工");
        kinds.put("ag", "Agent");
        kinds.put("sa", "SaaS");
        kinds.put("vs", "整包方案");
        kinds.put("tr", "训练");
        kinds.put("pk", "CoE 包");
        KIND_LABELS = Collections.unmodifiableMap(kinds);

        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put("on", "能接");
        statuses.put("beta", "排队中");
        statuses.put("dev", "还没做出来");
        STATUS_LABELS = Collections.unmodifiableMap(statuses);

        Map<String, String> adapts = new LinkedHashMap<>();
        adapts.put("a", "直接能用");
        adapts.put("b", "小改");
        adapts.put("c", "得重做");
        ADAPT_LABELS = Collections.unmodifiableMap(adapts);
    }

    private ProductMeta() {
    }

    public static Map<String, String> kindLabels() {
        return KIND_LABELS;
    }

    public static Map<String, String> statusLabels() {
        return STATUS_LABELS;
    }

    public static String kindLabel(String code) {
        return code == null ? null : KIND_LABELS.getOrDefault(code, code);
    }

    public static String statusLabel(String code) {
        return code == null ? null : STATUS_LABELS.getOrDefault(code, code);
    }

    public static String adaptLabel(String code) {
        return code == null ? null : ADAPT_LABELS.getOrDefault(code, code);
    }
}
