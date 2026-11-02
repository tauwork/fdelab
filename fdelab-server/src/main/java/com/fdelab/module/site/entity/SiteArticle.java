package com.fdelab.module.site.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fdelab.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("site_article")
public class SiteArticle extends BaseEntity {

    private String title;

    private String summary;

    private String content;

    private String coverUrl;

    private Integer published;

    @TableLogic
    private Integer deleted;
}
