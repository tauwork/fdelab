package com.fdelab.module.talent.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fdelab.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fde_talent")
public class FdeTalent extends BaseEntity {

    private String name;
    private String slug;
    private String headline;
    private String summary;
    private String avatarUrl;
    private String phone;
    private String wechat;
    private String website;
    private String websiteLabel;
    private String location;
    private Integer likeCount;
    private Integer published;

    private String profileJson;

    @TableLogic
    private Integer deleted;
}
