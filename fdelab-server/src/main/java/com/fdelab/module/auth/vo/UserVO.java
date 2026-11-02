package com.fdelab.module.auth.vo;

import com.fdelab.module.system.entity.SysUser;

import java.time.LocalDateTime;

public record UserVO(
        Long id,
        String username,
        String nickname,
        String roleCode,
        Integer status,
        LocalDateTime createTime
) {

    public static UserVO from(SysUser user) {
        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRoleCode(),
                user.getStatus(),
                user.getCreateTime());
    }
}
