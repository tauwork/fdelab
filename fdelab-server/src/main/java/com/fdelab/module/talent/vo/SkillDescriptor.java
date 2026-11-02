package com.fdelab.module.talent.vo;

public record SkillDescriptor(
        String name,
        String title,
        String description,
        String documentPath,
        String ingestPath
) {
}
