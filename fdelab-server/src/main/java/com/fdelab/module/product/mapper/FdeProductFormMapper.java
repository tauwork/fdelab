package com.fdelab.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fdelab.module.product.entity.FdeProductForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FdeProductFormMapper extends BaseMapper<FdeProductForm> {

    /**
     * 取某前缀下已用的最大数字后缀（含已软删行，避免复用业务码与唯一索引冲突）。
     */
    @Select("SELECT MAX(CAST(SUBSTRING(code, #{prefixLength} + 1) AS UNSIGNED)) "
            + "FROM fde_product_form "
            + "WHERE code REGEXP CONCAT('^', #{prefix}, '[0-9]+$')")
    Integer maxCodeSuffix(@Param("prefix") String prefix, @Param("prefixLength") int prefixLength);
}
