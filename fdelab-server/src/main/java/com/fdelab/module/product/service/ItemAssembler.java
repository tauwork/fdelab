package com.fdelab.module.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdelab.module.product.entity.FdeProductForm;
import com.fdelab.module.product.entity.FdeProductItem;
import com.fdelab.module.product.vo.ItemCardVO;
import com.fdelab.module.product.vo.ItemDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 列（detail / case_ids / deliverables）的唯一读写入口。
 * <p>detail 是内容文档而非 schema：只解析成 Map 原样透传，不校验内部键。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ItemAssembler {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public Map<String, Object> readObject(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> value = objectMapper.readValue(json, MAP_TYPE);
            return value == null ? new LinkedHashMap<>() : value;
        } catch (JsonProcessingException ex) {
            log.warn("产品条目 detail 解析失败，按空对象处理", ex);
            return new LinkedHashMap<>();
        }
    }

    public List<String> readStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? List.of() : values;
        } catch (JsonProcessingException ex) {
            log.warn("产品 JSON 字符串数组解析失败，按空数组处理", ex);
            return List.of();
        }
    }

    public List<Long> readLongList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            List<Long> values = objectMapper.readValue(json, LONG_LIST_TYPE);
            return values == null ? List.of() : values;
        } catch (JsonProcessingException ex) {
            log.warn("产品 JSON 数字数组解析失败，按空数组处理", ex);
            return List.of();
        }
    }

    /** 字符串数组 → JSON 文本；空集合写 NULL，保证后台「清空」能真正落库。 */
    public String writeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("产品 JSON 字符串数组序列化失败", ex);
        }
    }

    /** 数字数组 → JSON 文本；空集合写 NULL。 */
    public String writeLongList(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("产品 JSON 数字数组序列化失败", ex);
        }
    }

    public ItemCardVO toCard(FdeProductItem entity) {
        Map<String, Object> detail = readObject(entity.getDetail());
        Map<String, Object> source = asMap(detail.get("source"));
        return new ItemCardVO(
                entity.getCode(),
                entity.getKind(),
                ProductMeta.kindLabel(entity.getKind()),
                entity.getStatus(),
                ProductMeta.statusLabel(entity.getStatus()),
                entity.getName(),
                entity.getSummary(),
                entity.getCycle(),
                entity.getPricing(),
                source == null ? null : toSource(source));
    }

    public ItemDetailVO toDetail(FdeProductItem entity, FdeProductForm product) {
        Map<String, Object> detail = readObject(entity.getDetail());
        ItemDetailVO.ProductRefVO productRef = product == null
                ? null
                : new ItemDetailVO.ProductRefVO(product.getCode(), product.getName());
        return new ItemDetailVO(
                entity.getCode(),
                entity.getKind(),
                ProductMeta.kindLabel(entity.getKind()),
                entity.getStatus(),
                ProductMeta.statusLabel(entity.getStatus()),
                entity.getName(),
                entity.getSummary(),
                entity.getCycle(),
                entity.getPricing(),
                productRef,
                detail.isEmpty() ? null : detail);
    }

    private ItemCardVO.SourceVO toSource(Map<String, Object> source) {
        String adapt = asString(source.get("adapt"));
        return new ItemCardVO.SourceVO(
                asString(source.get("origin")),
                adapt,
                ProductMeta.adaptLabel(adapt),
                asString(source.get("open")),
                asString(source.get("runs")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
