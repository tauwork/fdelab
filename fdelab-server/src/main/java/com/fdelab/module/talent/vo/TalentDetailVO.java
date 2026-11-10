package com.fdelab.module.talent.vo;

import java.time.LocalDate;
import java.util.List;

/**
 * 人才详情页（/talents/:slug）数据。
 * <p>sections 由后端按实际有数据的区块生成，前端按数组顺序渲染并生成编号，
 * 空区块天然跳过、编号不跳号。</p>
 */
public record TalentDetailVO(
        Long id,
        String slug,
        String name,
        String levelCode,
        String levelName,
        String headline,
        String summary,
        Integer years,
        Integer accepting,
        LocalDate reviewTime,
        Integer likeCount,
        Integer published,
        String avatarUrl,
        List<String> identityTags,
        List<String> industryTags,
        List<String> skills,
        List<String> badges,
        List<Integer> radar,
        List<String> radarDims,
        String strengthText,
        String strengthDim,
        List<RoleVO> roles,
        List<TextSectionVO> services,
        List<TextSectionVO> collaborations,
        List<ProjectVO> projects,
        List<TextSectionVO> experiences,
        List<TalentCaseCardVO> cases,
        List<SectionVO> sections
) {

    public record RoleVO(String org, String title, String description) {
    }

    public record TextSectionVO(String title, String description) {
    }

    public record SectionVO(String key, String title) {
    }
}
