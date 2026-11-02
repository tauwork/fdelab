package com.fdelab.module.talent.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdelab.common.api.PageData;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.talent.config.TalentProperties;
import com.fdelab.module.talent.dto.TalentSaveRequest;
import com.fdelab.module.talent.entity.FdeTalent;
import com.fdelab.module.talent.entity.FdeTalentLike;
import com.fdelab.module.talent.mapper.FdeTalentLikeMapper;
import com.fdelab.module.talent.mapper.FdeTalentMapper;
import com.fdelab.module.talent.model.TalentProfile;
import com.fdelab.module.talent.support.TalentSlugs;
import com.fdelab.module.talent.vo.TalentLikeVO;
import com.fdelab.module.talent.vo.TalentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TalentService extends ServiceImpl<FdeTalentMapper, FdeTalent> {

    private final FdeTalentLikeMapper likeMapper;
    private final TalentProperties talentProperties;
    private final ObjectMapper objectMapper;

    public PageData<TalentVO> pagePublic(long page, long size, String visitorKey) {
        Page<FdeTalent> result = page(pageOf(page, size), Wrappers.<FdeTalent>lambdaQuery()
                .eq(FdeTalent::getPublished, 1)
                .orderByDesc(FdeTalent::getLikeCount)
                .orderByAsc(FdeTalent::getId));
        return toPage(result, visitorKey, false);
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
        talent.setProfileJson(writeProfile(request.getProfile() == null ? new TalentProfile() : request.getProfile()));
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
        TalentProfile profile = withProfile ? readProfile(talent.getProfileJson()) : new TalentProfile();
        if (!withProfile) {
            profile.setBadges(readProfile(talent.getProfileJson()).getBadges());
        }
        return TalentVO.of(talent, talentProperties.homepage(talent.getSlug()), liked, profile, withContact);
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
