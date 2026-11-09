package com.fdelab.module.cases.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fdelab.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 标杆案例。
 * <p>content_json 用 String 承接，统一由 CaseContentCodec 解析/序列化。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fde_case")
public class FdeCase extends BaseEntity {

    private String code;
    private String title;
    private String subtitle;
    private String industry;
    private String func;
    /** 发生时间，区间字符串；排序取前 4 位起始年，不按日期解析 */
    private String occurYear;
    /** 交付方 / FDE 主体，纯文本 */
    private String partner;
    /** A / B / C / D */
    private String cred;
    private String credNote;
    private String fdeMode;
    private String scale;
    /** 模型含量，DECIMAL(4,1)，不得用 TINYINT */
    private BigDecimal modelPct;
    private String pain;
    private String goal;
    /** 库里是 JSON 字符串，VO 层解析成对象 */
    private String verdict;
    /** 库里是 JSON 字符串，VO 层解析成数组 */
    private String testimony;
    /** tech / ai / mgmt / kpis / timeline / org / metrics / csf / quotes / sources */
    private String contentJson;
    private Long primaryFdeId;
    /** JSON 数字数组 */
    private String contributorIds;
    private Long customerId;
    private Integer published;

    @TableLogic
    private Integer deleted;
}
