package com.fdelab.module.talent.config;

import java.util.List;
import java.util.Map;

/**
 * 人才详情页的内容常量：五维名称、等级显示名、派驻形态与强项文案模板。
 * <p>这些是页面文案，不是业务数据，因此不建字典表、不写进 profile_json。</p>
 */
public final class TalentProfileMeta {

    /** 顺序与 profile_json.radar 一致，不可调整。 */
    public static final List<String> RADAR_DIMS = List.of(
            "工程实现", "业务翻译", "评测设计", "抽象复用", "现场推进");

    private static final Map<String, String> LEVEL_NAMES = Map.of(
            "M", "大师级",
            "P", "专业级",
            "A", "助理级");

    private static final Map<String, String> POSTS = Map.of(
            "M", "首席 / 专家位",
            "P", "方向负责人位",
            "A", "通才执行位");

    private TalentProfileMeta() {
    }

    public static String levelName(String levelCode) {
        return LEVEL_NAMES.getOrDefault(levelCode, "");
    }

    public static String strengthText(String dimension, int score, String levelCode) {
        String post = POSTS.getOrDefault(levelCode, "");
        String base = "五维中最强的是 " + dimension + "（" + score + "/10）。";
        if (post.isEmpty()) {
            return base;
        }
        return base + "典型派驻形态：「通才 + 领域专家混编」团队中的 " + post
                + "，对一条业务链路的生产结果负责。";
    }
}
