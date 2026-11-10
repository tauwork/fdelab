package com.fdelab.module.cases.admin.controller;

import com.fdelab.common.api.ApiResult;
import com.fdelab.module.cases.admin.dto.CustomerCreateRequest;
import com.fdelab.module.cases.admin.service.CustomerAdminService;
import com.fdelab.module.cases.entity.FdeCustomer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "客户管理")
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class CustomerAdminController {

    private final CustomerAdminService customerAdminService;

    @Operation(summary = "客户搜索")
    @GetMapping
    public ApiResult<List<CustomerVO>> search(@RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResult.ok(customerAdminService.search(keyword).stream()
                .map(CustomerVO::from)
                .toList());
    }

    @Operation(summary = "新建客户")
    @PostMapping
    public ApiResult<CustomerVO> create(@RequestBody CustomerCreateRequest request) {
        return ApiResult.ok(CustomerVO.from(customerAdminService.create(request)));
    }

    public record CustomerVO(Long id, String code, String displayName, Integer isMasked) {

        static CustomerVO from(FdeCustomer customer) {
            return new CustomerVO(
                    customer.getId(),
                    customer.getCode(),
                    customer.getDisplayName(),
                    customer.getIsMasked());
        }
    }
}
