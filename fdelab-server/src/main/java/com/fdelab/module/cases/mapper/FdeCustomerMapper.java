package com.fdelab.module.cases.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fdelab.module.cases.entity.FdeCustomer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FdeCustomerMapper extends BaseMapper<FdeCustomer> {

    /**
     * 后台生成下一个客户编号时使用：包含软删记录，避免复用已占用的唯一键。
     */
    @Select("SELECT code FROM fde_customer")
    List<String> selectAllCodes();
}
