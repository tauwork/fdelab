package com.fdelab.module.system.controller;

import com.fdelab.common.api.ApiResult;
import com.fdelab.common.api.PageData;
import com.fdelab.module.auth.vo.UserVO;
import com.fdelab.module.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "用户分页")
    @GetMapping
    public ApiResult<PageData<UserVO>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword) {
        return ApiResult.ok(sysUserService.pageUsers(page, size, keyword));
    }
}
