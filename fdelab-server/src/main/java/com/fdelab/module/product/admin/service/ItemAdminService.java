package com.fdelab.module.product.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdelab.common.api.PageData;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.product.admin.dto.ItemForm;
import com.fdelab.module.product.admin.vo.ItemAdminListItemVO;
import com.fdelab.module.product.admin.vo.ItemAdminVO;
import com.fdelab.module.product.admin.vo.ItemMetaVO;
import com.fdelab.module.product.entity.FdeProductForm;
import com.fdelab.module.product.entity.FdeProductItem;
import com.fdelab.module.product.mapper.FdeProductFormMapper;
import com.fdelab.module.product.mapper.FdeProductItemMapper;
import com.fdelab.module.product.service.ProductMeta;
import com.fdelab.module.product.service.ItemAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 条目后台写入路径。发布/下架的角色校验只在 Controller 做，本类不判角色。
 */
@Service
@RequiredArgsConstructor
public class ItemAdminService {

    private static final int MAX_PAGE_SIZE = 100;

    private final FdeProductItemMapper itemMapper;
    private final FdeProductFormMapper productMapper;
    private final ItemAssembler itemAssembler;
    private final ObjectMapper objectMapper;

    public PageData<ItemAdminListItemVO> page(long page, long size, String q,
                                             String kind, String status, Integer published) {
        String keyword = trimToNull(q);
        Page<FdeProductItem> result = itemMapper.selectPage(pageOf(page, size),
                Wrappers.<FdeProductItem>lambdaQuery()
                        .and(keyword != null, wrapper -> wrapper
                                .like(FdeProductItem::getCode, keyword)
                                .or()
                                .like(FdeProductItem::getName, keyword))
                        .eq(trimToNull(kind) != null, FdeProductItem::getKind, trimToNull(kind))
                        .eq(trimToNull(status) != null, FdeProductItem::getStatus, trimToNull(status))
                        .eq(published != null, FdeProductItem::getPublished, published)
                        .orderByAsc(FdeProductItem::getSortOrder)
                        .orderByAsc(FdeProductItem::getCode));

        List<ItemAdminListItemVO> records = result.getRecords().stream()
                .map(item -> new ItemAdminListItemVO(
                        item.getId(),
                        item.getCode(),
                        item.getName(),
                        item.getKind(),
                        item.getStatus(),
                        item.getPublished(),
                        item.getUpdateTime()))
                .toList();
        return PageData.of(result, records);
    }

    public ItemAdminVO get(Long id) {
        return toVO(require(id));
    }

    @Transactional
    public ItemAdminVO create(ItemForm form) {
        validate(form);
        FdeProductForm product = productMapper.selectById(form.productId());
        String prefix = codePrefix(product);
        FdeProductItem entity = new FdeProductItem();
        apply(entity, form);
        entity.setSortOrder(nextSortOrder());
        // 新建一律落草稿，发布只能走 publish 接口。
        entity.setPublished(0);
        // 业务码由服务端生成；并发下若被抢占，重取一次最大号再试。
        for (int attempt = 0; attempt < 5; attempt++) {
            entity.setId(null);
            entity.setCode(nextCode(prefix));
            try {
                itemMapper.insert(entity);
                return toVO(entity);
            } catch (DuplicateKeyException ignored) {
                // 唯一索引兜底，下一轮重新取号。
            }
        }
        throw new BizException(409, "业务码生成冲突，请重试");
    }

    @Transactional
    public ItemAdminVO update(Long id, ItemForm form) {
        FdeProductItem entity = require(id);
        validate(form);
        apply(entity, form);
        // 保存一律回到待复核，发布只能走 publish 接口。
        entity.setPublished(0);
        entity.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public ItemAdminVO publish(Long id, Integer published) {
        if (published == null || (published != 0 && published != 1)) {
            throw new BizException(422, "发布状态只能是 0 或 1");
        }
        FdeProductItem entity = require(id);
        entity.setPublished(published);
        entity.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public void delete(Long id) {
        require(id);
        itemMapper.deleteById(id);
    }

    public ItemMetaVO meta() {
        List<ItemMetaVO.Option> kinds = ProductMeta.kindLabels().entrySet().stream()
                .map(entry -> new ItemMetaVO.Option(entry.getKey(), entry.getValue()))
                .toList();
        List<ItemMetaVO.Option> statuses = ProductMeta.statusLabels().entrySet().stream()
                .map(entry -> new ItemMetaVO.Option(entry.getKey(), entry.getValue()))
                .toList();
        List<ItemMetaVO.ProductOption> products = productMapper.selectList(
                        Wrappers.<FdeProductForm>lambdaQuery()
                                .orderByAsc(FdeProductForm::getSortOrder))
                .stream()
                .map(item -> new ItemMetaVO.ProductOption(item.getId(), item.getCode(), item.getName()))
                .toList();
        return new ItemMetaVO(kinds, statuses, products);
    }

    private void validate(ItemForm form) {
        if (form == null) {
            throw new BizException(422, "表单不能为空");
        }
        if (form.productId() == null) {
            throw new BizException(422, "所属运作不能为空");
        }
        requireText(form.kind(), "形态不能为空");
        requireText(form.name(), "产品名不能为空");
        requireText(form.status(), "能力状态不能为空");
        requireText(form.summary(), "产品介绍不能为空");
        requireText(form.cycle(), "周期不能为空");
        requireText(form.pricing(), "计价不能为空");

        if (!ProductMeta.kindLabels().containsKey(form.kind())) {
            throw new BizException(422, "形态取值不合法");
        }
        if (!ProductMeta.statusLabels().containsKey(form.status())) {
            throw new BizException(422, "能力状态取值不合法");
        }
        if (productMapper.selectById(form.productId()) == null) {
            throw new BizException(422, "所属运作不存在");
        }
    }

    private void apply(FdeProductItem entity, ItemForm form) {
        entity.setProductId(form.productId());
        entity.setKind(form.kind());
        entity.setName(form.name().trim());
        entity.setStatus(form.status());
        entity.setSummary(form.summary());
        entity.setCycle(form.cycle());
        entity.setPricing(form.pricing());
        entity.setDetail(normalizeDetail(form.detail()));
    }

    /** 接受 JSON 对象或 JSON 文本；空值存 NULL，非法 JSON 直接拒绝。 */
    private String normalizeDetail(Object detail) {
        if (detail == null) {
            return null;
        }
        if (detail instanceof String text) {
            if (!StringUtils.hasText(text)) {
                return null;
            }
            try {
                if (objectMapper.readTree(text).isNull()) {
                    return null;
                }
            } catch (JsonProcessingException ex) {
                throw new BizException(422, "详情内容不是合法 JSON");
            }
            return text.trim();
        }
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException ex) {
            throw new BizException(422, "详情内容不是合法 JSON");
        }
    }

    private FdeProductItem require(Long id) {
        FdeProductItem entity = id == null ? null : itemMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "产品不存在");
        }
        return entity;
    }

    /**
     * 条目业务码沿用「运作序号 → 字母」的既有约定：F1→A、F2→B…，
     * 非 F{n} 形态的运作统一用 P 前缀。
     */
    private String codePrefix(FdeProductForm product) {
        String code = product == null ? null : product.getCode();
        if (code != null && code.matches("F\\d{1,2}")) {
            int no = Integer.parseInt(code.substring(1));
            if (no >= 1 && no <= 26) {
                return String.valueOf((char) ('A' + no - 1));
            }
        }
        return "P";
    }

    private String nextCode(String prefix) {
        Integer max = itemMapper.maxCodeSuffix(prefix, prefix.length());
        return prefix + (max == null ? 1 : max + 1);
    }

    private int nextSortOrder() {
        List<FdeProductItem> last = itemMapper.selectList(Wrappers.<FdeProductItem>lambdaQuery()
                .select(FdeProductItem::getSortOrder)
                .orderByDesc(FdeProductItem::getSortOrder)
                .last("LIMIT 1"));
        return last.isEmpty() || last.get(0).getSortOrder() == null
                ? 1
                : last.get(0).getSortOrder() + 1;
    }

    private ItemAdminVO toVO(FdeProductItem entity) {
        Map<String, Object> detail = itemAssembler.readObject(entity.getDetail());
        return new ItemAdminVO(
                entity.getId(),
                entity.getCode(),
                entity.getProductId(),
                entity.getKind(),
                entity.getName(),
                entity.getStatus(),
                entity.getSummary(),
                entity.getCycle(),
                entity.getPricing(),
                detail.isEmpty() ? null : detail,
                entity.getSortOrder(),
                entity.getPublished(),
                entity.getUpdateTime());
    }

    private Page<FdeProductItem> pageOf(long page, long size) {
        long pageNo = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return new Page<>(pageNo, pageSize);
    }

    private static void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(422, message);
        }
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
