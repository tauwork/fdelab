package com.fdelab.module.cases.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdelab.common.api.PageData;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.cases.admin.dto.CaseForm;
import com.fdelab.module.cases.admin.vo.CaseAdminListItemVO;
import com.fdelab.module.cases.admin.vo.CaseAdminVO;
import com.fdelab.module.cases.admin.vo.CaseMetaVO;
import com.fdelab.module.cases.admin.vo.CaseOptionVO;
import com.fdelab.module.cases.admin.vo.CaseValidationVO;
import com.fdelab.module.cases.entity.FdeCase;
import com.fdelab.module.cases.entity.FdeCustomer;
import com.fdelab.module.cases.mapper.FdeCaseMapper;
import com.fdelab.module.cases.mapper.FdeCustomerMapper;
import com.fdelab.module.cases.model.CaseContent;
import com.fdelab.module.cases.service.CaseContentCodec;
import com.fdelab.module.talent.service.TalentCaseLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 案例后台写入路径。
 *
 * <p>JSON 读写统一走 {@link CaseContentCodec}；本类不手拼 JSON。</p>
 * <p>发布/下架只在 CaseAdminController 里做 ADMIN 角色校验，本服务不判角色。</p>
 */
@Service
@RequiredArgsConstructor
public class CaseAdminService {

    private static final Pattern CASE_CODE = Pattern.compile("^CASE-(\\d+)$");
    private static final Set<String> CRED_CODES = Set.of("A", "B", "C", "D");
    private static final List<CaseMetaVO.CredOption> CRED_OPTIONS = List.of(
            new CaseMetaVO.CredOption("A", "客户侧证实", "客户侧书面 / 口头确认，指标可追溯"),
            new CaseMetaVO.CredOption("B", "厂商具名", "厂商具名发布，或有客户官方联合确认"),
            new CaseMetaVO.CredOption("C", "厂商匿名", "厂商侧匿名发布，缺少可具名的一手确认"),
            new CaseMetaVO.CredOption("D", "二手转述", "媒体、二手资料或无法回溯的转述")
    );
    private static final int MAX_PAGE_SIZE = 100;

    private final FdeCaseMapper caseMapper;
    private final FdeCustomerMapper customerMapper;
    private final CaseContentCodec contentCodec;
    private final CustomerAdminService customerAdminService;
    private final TalentCaseLinkService talentCaseLinkService;

    public PageData<CaseAdminListItemVO> page(long page, long size, String q, String industry,
                                              String cred, Integer published) {
        String keyword = trimToNull(q);
        String industryValue = trimToNull(industry);
        String credValue = normalizeCred(trimToNull(cred));

        Page<FdeCase> result = caseMapper.selectPage(pageOf(page, size), Wrappers.<FdeCase>lambdaQuery()
                .and(keyword != null, wrapper -> wrapper
                        .like(FdeCase::getTitle, keyword)
                        .or()
                        .like(FdeCase::getPartner, keyword)
                        .or()
                        .like(FdeCase::getCode, keyword))
                .eq(industryValue != null, FdeCase::getIndustry, industryValue)
                .eq(credValue != null, FdeCase::getCred, credValue)
                .eq(published != null, FdeCase::getPublished, published)
                .orderByDesc(FdeCase::getUpdateTime));

        Map<Long, FdeCustomer> customers = loadCustomers(result.getRecords());
        List<CaseAdminListItemVO> records = result.getRecords().stream()
                .map(item -> new CaseAdminListItemVO(
                        item.getId(),
                        item.getCode(),
                        item.getTitle(),
                        item.getIndustry(),
                        customerName(item.getCustomerId() == null ? null : customers.get(item.getCustomerId())),
                        item.getCred(),
                        item.getPublished(),
                        contentCodec.readContent(item.getContentJson()).getMetrics().size(),
                        item.getUpdateTime()))
                .toList();
        return PageData.of(result, records);
    }

    public CaseAdminVO get(Long id) {
        return toVO(require(id));
    }

    /** 人才关联选择器：按编号/标题/交付方搜索，返回最多 20 条（含草稿）。 */
    public List<CaseOptionVO> options(String keyword) {
        String text = trimToNull(keyword);
        List<FdeCase> rows = caseMapper.selectList(Wrappers.<FdeCase>lambdaQuery()
                .select(FdeCase::getId, FdeCase::getCode, FdeCase::getTitle,
                        FdeCase::getIndustry, FdeCase::getPublished)
                .and(text != null, wrapper -> wrapper
                        .like(FdeCase::getCode, text)
                        .or()
                        .like(FdeCase::getTitle, text)
                        .or()
                        .like(FdeCase::getPartner, text))
                .orderByDesc(FdeCase::getUpdateTime)
                .last("LIMIT 20"));
        return rows.stream()
                .map(item -> new CaseOptionVO(
                        item.getId(),
                        item.getCode(),
                        item.getTitle(),
                        item.getIndustry(),
                        item.getPublished()))
                .toList();
    }

    @Transactional
    public CaseAdminVO create(CaseForm form) {
        Long customerId = resolveCustomer(form);
        FdeCase entity = new FdeCase();
        entity.setCode(trimToNull(form.code()) == null ? nextCode() : form.code().trim());
        apply(entity, form, customerId);
        // POST 一律写草稿，忽略请求里的 published。
        entity.setPublished(0);
        insertWithRetry(entity);
        return toVO(entity);
    }

    @Transactional
    public CaseAdminVO update(Long id, CaseForm form) {
        FdeCase entity = require(id);
        Long previousPrimaryFdeId = entity.getPrimaryFdeId();
        // PUT 忽略请求里的 code，一律用库里的值。
        Long customerId = resolveCustomer(form);
        apply(entity, form, customerId);
        // PUT 不改 published；显式刷新更新时间，保证列表按更新时间倒序符合预期。
        entity.setUpdateTime(LocalDateTime.now());
        caseMapper.updateById(entity);
        List<Long> affected = talentCaseLinkService.talentIdsForCase(id);
        affected.add(previousPrimaryFdeId);
        talentCaseLinkService.syncCaseCounts(affected);
        return toVO(entity);
    }

    @Transactional
    public void delete(Long id) {
        require(id);
        List<Long> affected = talentCaseLinkService.talentIdsForCase(id);
        caseMapper.deleteById(id);
        talentCaseLinkService.syncCaseCounts(affected);
    }

    @Transactional
    public CaseAdminVO clone(Long id) {
        FdeCase source = require(id);
        FdeCase copy = new FdeCase();
        copy.setCode(nextCode());
        copy.setTitle(appendCopySuffix(source.getTitle()));
        copy.setSubtitle(source.getSubtitle());
        copy.setIndustry(source.getIndustry());
        copy.setFunc(source.getFunc());
        copy.setOccurYear(source.getOccurYear());
        copy.setPartner(source.getPartner());
        copy.setCred(source.getCred());
        copy.setCredNote(source.getCredNote());
        copy.setFdeMode(source.getFdeMode());
        copy.setScale(source.getScale());
        copy.setModelPct(source.getModelPct());
        copy.setPain(source.getPain());
        copy.setGoal(source.getGoal());
        copy.setVerdict(source.getVerdict());
        copy.setTestimony(source.getTestimony());
        // 原样复制 content_json，_review 等预留键一并保留。
        copy.setContentJson(source.getContentJson());
        copy.setPrimaryFdeId(source.getPrimaryFdeId());
        copy.setContributorIds(source.getContributorIds());
        copy.setCustomerId(source.getCustomerId());
        copy.setPublished(0);
        insertWithRetry(copy);
        return toVO(copy);
    }

    @Transactional
    public CaseAdminVO publish(Long id) {
        FdeCase entity = require(id);
        CaseValidationVO validation = validate(toForm(entity), id);
        if (!validation.errors().isEmpty()) {
            String message = validation.errors().stream()
                    .map(CaseValidationVO.Issue::message)
                    .collect(Collectors.joining("；"));
            throw new BizException(422, message);
        }
        entity.setPublished(1);
        entity.setUpdateTime(LocalDateTime.now());
        caseMapper.updateById(entity);
        talentCaseLinkService.syncCaseCounts(talentCaseLinkService.talentIdsForCase(id));
        return toVO(entity);
    }

    @Transactional
    public CaseAdminVO unpublish(Long id) {
        FdeCase entity = require(id);
        entity.setPublished(0);
        entity.setUpdateTime(LocalDateTime.now());
        caseMapper.updateById(entity);
        talentCaseLinkService.syncCaseCounts(talentCaseLinkService.talentIdsForCase(id));
        return toVO(entity);
    }

    /**
     * 校验不落库。existingId 为空表示新建，需要检查编号唯一。
     */
    public CaseValidationVO validate(CaseForm form, Long existingId) {
        List<CaseValidationVO.Issue> errors = new ArrayList<>();
        List<CaseValidationVO.Issue> warnings = new ArrayList<>();
        if (form == null) {
            errors.add(new CaseValidationVO.Issue("basic", "form", "表单不能为空"));
            return new CaseValidationVO(false, errors, warnings);
        }

        List<CaseForm.MetricRow> metrics = structured(form.metrics()).stream()
                .filter(item -> hasText(item.name()) || hasText(item.definition()) || hasText(item.baseline())
                        || hasText(item.result()) || hasText(item.window()) || hasText(item.cred())
                        || item.sourceRef() != null)
                .toList();
        List<CaseForm.SourceRow> sources = structured(form.sources()).stream()
                .filter(item -> hasText(item.title()) || hasText(item.url()) || hasText(item.type()))
                .toList();
        List<CaseForm.TestimonyRow> testimony = structured(form.testimony()).stream()
                .filter(item -> hasText(item.quote()) || hasText(item.attribution()))
                .toList();

        String title = trimToNull(form.title());
        if (title == null) {
            errors.add(new CaseValidationVO.Issue("basic", "title", "标题不能为空"));
        } else if (title.length() > 200) {
            errors.add(new CaseValidationVO.Issue("basic", "title", "标题不能超过 200 字"));
        } else if (title.length() > 40) {
            warnings.add(new CaseValidationVO.Issue("basic", "title", "标题超过 40 字，列表卡片会截断"));
        }
        if (trimToNull(form.industry()) == null) {
            errors.add(new CaseValidationVO.Issue("basic", "industry", "行业不能为空"));
        }
        boolean hasCustomer = form.customerId() != null
                || (form.newCustomer() != null && trimToNull(form.newCustomer().displayName()) != null);
        if (!hasCustomer) {
            errors.add(new CaseValidationVO.Issue("basic", "customerId", "请选择客户或在案例内新建客户"));
        }
        BigDecimal modelPct = form.modelPct();
        if (modelPct != null) {
            if (modelPct.compareTo(BigDecimal.ZERO) < 0 || modelPct.compareTo(BigDecimal.valueOf(100)) > 0) {
                errors.add(new CaseValidationVO.Issue("basic", "modelPct", "模型含量需在 0–100 之间"));
            }
            if (modelPct.stripTrailingZeros().scale() > 1) {
                errors.add(new CaseValidationVO.Issue("basic", "modelPct", "模型含量最多保留一位小数"));
            }
        }
        if (existingId == null) {
            String requestedCode = trimToNull(form.code());
            if (requestedCode != null && caseMapper.selectAllCodes().contains(requestedCode)) {
                errors.add(new CaseValidationVO.Issue("basic", "code", "编号已存在"));
            }
        }

        String cred = normalizeCred(trimToNull(form.cred()));
        if (cred == null) {
            errors.add(new CaseValidationVO.Issue("credibility", "cred", "可信度需为 A/B/C/D"));
        }

        if (trimToNull(form.pain()) == null) {
            errors.add(new CaseValidationVO.Issue("narrative", "pain", "痛点不能为空"));
        }
        if (trimToNull(form.goal()) == null) {
            errors.add(new CaseValidationVO.Issue("narrative", "goal", "目标不能为空"));
        }

        if (metrics.isEmpty()) {
            errors.add(new CaseValidationVO.Issue("effects", "metrics", "至少需要一条指标"));
        }
        for (int i = 0; i < metrics.size(); i++) {
            CaseForm.MetricRow row = metrics.get(i);
            int index = i + 1;
            if (trimToNull(row.name()) == null) {
                errors.add(new CaseValidationVO.Issue("effects", "metrics[" + i + "].name",
                        "第 " + index + " 条指标缺少指标名"));
            }
            String metricCred = normalizeCred(trimToNull(row.cred()));
            if (metricCred == null) {
                errors.add(new CaseValidationVO.Issue("effects", "metrics[" + i + "].cred",
                        "第 " + index + " 条指标可信度需为 A/B/C/D"));
            }
            if (row.sourceRef() != null
                    && (row.sourceRef() < 1 || row.sourceRef() > sources.size())) {
                errors.add(new CaseValidationVO.Issue("effects", "metrics[" + i + "].sourceRef",
                        "第 " + index + " 条指标的来源序号超出范围"));
            }
            if (trimToNull(row.baseline()) == null) {
                warnings.add(new CaseValidationVO.Issue("effects", "metrics[" + i + "].baseline",
                        "第 " + index + " 条指标缺少基线"));
            }
            if (trimToNull(row.window()) == null) {
                warnings.add(new CaseValidationVO.Issue("effects", "metrics[" + i + "].window",
                        "第 " + index + " 条指标缺少时间窗"));
            }
        }
        List<CaseForm.KpiRow> kpis = structured(form.kpis()).stream()
                .filter(item -> hasText(item.v()) || hasText(item.k()))
                .toList();
        for (int i = 0; i < kpis.size(); i++) {
            if (kpis.get(i).m() == null) {
                warnings.add(new CaseValidationVO.Issue("effects", "kpis[" + i + "].m",
                        "第 " + (i + 1) + " 个 KPI 未关联指标行"));
            }
        }

        if (sources.isEmpty()) {
            errors.add(new CaseValidationVO.Issue("sources", "sources", "至少需要一条来源"));
        }
        for (int i = 0; i < sources.size(); i++) {
            String url = trimToNull(sources.get(i).url());
            if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
                errors.add(new CaseValidationVO.Issue("sources", "sources[" + i + "].url",
                        "第 " + (i + 1) + " 条来源的 URL 需以 http:// 或 https:// 开头"));
            }
        }
        if ("A".equals(cred) || "B".equals(cred)) {
            boolean hasTestimony = testimony.stream().anyMatch(item -> trimToNull(item.quote()) != null);
            boolean hasOfficialSource = sources.stream().anyMatch(item -> {
                String type = trimToNull(item.type());
                return type != null && (type.contains("一手") || type.contains("官方"));
            });
            if (!hasTestimony && !hasOfficialSource) {
                errors.add(new CaseValidationVO.Issue("sources", "testimony",
                        "可信度 A/B 级需至少一条证言，或一条类型含「一手」「官方」的来源"));
            }
        }
        addDomainWarnings(sources, form.partner(), warnings);

        return new CaseValidationVO(errors.isEmpty(), errors, warnings);
    }

    public CaseMetaVO meta() {
        List<FdeCase> cases = caseMapper.selectList(Wrappers.<FdeCase>lambdaQuery()
                .select(FdeCase::getId, FdeCase::getIndustry, FdeCase::getFunc, FdeCase::getContentJson));

        Map<String, Integer> industryCounts = new HashMap<>();
        Set<String> funcs = new LinkedHashSet<>();
        Set<String> tech = new LinkedHashSet<>();
        Set<String> ai = new LinkedHashSet<>();
        Set<String> mgmt = new LinkedHashSet<>();
        for (FdeCase item : cases) {
            String industry = trimToNull(item.getIndustry());
            if (industry != null) {
                industryCounts.merge(industry, 1, Integer::sum);
            }
            String func = trimToNull(item.getFunc());
            if (func != null) {
                funcs.add(func);
            }
            CaseContent content = contentCodec.readContent(item.getContentJson());
            addAll(tech, content.getTech());
            addAll(ai, content.getAi());
            addAll(mgmt, content.getMgmt());
        }
        List<String> industries = industryCounts.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .toList();

        return new CaseMetaVO(
                nextCode(),
                industries,
                new ArrayList<>(funcs),
                new CaseMetaVO.TagWords(new ArrayList<>(tech), new ArrayList<>(ai), new ArrayList<>(mgmt)),
                CRED_OPTIONS);
    }

    // ------------------------------------------------------------------
    // 表单 <-> 实体
    // ------------------------------------------------------------------

    private CaseForm toForm(FdeCase entity) {
        CaseContent content = contentCodec.readContent(entity.getContentJson());
        return new CaseForm(
                entity.getCode(),
                entity.getTitle(),
                entity.getSubtitle(),
                entity.getIndustry(),
                entity.getFunc(),
                entity.getOccurYear(),
                entity.getPartner(),
                entity.getCred(),
                entity.getCredNote(),
                entity.getFdeMode(),
                entity.getScale(),
                entity.getModelPct(),
                entity.getCustomerId(),
                null,
                entity.getPain(),
                entity.getGoal(),
                new ArrayList<>(content.getTech()),
                new ArrayList<>(content.getAi()),
                new ArrayList<>(content.getMgmt()),
                toKpis(content.getKpis()),
                toTimeline(content.getTimeline()),
                toOrg(content.getOrg()),
                toMetrics(content.getMetrics()),
                toCsf(content.getCsf()),
                toTestimony(contentCodec.readTestimony(entity.getTestimony())),
                toQuotes(content.getQuotes()),
                toSources(content.getSources()),
                toVerdict(entity.getVerdict()),
                entity.getPrimaryFdeId(),
                new ArrayList<>(contentCodec.readLongList(entity.getContributorIds())),
                entity.getPublished());
    }

    private void apply(FdeCase entity, CaseForm form, Long customerId) {
        // 保存前先把旧的 _review 取出来，写入新内容时原样透传。
        CaseContent previous = contentCodec.readContent(entity.getContentJson());

        entity.setTitle(nullToEmpty(form.title()));
        entity.setSubtitle(trimToNull(form.subtitle()));
        entity.setIndustry(trimToNull(form.industry()));
        entity.setFunc(trimToNull(form.func()));
        entity.setOccurYear(trimToNull(form.occurYear()));
        entity.setPartner(trimToNull(form.partner()));
        entity.setCred(normalizeCred(trimToNull(form.cred())));
        entity.setCredNote(trimToNull(form.credNote()));
        entity.setFdeMode(trimToNull(form.fdeMode()));
        entity.setScale(trimToNull(form.scale()));
        entity.setModelPct(form.modelPct());
        entity.setPain(trimToNull(form.pain()));
        entity.setGoal(trimToNull(form.goal()));
        entity.setCustomerId(customerId);
        entity.setPrimaryFdeId(form.primaryFdeId());
        List<Long> contributorIds = form.contributorIds() == null ? List.of() : form.contributorIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        entity.setContributorIds(contributorIds.isEmpty()
                ? null
                : contentCodec.writeJson(contributorIds, "协作 FDE 保存失败"));

        CaseContent content = new CaseContent();
        content.setTech(normalizeTags(form.tech()));
        content.setAi(normalizeTags(form.ai()));
        content.setMgmt(normalizeTags(form.mgmt()));
        content.setKpis(toContentKpis(form.kpis()));
        content.setTimeline(toTuples(form.timeline(), CaseForm.TimelineRow::stage, CaseForm.TimelineRow::note));
        content.setOrg(toTuples(form.org(), CaseForm.OrgRow::role, CaseForm.OrgRow::headcount,
                CaseForm.OrgRow::duty));
        content.setMetrics(toContentMetrics(form.metrics()));
        content.setCsf(toTuples(form.csf(), CaseForm.CsfRow::dimension, CaseForm.CsfRow::practice,
                CaseForm.CsfRow::reason, CaseForm.CsfRow::consequence));
        content.setQuotes(toTuples(form.quotes(), CaseForm.QuoteRow::text, CaseForm.QuoteRow::source));
        content.setSources(toTuples(form.sources(), CaseForm.SourceRow::title, CaseForm.SourceRow::url,
                CaseForm.SourceRow::type));
        content.setReview(previous.getReview());
        entity.setContentJson(contentCodec.writeContent(content));

        entity.setVerdict(form.verdict() == null ? null : toVerdictJson(form.verdict()));
        List<List<String>> testimony = toTuples(form.testimony(),
                CaseForm.TestimonyRow::quote, CaseForm.TestimonyRow::attribution);
        entity.setTestimony(testimony.isEmpty() ? null : contentCodec.writeJson(testimony, "证言保存失败"));
    }

    private Long resolveCustomer(CaseForm form) {
        if (form.newCustomer() != null && trimToNull(form.newCustomer().displayName()) != null) {
            FdeCustomer customer = customerAdminService.createOrReuse(
                    form.newCustomer().displayName(), form.newCustomer().isMasked());
            return customer.getId();
        }
        return form.customerId();
    }

    private void insertWithRetry(FdeCase entity) {
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                caseMapper.insert(entity);
                return;
            } catch (DuplicateKeyException ex) {
                if (attempt == 1) {
                    throw new BizException("编号冲突，请重试");
                }
                entity.setId(null);
                entity.setCode(nextCode());
            }
        }
        throw new BizException("编号冲突，请重试");
    }

    // ------------------------------------------------------------------
    // 编号 / 查询辅助
    // ------------------------------------------------------------------

    public String nextCode() {
        int max = 0;
        for (String code : caseMapper.selectAllCodes()) {
            if (code == null) {
                continue;
            }
            Matcher matcher = CASE_CODE.matcher(code.trim());
            if (matcher.matches()) {
                max = Math.max(max, Integer.parseInt(matcher.group(1)));
            }
        }
        return String.format("CASE-%03d", max + 1);
    }

    private FdeCase require(Long id) {
        FdeCase entity = id == null ? null : caseMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "案例不存在");
        }
        return entity;
    }

    private Map<Long, FdeCustomer> loadCustomers(List<FdeCase> cases) {
        Set<Long> ids = cases.stream()
                .map(FdeCase::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return customerMapper.selectList(Wrappers.<FdeCustomer>lambdaQuery()
                        .in(FdeCustomer::getId, ids))
                .stream()
                .collect(Collectors.toMap(FdeCustomer::getId, Function.identity(), (a, b) -> a));
    }

    private FdeCustomer loadCustomer(Long id) {
        return id == null ? null : customerMapper.selectById(id);
    }

    private String customerName(FdeCustomer customer) {
        return customer == null ? null : customer.getDisplayName();
    }

    private Page<FdeCase> pageOf(long page, long size) {
        long pageNo = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return new Page<>(pageNo, pageSize);
    }

    private CaseAdminVO toVO(FdeCase entity) {
        FdeCustomer customer = loadCustomer(entity.getCustomerId());
        return new CaseAdminVO(
                entity.getId(),
                toForm(entity),
                customerName(customer),
                customer == null ? null : customer.getIsMasked(),
                entity.getCreateTime(),
                entity.getUpdateTime());
    }

    // ------------------------------------------------------------------
    // 元组 <-> 表单行
    // ------------------------------------------------------------------

    private List<CaseForm.KpiRow> toKpis(List<CaseContent.Kpi> kpis) {
        return kpis.stream()
                .filter(Objects::nonNull)
                .map(item -> new CaseForm.KpiRow(item.getV(), item.getK(), item.getR(), item.getM()))
                .toList();
    }

    private List<CaseForm.TimelineRow> toTimeline(List<List<String>> tuples) {
        return tuples.stream()
                .map(row -> new CaseForm.TimelineRow(cell(row, 0), cell(row, 1)))
                .toList();
    }

    private List<CaseForm.OrgRow> toOrg(List<List<String>> tuples) {
        return tuples.stream()
                .map(row -> new CaseForm.OrgRow(cell(row, 0), cell(row, 1), cell(row, 2)))
                .toList();
    }

    private List<CaseForm.MetricRow> toMetrics(List<List<String>> tuples) {
        return tuples.stream()
                .map(row -> new CaseForm.MetricRow(
                        cell(row, 0), cell(row, 1), cell(row, 2), cell(row, 3), cell(row, 4), cell(row, 5),
                        parseInteger(cell(row, 6))))
                .toList();
    }

    private List<CaseForm.CsfRow> toCsf(List<List<String>> tuples) {
        return tuples.stream()
                .map(row -> new CaseForm.CsfRow(cell(row, 0), cell(row, 1), cell(row, 2), cell(row, 3)))
                .toList();
    }

    private List<CaseForm.TestimonyRow> toTestimony(List<List<String>> tuples) {
        return tuples.stream()
                .map(row -> new CaseForm.TestimonyRow(cell(row, 0), cell(row, 1)))
                .toList();
    }

    private List<CaseForm.QuoteRow> toQuotes(List<List<String>> tuples) {
        return tuples.stream()
                .map(row -> new CaseForm.QuoteRow(cell(row, 0), cell(row, 1)))
                .toList();
    }

    private List<CaseForm.SourceRow> toSources(List<List<String>> tuples) {
        return tuples.stream()
                .map(row -> new CaseForm.SourceRow(cell(row, 0), cell(row, 1), cell(row, 2)))
                .toList();
    }

    private CaseForm.VerdictInput toVerdict(String json) {
        Map<String, Object> values = contentCodec.readVerdict(json);
        return new CaseForm.VerdictInput(
                mapValue(values, "deposit"),
                mapValue(values, "modelPct"),
                mapValue(values, "reusable"),
                mapValue(values, "economics"),
                mapValue(values, "survive"),
                mapValue(values, "answer"));
    }

    private String toVerdictJson(CaseForm.VerdictInput verdict) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("deposit", nullToEmpty(verdict.deposit()));
        values.put("modelPct", nullToEmpty(verdict.modelPct()));
        values.put("reusable", nullToEmpty(verdict.reusable()));
        values.put("economics", nullToEmpty(verdict.economics()));
        values.put("survive", nullToEmpty(verdict.survive()));
        values.put("answer", nullToEmpty(verdict.answer()));
        boolean allBlank = values.values().stream().allMatch(value -> !StringUtils.hasText(value));
        return allBlank ? null : contentCodec.writeJson(values, "判断保存失败");
    }

    private List<CaseContent.Kpi> toContentKpis(List<CaseForm.KpiRow> rows) {
        List<CaseContent.Kpi> result = new ArrayList<>();
        for (CaseForm.KpiRow row : structured(rows)) {
            if (!hasText(row.v()) && !hasText(row.k())) {
                continue;
            }
            CaseContent.Kpi kpi = new CaseContent.Kpi();
            kpi.setV(trimToNull(row.v()));
            kpi.setK(trimToNull(row.k()));
            kpi.setR(row.r());
            kpi.setM(row.m());
            result.add(kpi);
        }
        return result;
    }

    private List<List<String>> toContentMetrics(List<CaseForm.MetricRow> rows) {
        List<List<String>> result = new ArrayList<>();
        for (CaseForm.MetricRow row : structured(rows)) {
            boolean blank = !hasText(row.name()) && !hasText(row.definition()) && !hasText(row.baseline())
                    && !hasText(row.result()) && !hasText(row.window()) && !hasText(row.cred())
                    && row.sourceRef() == null;
            if (blank) {
                continue;
            }
            List<String> tuple = new ArrayList<>(6);
            tuple.add(nullToEmpty(row.name()));
            tuple.add(nullToEmpty(row.definition()));
            tuple.add(nullToEmpty(row.baseline()));
            tuple.add(nullToEmpty(row.result()));
            tuple.add(nullToEmpty(row.window()));
            tuple.add(nullToEmpty(row.cred()));
            if (row.sourceRef() != null) {
                // 第 7 位 = 来源序号（1-based），前台只读 row[0..5]。
                tuple.add(String.valueOf(row.sourceRef()));
            }
            result.add(tuple);
        }
        return result;
    }

    @SafeVarargs
    private <T> List<List<String>> toTuples(List<T> rows, Function<T, String>... mappers) {
        List<List<String>> result = new ArrayList<>();
        for (T row : structured(rows)) {
            List<String> tuple = new ArrayList<>(mappers.length);
            boolean blank = true;
            for (Function<T, String> mapper : mappers) {
                String value = mapper.apply(row);
                if (hasText(value)) {
                    blank = false;
                }
                tuple.add(nullToEmpty(value));
            }
            if (!blank) {
                result.add(tuple);
            }
        }
        return result;
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static <T> List<T> structured(List<T> rows) {
        return rows == null ? List.of() : rows.stream().filter(Objects::nonNull).toList();
    }

    private static String cell(List<String> row, int index) {
        if (row == null || index >= row.size()) {
            return "";
        }
        return nullToEmpty(row.get(index));
    }

    private static String mapValue(Map<String, Object> values, String key) {
        return values == null ? "" : Objects.toString(values.get(key), "");
    }

    private static Integer parseInteger(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static void addAll(Set<String> target, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                target.add(value.trim());
            }
        }
    }

    // ------------------------------------------------------------------
    // 校验辅助
    // ------------------------------------------------------------------

    private void addDomainWarnings(List<CaseForm.SourceRow> sources, String partner,
                                   List<CaseValidationVO.Issue> warnings) {
        String partnerText = trimToNull(partner);
        if (partnerText == null) {
            return;
        }
        List<String> tokens = asciiWords(partnerText);
        if (tokens.isEmpty()) {
            return;
        }
        for (int i = 0; i < sources.size(); i++) {
            CaseForm.SourceRow source = sources.get(i);
            String type = trimToNull(source.type());
            if (type != null && (type.contains("一手") || type.contains("官方"))) {
                continue;
            }
            String host = hostOf(source.url());
            if (host == null) {
                continue;
            }
            boolean matched = tokens.stream().anyMatch(host::contains);
            if (!matched) {
                warnings.add(new CaseValidationVO.Issue("sources", "sources[" + i + "].url",
                        "第 " + (i + 1) + " 条来源域名与交付方可能不匹配"));
            }
        }
    }

    private List<String> asciiWords(String text) {
        List<String> words = new ArrayList<>();
        Matcher matcher = Pattern.compile("[A-Za-z]{3,}").matcher(text);
        while (matcher.find()) {
            words.add(matcher.group().toLowerCase(Locale.ROOT));
        }
        return words;
    }

    private String hostOf(String url) {
        String value = trimToNull(url);
        if (value == null) {
            return null;
        }
        try {
            String host = URI.create(value).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String normalizeCred(String cred) {
        if (!StringUtils.hasText(cred)) {
            return null;
        }
        String value = cred.trim().toUpperCase(Locale.ROOT);
        return CRED_CODES.contains(value) ? value : null;
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private static String appendCopySuffix(String title) {
        String suffix = "（副本）";
        String base = title == null ? "" : title;
        if (base.length() + suffix.length() <= 200) {
            return base + suffix;
        }
        return base.substring(0, 200 - suffix.length()) + suffix;
    }
}
