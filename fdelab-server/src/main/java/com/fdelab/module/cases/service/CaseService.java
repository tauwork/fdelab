package com.fdelab.module.cases.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdelab.common.api.PageData;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.cases.entity.FdeCase;
import com.fdelab.module.cases.entity.FdeCustomer;
import com.fdelab.module.cases.mapper.FacetRow;
import com.fdelab.module.cases.mapper.FdeCaseMapper;
import com.fdelab.module.cases.mapper.FdeCustomerMapper;
import com.fdelab.module.cases.vo.CaseDetailVO;
import com.fdelab.module.cases.vo.CaseFacetsVO;
import com.fdelab.module.cases.vo.CaseListItemVO;
import com.fdelab.module.talent.entity.FdeTalent;
import com.fdelab.module.talent.mapper.FdeTalentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseService {

    private static final List<String> CRED_ORDER = List.of("A", "B", "C", "D");
    private static final int MAX_PAGE_SIZE = 50;

    private final FdeCaseMapper caseMapper;
    private final FdeCustomerMapper customerMapper;
    private final FdeTalentMapper talentMapper;
    private final CaseAssembler assembler;

    public PageData<CaseListItemVO> list(long page, long size, String q, String industry, String cred, String sort) {
        Page<FdeCase> result = caseMapper.selectPublicPage(
                pageOf(page, size),
                normalizeText(q),
                normalizeIndustry(industry),
                normalizeCred(cred),
                normalizeSort(sort));

        Map<Long, FdeCustomer> customers = loadCustomers(result.getRecords());
        List<CaseListItemVO> records = result.getRecords().stream()
                .map(item -> assembler.toListItem(item, customers.get(item.getCustomerId())))
                .toList();
        return PageData.of(result, records);
    }

    public CaseDetailVO detail(String code) {
        String value = normalizeText(code);
        if (value == null) {
            throw new BizException(404, "案例不存在");
        }
        FdeCase entity = caseMapper.selectOne(Wrappers.<FdeCase>lambdaQuery()
                .eq(FdeCase::getCode, value)
                .eq(FdeCase::getPublished, 1));
        if (entity == null) {
            // W7：查不到就 404，禁止用第一条顶替。
            throw new BizException(404, "案例不存在");
        }
        return assembler.toDetail(entity, loadCustomer(entity.getCustomerId()), loadPrimaryFde(entity.getPrimaryFdeId()));
    }

    public CaseFacetsVO facets() {
        Map<String, Long> credCounts = new HashMap<>();
        for (FacetRow row : caseMapper.selectCredFacets()) {
            if (!StringUtils.hasText(row.getCode())) {
                continue;
            }
            String code = row.getCode().trim().toUpperCase(Locale.ROOT);
            long count = row.getCnt() == null ? 0L : row.getCnt();
            credCounts.merge(code, count, Long::sum);
        }
        List<CaseFacetsVO.CredFacet> creds = CRED_ORDER.stream()
                .map(code -> new CaseFacetsVO.CredFacet(code, credCounts.getOrDefault(code, 0L)))
                .toList();

        List<CaseFacetsVO.IndustryFacet> industries = caseMapper.selectIndustryFacets().stream()
                .filter(row -> StringUtils.hasText(row.getName()))
                .map(row -> new CaseFacetsVO.IndustryFacet(
                        row.getName(),
                        row.getCnt() == null ? 0L : row.getCnt()))
                .toList();

        return new CaseFacetsVO(industries, creds, caseMapper.countPublic());
    }

    private Map<Long, FdeCustomer> loadCustomers(List<FdeCase> cases) {
        Set<Long> ids = cases.stream()
                .map(FdeCase::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<FdeCustomer> customers = customerMapper.selectList(Wrappers.<FdeCustomer>lambdaQuery()
                .in(FdeCustomer::getId, ids)
                .eq(FdeCustomer::getPublished, 1));
        return customers.stream()
                .collect(Collectors.toMap(FdeCustomer::getId, Function.identity(), (a, b) -> a));
    }

    private FdeCustomer loadCustomer(Long id) {
        if (id == null) {
            return null;
        }
        return customerMapper.selectOne(Wrappers.<FdeCustomer>lambdaQuery()
                .eq(FdeCustomer::getId, id)
                .eq(FdeCustomer::getPublished, 1));
    }

    private FdeTalent loadPrimaryFde(Long id) {
        if (id == null) {
            return null;
        }
        return talentMapper.selectOne(Wrappers.<FdeTalent>lambdaQuery()
                .eq(FdeTalent::getId, id)
                .eq(FdeTalent::getPublished, 1)
                .isNotNull(FdeTalent::getLevelCode));
    }

    private Page<FdeCase> pageOf(long page, long size) {
        long pageNo = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return new Page<>(pageNo, pageSize);
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeIndustry(String industry) {
        if (!StringUtils.hasText(industry)) {
            return null;
        }
        String value = industry.trim();
        return "全部".equals(value) ? null : value;
    }

    private String normalizeCred(String cred) {
        if (!StringUtils.hasText(cred)) {
            return null;
        }
        String value = cred.trim().toUpperCase(Locale.ROOT);
        return "全部".equals(value) ? null : value;
    }

    private String normalizeSort(String sort) {
        if (StringUtils.hasText(sort) && "occurYear".equalsIgnoreCase(sort.trim())) {
            return "occurYear";
        }
        return "code";
    }
}
