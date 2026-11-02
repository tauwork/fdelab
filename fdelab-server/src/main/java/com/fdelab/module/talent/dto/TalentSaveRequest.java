package com.fdelab.module.talent.dto;

import com.fdelab.module.talent.model.TalentProfile;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TalentSaveRequest {

    @NotBlank(message = "请填写姓名")
    private String name;

    private String slug;

    @NotBlank(message = "请填写一句话介绍")
    private String headline;

    private String summary;
    private String avatarUrl;
    private String phone;
    private String wechat;
    private String website;
    private String websiteLabel;
    private String location;
    private Integer published;
    private Integer likeCount;
    private TalentProfile profile;
}
