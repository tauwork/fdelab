package com.fdelab.module.talent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TalentLikeRequest {

    @NotBlank(message = "缺少访客标识")
    private String visitorKey;
}
