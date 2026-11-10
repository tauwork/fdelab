package com.fdelab.module.talent.service;

import com.fdelab.module.cases.entity.FdeCase;
import com.fdelab.module.cases.model.CaseContent;
import com.fdelab.module.cases.service.CaseContentCodec;
import com.fdelab.module.talent.vo.TalentCaseCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 把标杆案例实体组装成人才详情页的案例卡。
 * <p>所有展示文本都取自 fde_case 或 content_json 原文，只做结构拼接，不改写内容。</p>
 */
@Component
@RequiredArgsConstructor
public class TalentCaseCardAssembler {

    private static final int MAX_POINTS = 5;

    private final CaseContentCodec contentCodec;

    public TalentCaseCardVO toCard(FdeCase entity) {
        CaseContent content = contentCodec.readContent(entity.getContentJson());
        return new TalentCaseCardVO(
                entity.getId(),
                entity.getCode(),
                joinTitle(entity.getTitle(), entity.getSubtitle()),
                entity.getSubtitle(),
                entity.getIndustry(),
                entity.getPain(),
                points(content),
                delivery(content, entity.getVerdict()),
                entity.getCred(),
                entity.getCredNote(),
                "/articles/" + entity.getCode());
    }

    private String joinTitle(String title, String subtitle) {
        String base = StringUtils.hasText(title) ? title.trim() : "";
        if (!StringUtils.hasText(subtitle)) {
            return base;
        }
        return base.isEmpty() ? subtitle.trim() : base + "：" + subtitle.trim();
    }

    private List<String> points(CaseContent content) {
        List<String> timeline = content.getTimeline().stream()
                .limit(MAX_POINTS)
                .map(row -> pair(cell(row, 0), cell(row, 1), " — "))
                .filter(StringUtils::hasText)
                .toList();
        if (!timeline.isEmpty()) {
            return timeline;
        }
        return content.getCsf().stream()
                .limit(MAX_POINTS)
                .map(row -> pair(cell(row, 0), cell(row, 1), " — "))
                .filter(StringUtils::hasText)
                .toList();
    }

    private String delivery(CaseContent content, String verdictJson) {
        List<String> parts = new ArrayList<>();
        String metrics = content.getKpis().stream()
                .filter(kpi -> Boolean.TRUE.equals(kpi.getR()))
                .map(kpi -> pair(kpi.getK(), kpi.getV(), " "))
                .filter(StringUtils::hasText)
                .reduce((left, right) -> left + "、" + right)
                .orElse("");
        if (StringUtils.hasText(metrics)) {
            parts.add(metrics);
        }
        Map<String, Object> verdict = contentCodec.readVerdict(verdictJson);
        String answer = verdict == null ? "" : Objects.toString(verdict.get("answer"), "").trim();
        if (StringUtils.hasText(answer)) {
            parts.add(answer);
        }
        return String.join("；", parts);
    }

    private static String pair(String left, String right, String separator) {
        String first = StringUtils.hasText(left) ? left.trim() : "";
        String second = StringUtils.hasText(right) ? right.trim() : "";
        if (first.isEmpty()) {
            return second;
        }
        return second.isEmpty() ? first : first + separator + second;
    }

    private static String cell(List<String> row, int index) {
        if (row == null || index >= row.size()) {
            return "";
        }
        return row.get(index);
    }
}
