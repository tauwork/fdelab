package com.fdelab.module.talent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fdelab.common.api.ApiResult;
import com.fdelab.common.api.PageData;
import com.fdelab.module.talent.dto.TalentCaseLinkRequest;
import com.fdelab.module.talent.dto.TalentSaveRequest;
import com.fdelab.module.talent.service.TalentCaseLinkService;
import com.fdelab.module.talent.service.TalentService;
import com.fdelab.module.talent.vo.TalentLinkedCaseVO;
import com.fdelab.module.talent.vo.TalentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;

@Tag(name = "人才管理")
@RestController
@RequestMapping("/api/system/talents")
@RequiredArgsConstructor
public class TalentAdminController {

    private final TalentService talentService;
    private final TalentCaseLinkService talentCaseLinkService;

    @Operation(summary = "人才分页")
    @GetMapping
    public ApiResult<PageData<TalentVO>> page(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String keyword) {
        return ApiResult.ok(talentService.pageAdmin(page, size, keyword));
    }

    @Operation(summary = "人才详情")
    @GetMapping("/{id}")
    public ApiResult<TalentVO> detail(@PathVariable Long id) {
        return ApiResult.ok(talentService.getAdmin(id));
    }

    @Operation(summary = "新建人才")
    @PostMapping
    public ApiResult<TalentVO> create(@Valid @RequestBody TalentSaveRequest request) {
        return ApiResult.ok(talentService.create(request));
    }

    @Operation(summary = "更新人才")
    @PutMapping("/{id}")
    public ApiResult<TalentVO> update(@PathVariable Long id, @Valid @RequestBody TalentSaveRequest request) {
        return ApiResult.ok(talentService.update(id, request));
    }

    @Operation(summary = "发布（仅 ADMIN）")
    @PostMapping("/{id}/publish")
    public ApiResult<TalentVO> publish(@PathVariable Long id) {
        StpUtil.checkRole("ADMIN");
        return ApiResult.ok(talentService.changePublished(id, 1));
    }

    @Operation(summary = "下架（仅 ADMIN）")
    @PostMapping("/{id}/unpublish")
    public ApiResult<TalentVO> unpublish(@PathVariable Long id) {
        StpUtil.checkRole("ADMIN");
        return ApiResult.ok(talentService.changePublished(id, 0));
    }

    @Operation(summary = "已关联的标杆案例")
    @GetMapping("/{id}/cases")
    public ApiResult<List<TalentLinkedCaseVO>> cases(@PathVariable Long id) {
        return ApiResult.ok(talentCaseLinkService.listLinks(id));
    }

    @Operation(summary = "全量覆盖关联案例，数组顺序即展示顺序")
    @PutMapping("/{id}/cases")
    public ApiResult<List<TalentLinkedCaseVO>> replaceCases(@PathVariable Long id,
                                                            @RequestBody TalentCaseLinkRequest request) {
        return ApiResult.ok(talentCaseLinkService.replaceLinks(id, request.getCaseIds()));
    }

    @Operation(summary = "删除人才")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        talentService.removeById(id);
        return ApiResult.ok(null);
    }
}
