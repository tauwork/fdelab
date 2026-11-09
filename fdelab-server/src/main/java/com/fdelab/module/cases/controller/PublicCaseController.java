package com.fdelab.module.cases.controller;

import com.fdelab.common.api.ApiResult;
import com.fdelab.common.api.PageData;
import com.fdelab.module.cases.service.CaseService;
import com.fdelab.module.cases.vo.CaseDetailVO;
import com.fdelab.module.cases.vo.CaseFacetsVO;
import com.fdelab.module.cases.vo.CaseListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 案例库公开接口。
 * <p>二期后台接口固定走 /api/admin/cases + com.fdelab.module.cases.admin，本期不占用。</p>
 */
@Tag(name = "案例")
@RestController
@RequestMapping("/api/public/cases")
@RequiredArgsConstructor
public class PublicCaseController {

    private final CaseService caseService;

    @Operation(summary = "案例列表，分页 + 搜索 + 行业/可信度筛选")
    @GetMapping
    public ApiResult<PageData<CaseListItemVO>> list(
            @RequestParam(name = "page", defaultValue = "1") long page,
            @RequestParam(name = "size", defaultValue = "12") long size,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "industry", required = false) String industry,
            @RequestParam(name = "cred", required = false) String cred,
            @RequestParam(name = "sort", defaultValue = "code") String sort) {
        return ApiResult.ok(caseService.list(page, size, q, industry, cred, sort));
    }

    @Operation(summary = "案例筛选项（全量口径）")
    @GetMapping("/facets")
    public ApiResult<CaseFacetsVO> facets() {
        return ApiResult.ok(caseService.facets());
    }

    @Operation(summary = "案例详情，按对外编号 code 查询")
    @GetMapping("/{code}")
    public ApiResult<CaseDetailVO> detail(@PathVariable String code) {
        return ApiResult.ok(caseService.detail(code));
    }
}
