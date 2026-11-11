package com.fdelab.module.product.controller;

import com.fdelab.common.api.ApiResult;
import com.fdelab.module.product.service.ProductService;
import com.fdelab.module.product.vo.ProductFormDetailVO;
import com.fdelab.module.product.vo.ProductFormVO;
import com.fdelab.module.product.vo.ItemCardVO;
import com.fdelab.module.product.vo.ItemDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品与服务公开接口。
 * <p>默认只返回 published=1 的记录，按 sort_order 升序；数据量级个位数，不做分页。</p>
 */
@Tag(name = "产品与服务")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    @Operation(summary = "运作列表（4 条，含条目计数）")
    @GetMapping("/products")
    public ApiResult<List<ProductFormVO>> forms() {
        return ApiResult.ok(productService.forms());
    }

    @Operation(summary = "运作详情，按 code 查询")
    @GetMapping("/products/{code}")
    public ApiResult<ProductFormDetailVO> form(@PathVariable String code) {
        return ApiResult.ok(productService.form(code));
    }

    @Operation(summary = "条目卡片列表，kind 可选过滤")
    @GetMapping("/product-items")
    public ApiResult<List<ItemCardVO>> items(
            @RequestParam(name = "kind", required = false) String kind) {
        return ApiResult.ok(productService.items(kind));
    }

    @Operation(summary = "条目详情，按 code 查询（detail 原样透传）")
    @GetMapping("/product-items/{code}")
    public ApiResult<ItemDetailVO> item(@PathVariable String code) {
        return ApiResult.ok(productService.item(code));
    }
}
