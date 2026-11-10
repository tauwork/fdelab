package com.fdelab.module.cases.admin.vo;

import java.util.List;

/**
 * 编辑页词表。词表从全部未删除案例（含草稿）聚合，行业按出现次数降序。
 */
public record CaseMetaVO(
        String nextCode,
        List<String> industries,
        List<String> funcs,
        TagWords tags,
        List<CredOption> creds
) {

    public record TagWords(List<String> tech, List<String> ai, List<String> mgmt) {
    }

    public record CredOption(String code, String label, String hint) {
    }
}
