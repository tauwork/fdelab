package com.fdelab.module.product.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fdelab.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品条目。
 * <p>detail 是详情页内容文档，不是 schema：以 JSON 字符串承接，后端只校验合法 JSON 对象，
 * 不展开成 VO 字段。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fde_product_item")
public class FdeProductItem extends BaseEntity {

    private String code;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long productId;
    /** m / de / ag / sa / vs / tr / pk */
    private String kind;
    private String name;
    /** on / beta / dev */
    private String status;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String summary;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String cycle;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String pricing;
    /** 详情页内容（键名由详情页设计定稿后登记） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String detail;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer sortOrder;
    private Integer published;

    @TableLogic
    private Integer deleted;
}
