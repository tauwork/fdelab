package com.fdelab.module.talent.vo;

import java.util.List;

/**
 * FDE 名录页公开数据。
 * <p>stats 与 levels 是组织口径的全量数据，不随分页或筛选变化；
 * page.total 才是当前筛选条件下的命中总数。</p>
 */
public record TalentRosterVO(
        Stats stats,
        List<LevelVO> levels,
        com.fdelab.common.api.PageData<RosterItemVO> page
) {

    public record Stats(
            int talentCount,
            int levelCount,
            int industryCount,
            int standardCount
    ) {
    }

    public record LevelVO(
            String code,
            String label,
            String definition,
            int count
    ) {
    }
}
