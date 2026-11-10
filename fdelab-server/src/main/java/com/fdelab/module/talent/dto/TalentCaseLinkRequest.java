package com.fdelab.module.talent.dto;

import lombok.Data;

import java.util.List;

@Data
public class TalentCaseLinkRequest {

    /** 全量覆盖；数组顺序即详情页展示顺序。 */
    private List<Long> caseIds;
}
