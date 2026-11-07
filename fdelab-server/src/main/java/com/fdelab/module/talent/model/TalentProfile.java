package com.fdelab.module.talent.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TalentProfile {

    private List<String> badges = new ArrayList<>();
    private List<String> identityTags = new ArrayList<>();
    private List<String> industryTags = new ArrayList<>();
    @JsonAlias("domainTags")
    private List<String> domains = new ArrayList<>();
    private List<RoleItem> roles = new ArrayList<>();
    private List<TextItem> experiences = new ArrayList<>();
    private List<TextItem> services = new ArrayList<>();
    private List<TextItem> collaborations = new ArrayList<>();
    private List<CaseItem> cases = new ArrayList<>();
    /** 名录卡片下行技能标签 */
    private List<String> skills = new ArrayList<>();
    /** 五维评分，顺序与 RADAR_DIMS 一致：工程实现 / 业务翻译 / 评测设计 / 抽象复用 / 现场推进 */
    private List<Integer> radar = new ArrayList<>();

    @Data
    public static class RoleItem {
        private String org;
        private String title;
        private String description;
    }

    @Data
    public static class TextItem {
        private String title;
        private String description;
    }

    @Data
    public static class CaseItem {
        private String industry;
        private String title;
        private String portrait;
        private String budget;
        private String duration;
        private List<String> points = new ArrayList<>();
        private String delivery;
        /** 项目三元组里的「角色」，旧 CaseItem 没有 */
        private String role;
    }
}
