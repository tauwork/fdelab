package com.fdelab.module.cases.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fdelab.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户。案例卡片与详情页的客户显示名由本表解析，不在 fde_case 里存冗余文本。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fde_customer")
public class FdeCustomer extends BaseEntity {

    private String code;
    private String displayName;
    /** 1 脱敏 / 0 具名 */
    private Integer isMasked;
    private Long primaryFdeId;
    /** JSON 数字数组 */
    private String serviceFdeIds;
    private Integer published;

    @TableLogic
    private Integer deleted;
}
