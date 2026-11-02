package com.fdelab.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fdelab.site")
public class SiteProperties {

    private String name;
    private String slogan;
    private String intro;
}
