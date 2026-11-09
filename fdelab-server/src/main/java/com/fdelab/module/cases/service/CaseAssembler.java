package com.fdelab.module.cases.service;

import com.fdelab.module.cases.entity.FdeCase;
import com.fdelab.module.cases.entity.FdeCustomer;
import com.fdelab.module.cases.model.CaseContent;
import com.fdelab.module.cases.vo.CaseDetailVO;
import com.fdelab.module.cases.vo.CaseListItemVO;
import com.fdelab.module.talent.entity.FdeTalent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 案例 VO 转换的唯一入口：列表、详情共用，二期后台预览页也复用。
 */
@Component
@RequiredArgsConstructor
public class CaseAssembler {

    private final CaseContentCodec contentCodec;

    public CaseListItemVO toListItem(FdeCase entity, FdeCustomer customer) {
        CaseContent content = contentCodec.readContent(entity.getContentJson());
        return new CaseListItemVO(
                entity.getId(),
                entity.getCode(),
                entity.getTitle(),
                entity.getSubtitle(),
                entity.getIndustry(),
                entity.getFunc(),
                entity.getOccurYear(),
                entity.getPartner(),
                customer == null ? null : customer.getDisplayName(),
                entity.getCred(),
                entity.getCredNote(),
                entity.getFdeMode(),
                entity.getScale(),
                entity.getModelPct(),
                content.getTech(),
                content.getAi(),
                content.getMgmt(),
                content.getKpis(),
                content.getMetrics().size(),
                entity.getPublished(),
                entity.getPain());
    }

    public CaseDetailVO toDetail(FdeCase entity, FdeCustomer customer, FdeTalent primaryFde) {
        CaseContent content = contentCodec.readContent(entity.getContentJson());
        CaseDetailVO.CustomerVO customerVO = customer == null
                ? null
                : new CaseDetailVO.CustomerVO(customer.getId(), customer.getDisplayName(), customer.getIsMasked());
        CaseDetailVO.FdeRefVO fdeVO = primaryFde == null
                ? null
                : new CaseDetailVO.FdeRefVO(primaryFde.getName(), primaryFde.getSlug(), primaryFde.getLevelCode());

        return new CaseDetailVO(
                entity.getId(),
                entity.getCode(),
                entity.getTitle(),
                entity.getSubtitle(),
                entity.getIndustry(),
                entity.getFunc(),
                entity.getOccurYear(),
                entity.getPartner(),
                customer == null ? null : customer.getDisplayName(),
                entity.getCred(),
                entity.getCredNote(),
                entity.getFdeMode(),
                entity.getScale(),
                entity.getModelPct(),
                content.getTech(),
                content.getAi(),
                content.getMgmt(),
                content.getKpis(),
                content.getMetrics().size(),
                entity.getPublished(),
                entity.getPain(),
                entity.getGoal(),
                contentCodec.readVerdict(entity.getVerdict()),
                contentCodec.readTestimony(entity.getTestimony()),
                content.getTimeline(),
                content.getOrg(),
                content.getMetrics(),
                content.getCsf(),
                content.getQuotes(),
                content.getSources(),
                customerVO,
                fdeVO,
                // 一期不建 fde_product 表，详情页「相关产品」区块不渲染。
                // TODO 二期：按 fde_product.case_ids 反查后返回。
                List.of());
    }
}
