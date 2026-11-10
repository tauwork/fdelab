package com.fdelab.module.talent.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fdelab.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

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
    /** 1 可接单 / 0 暂停接单 */
    private Integer accepting;
    /** 最近复核时间，NULL 时详情页不展示该行 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate reviewTime;

    /**
     * CFDE 等级：A / P / M；旧数据为 NULL，同时作为新旧名单分界。
     * <p>ALWAYS 让后台把等级改成「未设置」时真能写回 NULL，否则名录仍按旧等级展示。</p>
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String levelCode;

    /** 年资（年） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer years;
    /** 可公开案例数 */
    private Integer caseCount;

    private String profileJson;

    @TableLogic
    private Integer deleted;
}
