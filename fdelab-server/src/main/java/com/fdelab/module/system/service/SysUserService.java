package com.fdelab.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.fdelab.common.api.PageData;
import com.fdelab.module.auth.vo.UserVO;
import com.fdelab.module.system.entity.SysUser;
import com.fdelab.module.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    public PageData<UserVO> pageUsers(long page, long size, String keyword) {
        long pageNo = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), 100);
        String text = StringUtils.hasText(keyword) ? keyword.trim() : null;
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>()
                .and(text != null, wrapper -> wrapper
                        .like(SysUser::getUsername, text)
                        .or()
                        .like(SysUser::getNickname, text))
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = page(new Page<>(pageNo, pageSize), query);
        List<UserVO> records = result.getRecords().stream().map(UserVO::from).toList();
        return PageData.of(result, records);
    }
}
