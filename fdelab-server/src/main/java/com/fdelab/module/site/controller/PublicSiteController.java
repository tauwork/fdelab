package com.fdelab.module.site.controller;

import com.fdelab.common.api.ApiResult;
import com.fdelab.common.config.SiteProperties;
import com.fdelab.module.site.service.SiteArticleService;
import com.fdelab.module.site.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "展示站")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicSiteController {

    private final SiteProperties siteProperties;
    private final SiteArticleService siteArticleService;

    @Operation(summary = "站点资料")
    @GetMapping("/profile")
    public ApiResult<SiteProperties> profile() {
        return ApiResult.ok(siteProperties);
    }

    @Operation(summary = "已发布动态")
    @GetMapping("/articles")
    public ApiResult<List<ArticleVO>> articles() {
        return ApiResult.ok(siteArticleService.listPublished());
    }

    @Operation(summary = "动态详情")
    @GetMapping("/articles/{id}")
    public ApiResult<ArticleVO> article(@PathVariable Long id) {
        return ApiResult.ok(siteArticleService.getPublished(id));
    }
}
