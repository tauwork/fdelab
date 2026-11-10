package com.fdelab.module.cases.admin.controller;

import com.fdelab.common.api.ApiResult;
import com.fdelab.common.api.PageData;
import com.fdelab.module.cases.admin.dto.CaseForm;
import com.fdelab.module.cases.admin.service.CaseAdminService;
import com.fdelab.module.cases.admin.vo.CaseAdminListItemVO;
import com.fdelab.module.cases.admin.vo.CaseAdminVO;
import com.fdelab.module.cases.admin.vo.CaseMetaVO;
import com.fdelab.module.cases.admin.vo.CaseValidationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "案例管理")
@RestController
@RequestMapping("/api/admin/cases")
@RequiredArgsConstructor
public class CaseAdminController {

    private final CaseAdminService caseAdminService;

    @Operation(summary = "案例分页")
    @GetMapping
    public ApiResult<PageData<CaseAdminListItemVO>> page(@RequestParam(name = "page", defaultValue = "1") long page,
                                                         @RequestParam(name = "size", defaultValue = "10") long size,
                                                         @RequestParam(name = "q", required = false) String q,
                                                         @RequestParam(name = "industry", required = false) String industry,
                                                         @RequestParam(name = "cred", required = false) String cred,
                                                         @RequestParam(name = "published", required = false) Integer published) {
        return ApiResult.ok(caseAdminService.page(page, size, q, industry, cred, published));
    }

    @Operation(summary = "案例词表与下一个编号")
    @GetMapping("/meta")
    public ApiResult<CaseMetaVO> meta() {
        return ApiResult.ok(caseAdminService.meta());
    }

    @Operation(summary = "案例详情")
    @GetMapping("/{id}")
    public ApiResult<CaseAdminVO> detail(@PathVariable("id") Long id) {
        return ApiResult.ok(caseAdminService.get(id));
    }

    @Operation(summary = "新建草稿")
    @PostMapping
    public ApiResult<CaseAdminVO> create(@RequestBody CaseForm form) {
        return ApiResult.ok(caseAdminService.create(form));
    }

    @Operation(summary = "保存")
    @PutMapping("/{id}")
    public ApiResult<CaseAdminVO> update(@PathVariable("id") Long id, @RequestBody CaseForm form) {
        return ApiResult.ok(caseAdminService.update(id, form));
    }

    @Operation(summary = "软删")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        caseAdminService.delete(id);
        return ApiResult.ok(null);
    }

    @Operation(summary = "复制为新草稿")
    @PostMapping("/{id}/clone")
    public ApiResult<CaseAdminVO> clone(@PathVariable("id") Long id) {
        return ApiResult.ok(caseAdminService.clone(id));
    }

    @Operation(summary = "发布")
    @PostMapping("/{id}/publish")
    public ApiResult<CaseAdminVO> publish(@PathVariable("id") Long id) {
        // TODO 二期：角色鉴权（仅 ADMIN 可发布）。本期不写后端角色校验。
        return ApiResult.ok(caseAdminService.publish(id));
    }

    @Operation(summary = "下架")
    @PostMapping("/{id}/unpublish")
    public ApiResult<CaseAdminVO> unpublish(@PathVariable("id") Long id) {
        return ApiResult.ok(caseAdminService.unpublish(id));
    }

    @Operation(summary = "校验，不落库")
    @PostMapping("/validate")
    public ApiResult<CaseValidationVO> validate(@RequestBody CaseForm form,
                                                @RequestParam(name = "id", required = false) Long id) {
        return ApiResult.ok(caseAdminService.validate(form, id));
    }
}
