package com.fdelab.module.talent.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fde_talent_like")
public class FdeTalentLike {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long talentId;

    private String visitorKey;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
