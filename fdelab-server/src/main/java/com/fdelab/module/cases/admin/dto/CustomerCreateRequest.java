package com.fdelab.module.cases.admin.dto;

/**
 * 案例编辑页内联新建客户。
 */
public record CustomerCreateRequest(String displayName, Integer isMasked) {
}
