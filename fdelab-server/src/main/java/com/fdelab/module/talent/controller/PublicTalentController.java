package com.fdelab.module.talent.controller;

import com.fdelab.common.api.ApiResult;
import com.fdelab.common.api.PageData;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.talent.config.TalentProperties;
import com.fdelab.module.talent.dto.TalentLikeRequest;
import com.fdelab.module.talent.dto.TalentSaveRequest;
import com.fdelab.module.talent.service.TalentService;
import com.fdelab.module.talent.vo.SkillDescriptor;
import com.fdelab.module.talent.vo.TalentLikeVO;
import com.fdelab.module.talent.vo.TalentProfileVO;
import com.fdelab.module.talent.vo.TalentRosterVO;
import com.fdelab.module.talent.vo.TalentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "人才")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicTalentController {

    private final TalentService talentService;
    private final TalentProperties talentProperties;

    @Operation(summary = "FDE 列表，按点赞从高到低")
    @GetMapping("/talents")
    public ApiResult<PageData<TalentVO>> page(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "20") long size,
                                               @RequestParam(required = false) String visitorKey) {
        return ApiResult.ok(talentService.pagePublic(page, size, visitorKey));
    }

    @Operation(summary = "FDE 名录，分页 + 筛选 + 排序")
    @GetMapping("/talents/roster")
    public ApiResult<TalentRosterVO> roster(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "12") long size,
                                             @RequestParam(required = false) String q,
                                             @RequestParam(defaultValue = "all") String level,
                                             @RequestParam(defaultValue = "cases") String sort) {
        return ApiResult.ok(talentService.rosterList(page, size, q, level, sort));
    }

    @Operation(summary = "按子域名读取个人主页")
    @GetMapping("/talents/slug/{slug}")
    public ApiResult<TalentVO> bySlug(@PathVariable String slug,
                                       @RequestParam(required = false) String visitorKey) {
        return ApiResult.ok(talentService.getPublicBySlug(slug, visitorKey));
    }

    @Operation(summary = "FDE 详情页")
    @GetMapping("/talents/profile/{slug}")
    public ApiResult<TalentProfileVO> profile(@PathVariable String slug) {
        return ApiResult.ok(talentService.getProfileBySlug(slug));
    }

    @Operation(summary = "点赞或取消点赞")
    @PostMapping("/talents/{id}/like")
    public ApiResult<TalentLikeVO> like(@PathVariable Long id, @Valid @RequestBody TalentLikeRequest request) {
        return ApiResult.ok(talentService.toggleLike(id, request.getVisitorKey()));
    }

    @Operation(summary = "技能库上传或更新个人主页")
    @PostMapping("/talents/ingest")
    public ApiResult<TalentVO> ingest(@RequestHeader(value = "X-Fdelab-Skill-Key", required = false) String skillKey,
                                       @Valid @RequestBody TalentSaveRequest request) {
        if (skillKey == null || !skillKey.equals(talentProperties.getSkillKey())) {
            throw new BizException(401, "技能密钥不正确");
        }
        return ApiResult.ok(talentService.ingest(request));
    }

    @Operation(summary = "技能库目录")
    @GetMapping("/skills")
    public ApiResult<List<SkillDescriptor>> skills() {
        return ApiResult.ok(List.of(new SkillDescriptor(
                "fde-profile",
                "FDE 个人简介",
                "根据交谈内容生成个人简介，并上传到 FDELab，自动分配子域名主页。",
                "/api/public/skills/fde-profile",
                "/api/public/talents/ingest")));
    }

    @Operation(summary = "FDE 个人简介技能说明")
    @GetMapping(value = "/skills/fde-profile", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public String skillDocument() throws IOException {
        ClassPathResource resource = new ClassPathResource("skills/fde-profile/SKILL.md");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
