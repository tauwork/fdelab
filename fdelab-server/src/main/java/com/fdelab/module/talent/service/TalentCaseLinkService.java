package com.fdelab.module.talent.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.cases.entity.FdeCase;
import com.fdelab.module.cases.mapper.FdeCaseMapper;
import com.fdelab.module.talent.entity.FdeTalent;
import com.fdelab.module.talent.entity.FdeTalentCase;
import com.fdelab.module.talent.mapper.FdeTalentCaseMapper;
import com.fdelab.module.talent.mapper.FdeTalentMapper;
import com.fdelab.module.talent.vo.TalentLinkedCaseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 人才与标杆案例的关联维护。
 * <p>详情页展示口径 = 关联表（已发布案例）∪ primary_fde_id 命中的案例，按关联表 sort_no 优先排序。
 * case_count 始终按同一口径回写，避免列表数字与详情页卡片不一致。</p>
 */
@Service
@RequiredArgsConstructor
public class TalentCaseLinkService {

    private final FdeTalentCaseMapper linkMapper;
    private final FdeTalentMapper talentMapper;
    private final FdeCaseMapper caseMapper;

    public List<FdeCase> visibleCases(Long talentId) {
        return linkMapper.selectVisibleCases(talentId);
    }

    public List<TalentLinkedCaseVO> listLinks(Long talentId) {
        requireTalent(talentId);
        List<FdeTalentCase> links = linkMapper.selectList(Wrappers.<FdeTalentCase>lambdaQuery()
                .eq(FdeTalentCase::getTalentId, talentId)
                .orderByAsc(FdeTalentCase::getSortNo)
                .orderByAsc(FdeTalentCase::getId));
        if (links.isEmpty()) {
            return List.of();
        }
        List<Long> caseIds = links.stream().map(FdeTalentCase::getCaseId).toList();
        Map<Long, FdeCase> cases = caseMapper.selectBatchIds(caseIds).stream()
                .collect(Collectors.toMap(FdeCase::getId, Function.identity(), (left, right) -> left));
        List<TalentLinkedCaseVO> result = new ArrayList<>();
        for (FdeTalentCase link : links) {
            FdeCase item = cases.get(link.getCaseId());
            if (item == null) {
                continue;
            }
            result.add(new TalentLinkedCaseVO(
                    item.getId(),
                    item.getCode(),
                    item.getTitle(),
                    item.getIndustry(),
                    item.getPublished(),
                    link.getSortNo()));
        }
        return result;
    }

    /** 全量覆盖关联，数组顺序即 sort_no。 */
    @Transactional
    public List<TalentLinkedCaseVO> replaceLinks(Long talentId, List<Long> caseIds) {
        requireTalent(talentId);
        List<Long> ordered = caseIds == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(caseIds));
        ordered.removeIf(Objects::isNull);

        if (!ordered.isEmpty()) {
            Set<Long> found = caseMapper.selectBatchIds(ordered).stream()
                    .map(FdeCase::getId)
                    .collect(Collectors.toSet());
            if (found.size() != ordered.size()) {
                throw new BizException(422, "关联的案例不存在或已删除");
            }
        }

        linkMapper.delete(Wrappers.<FdeTalentCase>lambdaQuery()
                .eq(FdeTalentCase::getTalentId, talentId));
        for (int index = 0; index < ordered.size(); index++) {
            FdeTalentCase link = new FdeTalentCase();
            link.setTalentId(talentId);
            link.setCaseId(ordered.get(index));
            link.setSortNo(index);
            linkMapper.insert(link);
        }
        syncCaseCount(talentId);
        return listLinks(talentId);
    }

    public void syncCaseCount(Long talentId) {
        int count = (int) linkMapper.countVisibleCases(talentId);
        talentMapper.update(null, Wrappers.<FdeTalent>lambdaUpdate()
                .eq(FdeTalent::getId, talentId)
                .set(FdeTalent::getCaseCount, count));
    }

    /** 案例状态变化后，重算所有可能展示该案例的人才。 */
    public void syncCaseCounts(Collection<Long> talentIds) {
        if (talentIds == null) {
            return;
        }
        talentIds.stream().filter(Objects::nonNull).distinct().forEach(this::syncCaseCount);
    }

    /** 关联表 + primary_fde_id 指向该案例的人才 id。 */
    public List<Long> talentIdsForCase(Long caseId) {
        Set<Long> ids = new LinkedHashSet<>(linkMapper.selectTalentIdsByCase(caseId));
        FdeCase entity = caseId == null ? null : caseMapper.selectById(caseId);
        if (entity != null && entity.getPrimaryFdeId() != null) {
            ids.add(entity.getPrimaryFdeId());
        }
        return new ArrayList<>(ids);
    }

    private void requireTalent(Long talentId) {
        FdeTalent talent = talentId == null ? null : talentMapper.selectById(talentId);
        if (talent == null) {
            throw new BizException(404, "人才不存在");
        }
    }
}
