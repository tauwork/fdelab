package com.fdelab.module.auth.vo;

public record LoginResponse(String token, UserVO user) {
}
