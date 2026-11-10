package com.fdelab.module.talent.dto;

import com.fdelab.module.talent.model.TalentProfile;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

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
    private Integer likeCount;
    /** 1 可接单 / 0 暂停接单 */
    private Integer accepting;
    /** 最近复核时间 */
    private LocalDate reviewTime;
    /** CFDE 等级：A / P / M */
    private String levelCode;
    /** 年资（年） */
    private Integer years;
    /** 可公开案例数 */
    private Integer caseCount;
    private TalentProfile profile;
}
