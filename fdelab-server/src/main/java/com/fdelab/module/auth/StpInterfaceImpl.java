package com.fdelab.module.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.fdelab.module.system.entity.SysUser;
import com.fdelab.module.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserService sysUserService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SysUser user = sysUserService.getById(Long.valueOf(String.valueOf(loginId)));
        if (user == null || user.getRoleCode() == null) {
            return List.of();
        }
        return List.of(user.getRoleCode());
    }
}
