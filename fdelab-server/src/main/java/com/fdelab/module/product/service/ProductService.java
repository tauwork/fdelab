package com.fdelab.module.product.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.cases.entity.FdeCase;
import com.fdelab.module.cases.mapper.FdeCaseMapper;
import com.fdelab.module.cases.service.CaseContentCodec;
import com.fdelab.module.product.entity.FdeProductForm;
import com.fdelab.module.product.entity.FdeProductItem;
import com.fdelab.module.product.mapper.FdeProductFormMapper;
import com.fdelab.module.product.mapper.FdeProductItemMapper;
import com.fdelab.module.product.vo.CaseRefVO;
import com.fdelab.module.product.vo.ProductFormDetailVO;
import com.fdelab.module.product.vo.ProductFormVO;
import com.fdelab.module.product.vo.ItemCardVO;
import com.fdelab.module.product.vo.ItemDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 产品与服务公开读接口的组装：运作层、条目层、枚举翻译、参照案例解析。
 * <p>可见性口径统一为 published=1（逻辑删除由 MyBatis-Plus 自动追加）。</p>
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final FdeProductFormMapper productFormMapper;
    private final FdeProductItemMapper itemMapper;
    private final FdeCaseMapper caseMapper;
    private final CaseContentCodec caseContentCodec;
    private final ItemAssembler assembler;

    public List<ProductFormVO> forms() {
        return productFormMapper.selectList(Wrappers.<FdeProductForm>lambdaQuery()
                .eq(FdeProductForm::getPublished, 1)
                .orderByAsc(FdeProductForm::getSortOrder))
                .stream()
                .map(this::toForm)
                .toList();
    }

    public ProductFormDetailVO form(String code) {
        String value = normalize(code);
        if (value == null) {
            throw new BizException(404, "产品运作不存在");
        }
        FdeProductForm entity = productFormMapper.selectOne(Wrappers.<FdeProductForm>lambdaQuery()
                .eq(FdeProductForm::getCode, value)
                .eq(FdeProductForm::getPublished, 1));
        if (entity == null) {
            throw new BizException(404, "产品运作不存在");
        }
        return new ProductFormDetailVO(
                entity.getCode(),
                no(entity.getSortOrder()),
                entity.getName(),
                label(entity),
                entity.getDescription(),
                entity.getFit(),
                entity.getCycle(),
                entity.getPricing(),
                assembler.readStringList(entity.getDeliverables()),
                entity.getSuccess(),
                caseRefs(entity.getCaseIds()));
    }

    public List<ItemCardVO> items(String kind) {
        String kindValue = normalize(kind);
        return itemMapper.selectList(Wrappers.<FdeProductItem>lambdaQuery()
                        .eq(FdeProductItem::getPublished, 1)
                        .eq(kindValue != null, FdeProductItem::getKind, kindValue)
                        .orderByAsc(FdeProductItem::getSortOrder))
                .stream()
                .map(assembler::toCard)
                .toList();
    }

    public ItemDetailVO item(String code) {
        String value = normalize(code);
        if (value == null) {
            throw new BizException(404, "条目不存在");
        }
        FdeProductItem entity = itemMapper.selectOne(Wrappers.<FdeProductItem>lambdaQuery()
                .eq(FdeProductItem::getCode, value)
                .eq(FdeProductItem::getPublished, 1));
        if (entity == null) {
            throw new BizException(404, "条目不存在");
        }
        FdeProductForm form = entity.getProductId() == null ? null : productFormMapper.selectById(entity.getProductId());
        return assembler.toDetail(entity, form);
    }

    private ProductFormVO toForm(FdeProductForm entity) {
        return new ProductFormVO(
                entity.getCode(),
                no(entity.getSortOrder()),
                entity.getName(),
                label(entity),
                entity.getDescription(),
                entity.getFit(),
                entity.getCycle(),
                entity.getPricing());
    }

    /** 参照案例按 case_ids 顺序解析；查不到或未发布的自动跳过，空则不顶替。 */
    private List<CaseRefVO> caseRefs(String caseIdsJson) {
        List<Long> ids = assembler.readLongList(caseIdsJson);
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, FdeCase> cases = caseMapper.selectList(Wrappers.<FdeCase>lambdaQuery()
                        .select(FdeCase::getId, FdeCase::getCode, FdeCase::getTitle,
                                FdeCase::getSubtitle, FdeCase::getCred, FdeCase::getContentJson)
                        .in(FdeCase::getId, ids)
                        .eq(FdeCase::getPublished, 1))
                .stream()
                .collect(Collectors.toMap(FdeCase::getId, Function.identity()));
        return ids.stream()
                .map(cases::get)
                .filter(Objects::nonNull)
                .map(this::toCaseRef)
                .toList();
    }

    private CaseRefVO toCaseRef(FdeCase entity) {
        List<CaseRefVO.KpiVO> kpis = caseContentCodec.readContent(entity.getContentJson()).getKpis().stream()
                .map(kpi -> new CaseRefVO.KpiVO(kpi.getV(), kpi.getK(), Boolean.TRUE.equals(kpi.getR())))
                .toList();
        return new CaseRefVO(
                entity.getId(),
                entity.getCode(),
                entity.getTitle(),
                entity.getSubtitle(),
                entity.getCred(),
                kpis);
    }

    private static String label(FdeProductForm entity) {
        boolean hasForm = StringUtils.hasText(entity.getForm());
        boolean hasWeight = StringUtils.hasText(entity.getWeight());
        if (!hasForm) {
            return hasWeight ? entity.getWeight() : null;
        }
        return hasWeight ? entity.getForm() + " · " + entity.getWeight() : entity.getForm();
    }

    private static String no(Integer sortOrder) {
        return sortOrder == null ? null : String.format("%02d", sortOrder);
    }

    private static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
