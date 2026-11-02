package com.fdelab.module.talent.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TalentProfile {

    private List<String> badges = new ArrayList<>();
    private List<String> identityTags = new ArrayList<>();
    private List<String> industryTags = new ArrayList<>();
    private List<RoleItem> roles = new ArrayList<>();
    private List<TextItem> experiences = new ArrayList<>();
    private List<TextItem> services = new ArrayList<>();
    private List<TextItem> collaborations = new ArrayList<>();
    private List<CaseItem> cases = new ArrayList<>();

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
        private List<String> points = new ArrayList<>();
        private String delivery;
    }
}
