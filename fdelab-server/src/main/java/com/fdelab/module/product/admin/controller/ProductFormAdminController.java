package com.fdelab.module.product.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fdelab.common.api.ApiResult;
import com.fdelab.common.api.PageData;
import com.fdelab.module.product.admin.dto.ProductFormRequest;
import com.fdelab.module.product.admin.service.ProductFormAdminService;
import com.fdelab.module.product.admin.vo.ProductFormAdminListItemVO;
import com.fdelab.module.product.admin.vo.ProductFormAdminVO;
import com.fdelab.module.product.admin.vo.ProductFormMetaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "服务运作管理")
@RestController
@RequestMapping("/api/admin/product-forms")
@RequiredArgsConstructor
public class ProductFormAdminController {

    private final ProductFormAdminService productFormAdminService;

    @Operation(summary = "服务运作分页")
    @GetMapping
    public ApiResult<PageData<ProductFormAdminListItemVO>> page(
            @RequestParam(name = "page", defaultValue = "1") long page,
            @RequestParam(name = "size", defaultValue = "10") long size,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "published", required = false) Integer published) {
        return ApiResult.ok(productFormAdminService.page(page, size, q, published));
    }

    @Operation(summary = "服务运作词表（参照案例选择器）")
    @GetMapping("/meta")
    public ApiResult<ProductFormMetaVO> meta() {
        return ApiResult.ok(productFormAdminService.meta());
    }

    @Operation(summary = "服务运作详情")
    @GetMapping("/{id}")
    public ApiResult<ProductFormAdminVO> detail(@PathVariable("id") Long id) {
        return ApiResult.ok(productFormAdminService.get(id));
    }

    @Operation(summary = "新建草稿")
    @PostMapping
    public ApiResult<ProductFormAdminVO> create(@RequestBody ProductFormRequest request) {
        return ApiResult.ok(productFormAdminService.create(request));
    }

    @Operation(summary = "保存")
    @PutMapping("/{id}")
    public ApiResult<ProductFormAdminVO> update(@PathVariable("id") Long id,
                                               @RequestBody ProductFormRequest request) {
        return ApiResult.ok(productFormAdminService.update(id, request));
    }

    @Operation(summary = "发布 / 下架（仅 ADMIN）")
    @PutMapping("/{id}/publish")
    public ApiResult<ProductFormAdminVO> publish(@PathVariable("id") Long id,
                                                @RequestBody Map<String, Integer> body) {
        StpUtil.checkRole("ADMIN");
        return ApiResult.ok(productFormAdminService.publish(id, body == null ? null : body.get("published")));
    }

    @Operation(summary = "软删（下挂产品条目时拒绝）")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        productFormAdminService.delete(id);
        return ApiResult.ok(null);
    }
}
