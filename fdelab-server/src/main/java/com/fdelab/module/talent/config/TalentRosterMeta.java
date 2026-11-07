package com.fdelab.module.talent.config;

import java.util.List;

/**
 * 名录页的等级标尺与识别标准数量。
 * <p>这些是内容常量，不是业务数据，因此不建字典表。</p>
 */
public final class TalentRosterMeta {

    /** 原型统计带里的「3 识别标准」，只显示数字。 */
    public static final int STANDARD_COUNT = 3;

    /** 三级标尺，按 A / P / M 顺序返回。 */
    public static final List<LevelDefinition> LEVELS = List.of(
            new LevelDefinition(
                    "A",
                    "助理",
                    "通才型部署工程师。执行 connector / MCP 集成与新场景<span style=\"white-space:nowrap\">部署</span>。对应 OpenAI FDE「通才 + 领域专家混编」里的通才位：入职不必懂行业，靠嵌入与共建 eval 补齐。"
            ),
            new LevelDefinition(
                    "P",
                    "专业",
                    "方向型 FDE。某一或两个行业深耕，能独立判断模型能力边界、设计验证器、扛起一条业务链路的生产结果。"
            ),
            new LevelDefinition(
                    "M",
                    "大师",
                    "首席 FDE。能冷启动「无 ground truth」场景的评测体系，设计抽象触发机制，把一次性驻场变成可复制资产。"
            )
    );

    private TalentRosterMeta() {
    }

    public record LevelDefinition(String code, String label, String definition) {
    }
}
