package com.fdelab.module.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.auth.dto.LoginRequest;
import com.fdelab.module.auth.vo.LoginResponse;
import com.fdelab.module.auth.vo.UserVO;
import com.fdelab.module.system.entity.SysUser;
import com.fdelab.module.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserService.getOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername().trim()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(403, "账号已停用");
        }
        StpUtil.login(user.getId());
        return new LoginResponse(StpUtil.getTokenValue(), UserVO.from(user));
    }

    public UserVO currentUser() {
        SysUser user = sysUserService.getById(StpUtil.getLoginIdAsLong());
        if (user == null) {
            throw new BizException(401, "登录用户不存在");
        }
        return UserVO.from(user);
    }
}
