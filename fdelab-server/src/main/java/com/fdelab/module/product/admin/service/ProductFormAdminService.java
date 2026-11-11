package com.fdelab.module.product.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdelab.common.api.PageData;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.cases.entity.FdeCase;
import com.fdelab.module.cases.mapper.FdeCaseMapper;
import com.fdelab.module.product.admin.dto.ProductFormRequest;
import com.fdelab.module.product.admin.vo.ProductFormAdminListItemVO;
import com.fdelab.module.product.admin.vo.ProductFormAdminVO;
import com.fdelab.module.product.admin.vo.ProductFormMetaVO;
import com.fdelab.module.product.entity.FdeProductForm;
import com.fdelab.module.product.entity.FdeProductItem;
import com.fdelab.module.product.mapper.FdeProductFormMapper;
import com.fdelab.module.product.mapper.FdeProductItemMapper;
import com.fdelab.module.product.service.ItemAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 服务运作后台写入路径。与条目模块同口径：
 * 新建/保存一律回到待复核，发布/下架的角色校验只在 Controller 做，本类不判角色。
 */
@Service
@RequiredArgsConstructor
public class ProductFormAdminService {

    private static final int MAX_PAGE_SIZE = 100;

    private final FdeProductFormMapper formMapper;
    private final FdeProductItemMapper itemMapper;
    private final FdeCaseMapper caseMapper;
    private final ItemAssembler itemAssembler;

    public PageData<ProductFormAdminListItemVO> page(long page, long size, String q, Integer published) {
        String keyword = trimToNull(q);
        Page<FdeProductForm> result = formMapper.selectPage(pageOf(page, size),
                Wrappers.<FdeProductForm>lambdaQuery()
                        .and(keyword != null, wrapper -> wrapper
                                .like(FdeProductForm::getCode, keyword)
                                .or()
                                .like(FdeProductForm::getName, keyword))
                        .eq(published != null, FdeProductForm::getPublished, published)
                        .orderByAsc(FdeProductForm::getSortOrder)
                        .orderByAsc(FdeProductForm::getCode));

        List<ProductFormAdminListItemVO> records = result.getRecords().stream()
                .map(item -> new ProductFormAdminListItemVO(
                        item.getId(),
                        item.getCode(),
                        item.getName(),
                        item.getForm(),
                        item.getWeight(),
                        item.getSortOrder(),
                        countItems(item.getId()),
                        item.getPublished(),
                        item.getUpdateTime()))
                .toList();
        return PageData.of(result, records);
    }

    public ProductFormMetaVO meta() {
        List<ProductFormMetaVO.CaseOption> cases = caseMapper.selectList(
                        Wrappers.<FdeCase>lambdaQuery()
                                .select(FdeCase::getId, FdeCase::getCode,
                                        FdeCase::getTitle, FdeCase::getPublished)
                                .orderByAsc(FdeCase::getCode))
                .stream()
                .map(item -> new ProductFormMetaVO.CaseOption(
                        item.getId(), item.getCode(), item.getTitle(), item.getPublished()))
                .toList();
        return new ProductFormMetaVO(cases);
    }

    public ProductFormAdminVO get(Long id) {
        return toVO(require(id));
    }

    @Transactional
    public ProductFormAdminVO create(ProductFormRequest request) {
        validate(request);
        String code = request.code().trim();
        if (formMapper.selectCount(Wrappers.<FdeProductForm>lambdaQuery()
                .eq(FdeProductForm::getCode, code)) > 0) {
            throw new BizException(422, "业务码已存在");
        }
        FdeProductForm entity = new FdeProductForm();
        entity.setCode(code);
        apply(entity, request);
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(nextSortOrder());
        }
        entity.setPublished(0);
        try {
            formMapper.insert(entity);
        } catch (DuplicateKeyException ex) {
            throw new BizException(422, "业务码已存在");
        }
        return toVO(entity);
    }

    @Transactional
    public ProductFormAdminVO update(Long id, ProductFormRequest request) {
        FdeProductForm entity = require(id);
        validate(request);
        apply(entity, request);
        // 保存一律回到待复核，发布只能走 publish 接口。
        entity.setPublished(0);
        entity.setUpdateTime(LocalDateTime.now());
        formMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public ProductFormAdminVO publish(Long id, Integer published) {
        if (published == null || (published != 0 && published != 1)) {
            throw new BizException(422, "发布状态只能是 0 或 1");
        }
        FdeProductForm entity = require(id);
        entity.setPublished(published);
        entity.setUpdateTime(LocalDateTime.now());
        formMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public void delete(Long id) {
        FdeProductForm entity = require(id);
        if (countItems(entity.getId()) > 0) {
            throw new BizException(422, "该服务运作下仍挂着产品条目，请先调整条目归属再删除");
        }
        formMapper.deleteById(id);
    }

    private void validate(ProductFormRequest request) {
        if (request == null) {
            throw new BizException(422, "表单不能为空");
        }
        requireText(request.code(), "业务码不能为空");
        requireText(request.name(), "名称不能为空");
        requireText(request.form(), "运作标签不能为空");
        requireText(request.description(), "说明不能为空");
        requireText(request.cycle(), "周期不能为空");
        requireText(request.pricing(), "计价不能为空");
        if (request.sortOrder() != null && (request.sortOrder() < 1 || request.sortOrder() > 999)) {
            throw new BizException(422, "展示顺序需在 1–999 之间");
        }
        validateCaseIds(request.caseIds());
    }

    /** 参照案例 id 必须真实存在，避免详情页出现永远解析不出的空引用。 */
    private void validateCaseIds(List<Long> caseIds) {
        if (caseIds == null || caseIds.isEmpty()) {
            return;
        }
        if (caseIds.stream().anyMatch(Objects::isNull)) {
            throw new BizException(422, "参照案例 id 不能为空");
        }
        Set<Long> requested = new HashSet<>(caseIds);
        Set<Long> existing = new HashSet<>(caseMapper.selectList(Wrappers.<FdeCase>lambdaQuery()
                        .select(FdeCase::getId)
                        .in(FdeCase::getId, requested))
                .stream()
                .map(FdeCase::getId)
                .toList());
        if (!existing.containsAll(requested)) {
            throw new BizException(422, "参照案例不存在");
        }
    }

    private void apply(FdeProductForm entity, ProductFormRequest request) {
        entity.setName(request.name().trim());
        entity.setForm(trimToNull(request.form()));
        entity.setWeight(trimToNull(request.weight()));
        entity.setSortOrder(request.sortOrder());
        entity.setFit(trimToNull(request.fit()));
        entity.setDescription(trimToNull(request.description()));
        entity.setDeliverables(itemAssembler.writeStringList(cleanStrings(request.deliverables())));
        entity.setCycle(trimToNull(request.cycle()));
        entity.setPricing(trimToNull(request.pricing()));
        entity.setSuccess(trimToNull(request.success()));
        entity.setCaseIds(itemAssembler.writeLongList(cleanIds(request.caseIds())));
        // customer_ids 一期不展示、后台也不读写：entity 从库里带出来的原值在 updateById 时原样保留。
    }

    private int countItems(Long formId) {
        if (formId == null) {
            return 0;
        }
        Long count = itemMapper.selectCount(Wrappers.<FdeProductItem>lambdaQuery()
                .eq(FdeProductItem::getProductId, formId));
        return count == null ? 0 : count.intValue();
    }

    private FdeProductForm require(Long id) {
        FdeProductForm entity = id == null ? null : formMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "服务运作不存在");
        }
        return entity;
    }

    private int nextSortOrder() {
        List<FdeProductForm> last = formMapper.selectList(Wrappers.<FdeProductForm>lambdaQuery()
                .select(FdeProductForm::getSortOrder)
                .orderByDesc(FdeProductForm::getSortOrder)
                .last("LIMIT 1"));
        return last.isEmpty() || last.get(0).getSortOrder() == null
                ? 1
                : last.get(0).getSortOrder() + 1;
    }

    private ProductFormAdminVO toVO(FdeProductForm entity) {
        return new ProductFormAdminVO(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getForm(),
                entity.getWeight(),
                entity.getSortOrder(),
                entity.getFit(),
                entity.getDescription(),
                itemAssembler.readStringList(entity.getDeliverables()),
                entity.getCycle(),
                entity.getPricing(),
                entity.getSuccess(),
                itemAssembler.readLongList(entity.getCaseIds()),
                entity.getPublished(),
                entity.getUpdateTime());
    }

    private Page<FdeProductForm> pageOf(long page, long size) {
        long pageNo = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return new Page<>(pageNo, pageSize);
    }

    private static List<String> cleanStrings(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
    }

    private static List<Long> cleanIds(List<Long> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(Objects::nonNull).distinct().toList();
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
