package com.fdelab.module.cases.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fdelab.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 标杆案例。
 * <p>content_json 用 String 承接，统一由 CaseContentCodec 解析/序列化。</p>
 * <p>后台编辑允许清空字段，因此可空列使用 {@link FieldStrategy#ALWAYS}：
 * 否则 MyBatis-Plus 默认的 NOT_NULL 策略会跳过 null，导致「清空后保存不生效」。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fde_case")
public class FdeCase extends BaseEntity {

    private String code;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String title;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String subtitle;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String industry;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String func;
    /** 发生时间，区间字符串；排序取前 4 位起始年，不按日期解析 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String occurYear;
    /** 交付方 / FDE 主体，纯文本 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String partner;
    /** A / B / C / D */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String cred;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String credNote;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String fdeMode;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String scale;
    /** 模型含量，DECIMAL(4,1)，不得用 TINYINT */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private BigDecimal modelPct;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String pain;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String goal;
    /** 库里是 JSON 字符串，VO 层解析成对象 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String verdict;
    /** 库里是 JSON 字符串，VO 层解析成数组 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String testimony;
    /** tech / ai / mgmt / kpis / timeline / org / metrics / csf / quotes / sources */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String contentJson;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long primaryFdeId;
    /** JSON 数字数组 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String contributorIds;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long customerId;
    private Integer published;

    @TableLogic
    private Integer deleted;
}
