package com.fdelab.module.talent.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 人才 ↔ 标杆案例 关联，sort_no 决定详情页展示顺序。 */
@Data
@TableName("fde_talent_case")
public class FdeTalentCase {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long talentId;

    private Long caseId;

    private Integer sortNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
