package com.fdelab.module.cases.admin.vo;

import java.util.List;

/**
 * 校验结果。error 阻断发布，warning 只提示。
 */
public record CaseValidationVO(
        boolean ok,
        List<Issue> errors,
        List<Issue> warnings
) {

    public record Issue(String section, String field, String message) {
    }
}
