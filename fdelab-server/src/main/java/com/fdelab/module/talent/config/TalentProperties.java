package com.fdelab.module.talent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fdelab.talent")
public class TalentProperties {

    private String domain = "fdelab.io";

    private String skillKey = "fdelab-dev-skill-key";

    public String homepage(String slug) {
        return "https://" + slug + "." + domain;
    }
}
