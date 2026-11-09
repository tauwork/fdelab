package com.fdelab.module.talent.vo;

import java.util.List;

public record TalentProfileVO(
        String slug,
        String name,
        String level,
        String levelName,
        String headline,
        String summary,
        Integer years,
        List<String> industries,
        List<String> skills,
        List<Integer> radar,
        List<ProjectVO> projects,
        List<String> radarDims,
        String strengthText,
        String strengthDim
) {
}
