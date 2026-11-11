package com.fdelab.module.product.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fdelab.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品运作（4 条，结构恒定）。
 * <p>case_ids / customer_ids / deliverables 以 JSON 字符串承接，读写统一收口在 ItemAssembler。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fde_product_form")
public class FdeProductForm extends BaseEntity {

    private String code;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String name;
    /** 「产品运作 ①」，与 weight 拼接成对外 label */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String form;
    /** 「最轻 · 不改变交付模式」 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String weight;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer sortOrder;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String fit;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String description;
    /** JSON 有序数组 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String deliverables;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String cycle;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String pricing;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String success;
    /** JSON 数字数组 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String caseIds;
    /** JSON 数字数组；一期不展示 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String customerIds;
    private Integer published;

    @TableLogic
    private Integer deleted;
}
