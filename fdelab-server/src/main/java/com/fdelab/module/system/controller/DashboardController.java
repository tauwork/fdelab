package com.fdelab.module.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fdelab.common.api.ApiResult;
import com.fdelab.module.site.entity.SiteArticle;
import com.fdelab.module.site.service.SiteArticleService;
import com.fdelab.module.system.service.SysUserService;
import com.fdelab.module.talent.entity.FdeTalent;
import com.fdelab.module.talent.service.TalentService;
import com.fdelab.module.system.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "概览")
@RestController
@RequestMapping("/api/system/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserService sysUserService;
    private final SiteArticleService siteArticleService;
    private final TalentService talentService;

    @Operation(summary = "后台概览")
    @GetMapping
    public ApiResult<DashboardVO> dashboard() {
        long articles = siteArticleService.count(Wrappers.<SiteArticle>lambdaQuery()
                .eq(SiteArticle::getPublished, 1));
        long talents = talentService.count(Wrappers.<FdeTalent>lambdaQuery()
                .eq(FdeTalent::getPublished, 1));
        return ApiResult.ok(new DashboardVO(sysUserService.count(), articles, talents));
    }
}
