package com.fdelab.module.talent.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdelab.common.api.PageData;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.talent.config.TalentProfileMeta;
import com.fdelab.module.talent.config.TalentProperties;
import com.fdelab.module.talent.config.TalentRosterMeta;
import com.fdelab.module.talent.dto.TalentSaveRequest;
import com.fdelab.module.talent.entity.FdeTalent;
import com.fdelab.module.talent.entity.FdeTalentLike;
import com.fdelab.module.talent.mapper.FdeTalentLikeMapper;
import com.fdelab.module.talent.mapper.FdeTalentMapper;
import com.fdelab.module.talent.model.TalentProfile;
import com.fdelab.module.talent.support.TalentSlugs;
import com.fdelab.module.talent.vo.ProjectVO;
import com.fdelab.module.talent.vo.RosterItemVO;
import com.fdelab.module.talent.vo.TalentLikeVO;
import com.fdelab.module.talent.vo.TalentProfileVO;
import com.fdelab.module.talent.vo.TalentRosterVO;
import com.fdelab.module.talent.vo.TalentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TalentService extends ServiceImpl<FdeTalentMapper, FdeTalent> {

    private final FdeTalentLikeMapper likeMapper;
    private final TalentProperties talentProperties;
    private final ObjectMapper objectMapper;

    public PageData<TalentVO> pagePublic(long page, long size, String visitorKey) {
        // 旧接口只返回旧名单（level_code IS NULL），保证小程序与旧个人页行为不变。
        Page<FdeTalent> result = page(pageOf(page, size), Wrappers.<FdeTalent>lambdaQuery()
                .eq(FdeTalent::getPublished, 1)
                .isNull(FdeTalent::getLevelCode)
                .orderByDesc(FdeTalent::getLikeCount)
                .orderByAsc(FdeTalent::getId));
        return toPage(result, visitorKey, false);
    }

    /**
     * 新版 FDE 名录：分页、关键词、等级筛选、SQL 排序。
     * <p>stats 与 levels 始终按全量已发布新名单计算，不随分页/筛选变化。</p>
     */
    public TalentRosterVO rosterList(long page, long size, String q, String level, String sort) {
        String keyword = StringUtils.hasText(q) ? q.trim() : null;
        String levelCode = normalizeLevelFilter(level);
        String sortKey = normalizeRosterSort(sort);

        Page<FdeTalent> result = baseMapper.selectRosterPage(pageOf(page, size), keyword, levelCode, sortKey);
        List<RosterItemVO> records = result.getRecords().stream()
                .map(this::toRosterItem)
                .toList();

        RosterStats stats = loadRosterStats();
        List<TalentRosterVO.LevelVO> levels = TalentRosterMeta.LEVELS.stream()
                .map(item -> new TalentRosterVO.LevelVO(
                        item.code(),
                        item.label(),
                        item.definition(),
                        stats.levelCounts().getOrDefault(item.code(), 0L).intValue()))
                .toList();

        return new TalentRosterVO(
                new TalentRosterVO.Stats(
                        (int) stats.talentCount(),
                        (int) stats.levelCount(),
                        (int) stats.industryCount(),
                        TalentRosterMeta.STANDARD_COUNT),
                levels,
                PageData.of(result, records));
    }

    public PageData<TalentVO> pageAdmin(long page, long size, String keyword) {
        String text = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<FdeTalent> result = page(pageOf(page, size), Wrappers.<FdeTalent>lambdaQuery()
                .and(text != null, wrapper -> wrapper
                        .like(FdeTalent::getName, text)
                        .or()
                        .like(FdeTalent::getSlug, text)
                        .or()
                        .like(FdeTalent::getHeadline, text))
                .orderByDesc(FdeTalent::getLikeCount)
                .orderByDesc(FdeTalent::getUpdateTime));
        return toPage(result, null, true);
    }

    public TalentVO getPublicBySlug(String slug, String visitorKey) {
        FdeTalent talent = getOne(Wrappers.<FdeTalent>lambdaQuery()
                .eq(FdeTalent::getSlug, slug)
                .eq(FdeTalent::getPublished, 1));
        if (talent == null) {
            throw new BizException(404, "还没有这位 FDE 的主页");
        }
        return toVo(talent, visitorKey, true);
    }

    public TalentProfileVO getProfileBySlug(String slug) {
        FdeTalent talent = getOne(Wrappers.<FdeTalent>lambdaQuery()
                .eq(FdeTalent::getSlug, slug)
                .eq(FdeTalent::getPublished, 1)
                .isNotNull(FdeTalent::getLevelCode));
        if (talent == null) {
            throw new BizException(404, "还没有这位 FDE 的主页");
        }
        String levelCode = normalizeLevelCode(talent.getLevelCode());
        if (levelCode == null) {
            throw new BizException(404, "还没有这位 FDE 的主页");
        }
        TalentProfile profile = readProfile(talent.getProfileJson());

        List<Integer> radar = validRadar(profile.getRadar())
                ? copy(profile.getRadar())
                : List.of();
        String strengthDim = "";
        String strengthText = "";
        if (!radar.isEmpty()) {
            int strongest = radar.indexOf(Collections.max(radar));
            strengthDim = TalentProfileMeta.RADAR_DIMS.get(strongest);
            strengthText = TalentProfileMeta.strengthText(strengthDim, radar.get(strongest), levelCode);
        }

        List<ProjectVO> projects = profile.getCases() == null ? List.of() : profile.getCases().stream()
                .filter(Objects::nonNull)
                .map(item -> new ProjectVO(item.getTitle(), item.getRole(), item.getDelivery()))
                .toList();

        return new TalentProfileVO(
                talent.getSlug(),
                talent.getName(),
                "CFDE-" + levelCode,
                TalentProfileMeta.levelName(levelCode),
                talent.getHeadline(),
                talent.getSummary(),
                talent.getYears(),
                copy(profile.getIndustryTags()),
                copy(profile.getSkills()),
                radar,
                projects,
                TalentProfileMeta.RADAR_DIMS,
                strengthText,
                strengthDim);
    }

    private boolean validRadar(List<Integer> radar) {
        if (radar == null || radar.size() != 5) {
            return false;
        }
        for (Integer value : radar) {
            if (value == null || value < 0 || value > 10) {
                return false;
            }
        }
        return true;
    }

    public TalentVO getAdmin(Long id) {
        return toVo(require(id), null, true);
    }

    @Transactional
    public TalentVO ingest(TalentSaveRequest request) {
        String slug = resolveSlug(request, null);
        FdeTalent existing = getOne(Wrappers.<FdeTalent>lambdaQuery().eq(FdeTalent::getSlug, slug));
        if (existing == null) {
            FdeTalent created = new FdeTalent();
            apply(created, request, slug, true);
            created.setLikeCount(0);
            created.setPublished(1);
            saveOrConflict(created);
            return toVo(created, null, true);
        }
        apply(existing, request, existing.getSlug(), false);
        updateById(existing);
        return toVo(existing, null, true);
    }

    @Transactional
    public TalentVO create(TalentSaveRequest request) {
        String slug = TalentSlugs.unique(resolveSlug(request, null), this::slugTaken);
        FdeTalent talent = new FdeTalent();
        apply(talent, request, slug, true);
        talent.setLikeCount(request.getLikeCount() == null ? 0 : Math.max(request.getLikeCount(), 0));
        talent.setPublished(request.getPublished() == null ? 1 : request.getPublished());
        saveOrConflict(talent);
        return toVo(talent, null, true);
    }

    @Transactional
    public TalentVO update(Long id, TalentSaveRequest request) {
        FdeTalent talent = require(id);
        String slug = TalentSlugs.unique(resolveSlug(request, talent.getSlug()), candidate ->
                slugTakenByOther(candidate, id));
        apply(talent, request, slug, true);
        if (request.getLikeCount() != null) {
            talent.setLikeCount(Math.max(request.getLikeCount(), 0));
        }
        if (request.getPublished() != null) {
            talent.setPublished(request.getPublished());
        }
        updateById(talent);
        return toVo(talent, null, true);
    }

    @Transactional
    public TalentLikeVO toggleLike(Long id, String visitorKey) {
        FdeTalent talent = getOne(Wrappers.<FdeTalent>lambdaQuery()
                .eq(FdeTalent::getId, id)
                .eq(FdeTalent::getPublished, 1));
        if (talent == null) {
            throw new BizException(404, "还没有这位 FDE 的主页");
        }
        String key = visitorKey.trim();
        if (key.length() > 64) {
            throw new BizException("访客标识过长");
        }
        FdeTalentLike existing = likeMapper.selectOne(Wrappers.<FdeTalentLike>lambdaQuery()
                .eq(FdeTalentLike::getTalentId, id)
                .eq(FdeTalentLike::getVisitorKey, key));
        int count = talent.getLikeCount() == null ? 0 : talent.getLikeCount();
        boolean liked;
        if (existing == null) {
            FdeTalentLike like = new FdeTalentLike();
            like.setTalentId(id);
            like.setVisitorKey(key);
            likeMapper.insert(like);
            count += 1;
            liked = true;
        } else {
            likeMapper.deleteById(existing.getId());
            count = Math.max(count - 1, 0);
            liked = false;
        }
        talent.setLikeCount(count);
        updateById(talent);
        return new TalentLikeVO(liked, count);
    }

    private void saveOrConflict(FdeTalent talent) {
        try {
            save(talent);
        } catch (DuplicateKeyException ex) {
            throw new BizException("子域名已被占用");
        }
    }

    private void apply(FdeTalent talent, TalentSaveRequest request, String slug, boolean assignSlug) {
        talent.setName(request.getName().trim());
        if (assignSlug) {
            talent.setSlug(slug);
        }
        talent.setHeadline(request.getHeadline().trim());
        talent.setSummary(StringUtils.hasText(request.getSummary()) ? request.getSummary().trim() : request.getHeadline().trim());
        talent.setAvatarUrl(blankToNull(request.getAvatarUrl()));
        talent.setPhone(blankToNull(request.getPhone()));
        talent.setWechat(blankToNull(request.getWechat()));
        talent.setWebsite(blankToNull(request.getWebsite()));
        talent.setWebsiteLabel(blankToNull(request.getWebsiteLabel()));
        talent.setLocation(blankToNull(request.getLocation()));
        // 旧版后台/上传请求不携带这三个字段，保留已有值，避免保存时清空人才名录数据
        if (request.getLevelCode() != null) {
            talent.setLevelCode(normalizeLevelCode(request.getLevelCode()));
        }
        if (request.getYears() != null) {
            talent.setYears(request.getYears());
        }
        if (request.getCaseCount() != null) {
            talent.setCaseCount(request.getCaseCount());
        }
        talent.setProfileJson(writeProfile(request.getProfile() == null ? new TalentProfile() : request.getProfile()));
    }

    private String normalizeLevelCode(String level) {
        if (!StringUtils.hasText(level)) {
            return null;
        }
        String value = level.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "A", "P", "M" -> value;
            default -> null;
        };
    }

    private String normalizeLevelFilter(String level) {
        String value = normalizeLevelCode(level);
        return value == null ? "all" : value;
    }

    private String normalizeRosterSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return "cases";
        }
        String value = sort.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "years", "level", "cases" -> value;
            default -> "cases";
        };
    }

    private RosterItemVO toRosterItem(FdeTalent talent) {
        TalentProfile profile = readProfile(talent.getProfileJson());
        return new RosterItemVO(
                talent.getSlug(),
                talent.getName(),
                talent.getLevelCode(),
                talent.getHeadline(),
                talent.getSummary(),
                copy(profile.getIndustryTags()),
                copy(profile.getSkills()),
                talent.getYears(),
                talent.getCaseCount() == null ? 0 : talent.getCaseCount());
    }

    /**
     * 全量统计口径：
     * <ul>
     *   <li>talentCount：已发布且 level_code 非空的记录数</li>
     *   <li>levelCount：上述记录里 distinct level_code 的数量</li>
     *   <li>industryCount：上述记录 profile_json.industryTags 去重，
     *       排除字面值「跨行业（通才）」——它表示通才位，不是一个行业</li>
     * </ul>
     * 注意：这是全量统计，不对当前页 records 去重，否则翻页时数字会变。
     */
    private RosterStats loadRosterStats() {
        List<FdeTalent> all = list(Wrappers.<FdeTalent>lambdaQuery()
                .select(FdeTalent::getLevelCode, FdeTalent::getProfileJson)
                .eq(FdeTalent::getPublished, 1)
                .isNotNull(FdeTalent::getLevelCode));

        Map<String, Long> levelCounts = new HashMap<>();
        Set<String> levels = new HashSet<>();
        Set<String> industries = new HashSet<>();
        for (FdeTalent talent : all) {
            String code = normalizeLevelCode(talent.getLevelCode());
            if (code != null) {
                levels.add(code);
                levelCounts.merge(code, 1L, Long::sum);
            }
            TalentProfile profile = readProfile(talent.getProfileJson());
            if (profile.getIndustryTags() == null) {
                continue;
            }
            for (String tag : profile.getIndustryTags()) {
                if (!StringUtils.hasText(tag)) {
                    continue;
                }
                String value = tag.trim();
                if (!"跨行业（通才）".equals(value)) {
                    industries.add(value);
                }
            }
        }
        return new RosterStats(all.size(), levels.size(), industries.size(), levelCounts);
    }

    private record RosterStats(long talentCount, long levelCount, long industryCount,
                               Map<String, Long> levelCounts) {
    }

    private String resolveSlug(TalentSaveRequest request, String current) {
        String requested = TalentSlugs.normalize(request.getSlug());
        if (StringUtils.hasText(requested)) {
            return requested;
        }
        if (StringUtils.hasText(current) && !StringUtils.hasText(request.getSlug())) {
            return current;
        }
        return TalentSlugs.fromName(request.getName());
    }

    private boolean slugTaken(String slug) {
        return count(Wrappers.<FdeTalent>lambdaQuery().eq(FdeTalent::getSlug, slug)) > 0;
    }

    private boolean slugTakenByOther(String slug, Long id) {
        return count(Wrappers.<FdeTalent>lambdaQuery()
                .eq(FdeTalent::getSlug, slug)
                .ne(FdeTalent::getId, id)) > 0;
    }

    private Page<FdeTalent> pageOf(long page, long size) {
        long pageNo = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), 100);
        return new Page<>(pageNo, pageSize);
    }

    private PageData<TalentVO> toPage(Page<FdeTalent> result, String visitorKey, boolean withContact) {
        Set<Long> likedIds = likedIds(result.getRecords(), visitorKey);
        List<TalentVO> records = result.getRecords().stream()
                .map(item -> toVo(item, likedIds.contains(item.getId()), withContact, false))
                .toList();
        return PageData.of(result, records);
    }

    private TalentVO toVo(FdeTalent talent, String visitorKey, boolean withContact) {
        boolean liked = StringUtils.hasText(visitorKey) && likeMapper.selectCount(Wrappers.<FdeTalentLike>lambdaQuery()
                .eq(FdeTalentLike::getTalentId, talent.getId())
                .eq(FdeTalentLike::getVisitorKey, visitorKey.trim())) > 0;
        return toVo(talent, liked, withContact, true);
    }

    private TalentVO toVo(FdeTalent talent, boolean liked, boolean withContact, boolean withProfile) {
        TalentProfile profile = withProfile
                ? readProfile(talent.getProfileJson())
                : readListProfile(talent.getProfileJson());
        return TalentVO.of(talent, talentProperties.homepage(talent.getSlug()), liked, profile, withContact);
    }

    private TalentProfile readListProfile(String json) {
        TalentProfile full = readProfile(json);
        TalentProfile summary = new TalentProfile();
        summary.setBadges(copy(full.getBadges()));
        summary.setIdentityTags(copy(full.getIdentityTags()));
        summary.setIndustryTags(copy(full.getIndustryTags()));
        summary.setDomains(copy(full.getDomains()));
        summary.setCases(full.getCases() == null ? List.of() : full.getCases().stream()
                .filter(Objects::nonNull)
                .map(TalentService::caseSummary)
                .toList());
        return summary;
    }

    private static <T> List<T> copy(List<T> source) {
        return source == null ? List.of() : new ArrayList<>(source);
    }

    private static TalentProfile.CaseItem caseSummary(TalentProfile.CaseItem item) {
        TalentProfile.CaseItem summary = new TalentProfile.CaseItem();
        summary.setIndustry(item.getIndustry());
        summary.setTitle(item.getTitle());
        summary.setBudget(item.getBudget());
        summary.setDuration(item.getDuration());
        summary.setPoints(null);
        return summary;
    }

    private Set<Long> likedIds(List<FdeTalent> talents, String visitorKey) {
        if (!StringUtils.hasText(visitorKey) || talents.isEmpty()) {
            return Set.of();
        }
        List<Long> ids = talents.stream().map(FdeTalent::getId).toList();
        return new HashSet<>(likeMapper.selectList(Wrappers.<FdeTalentLike>lambdaQuery()
                        .eq(FdeTalentLike::getVisitorKey, visitorKey.trim())
                        .in(FdeTalentLike::getTalentId, ids))
                .stream()
                .map(FdeTalentLike::getTalentId)
                .toList());
    }

    private FdeTalent require(Long id) {
        FdeTalent talent = getById(id);
        if (talent == null) {
            throw new BizException(404, "人才不存在");
        }
        return talent;
    }

    private TalentProfile readProfile(String json) {
        if (!StringUtils.hasText(json)) {
            return new TalentProfile();
        }
        try {
            TalentProfile profile = objectMapper.readValue(json, TalentProfile.class);
            return profile == null ? new TalentProfile() : profile;
        } catch (JsonProcessingException ex) {
            throw new BizException("人才档案解析失败");
        }
    }

    private String writeProfile(TalentProfile profile) {
        try {
            return objectMapper.writeValueAsString(profile);
        } catch (JsonProcessingException ex) {
            throw new BizException("人才档案保存失败");
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
