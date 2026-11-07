package com.fdelab.module.talent.vo;

import java.util.List;

/**
 * 名录卡片/表格行。
 * <p>刻意不包含 phone / wechat / website / likeCount / liked / homepageUrl / avatar，
 * 公开名录页不输出联系方式，头像由前端从 name 取首字。</p>
 */
public record RosterItemVO(
        String slug,
        String name,
        String level,
        String role,
        String bio,
        List<String> industries,
        List<String> skills,
        Integer years,
        Integer caseCount
) {
}
