package com.fdelab.module.product.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fdelab.common.api.ApiResult;
import com.fdelab.common.api.PageData;
import com.fdelab.module.product.admin.dto.ItemForm;
import com.fdelab.module.product.admin.service.ItemAdminService;
import com.fdelab.module.product.admin.vo.ItemAdminListItemVO;
import com.fdelab.module.product.admin.vo.ItemAdminVO;
import com.fdelab.module.product.admin.vo.ItemMetaVO;
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

@Tag(name = "产品条目管理")
@RestController
@RequestMapping("/api/admin/product-items")
@RequiredArgsConstructor
public class ItemAdminController {

    private final ItemAdminService itemAdminService;

    @Operation(summary = "条目分页")
    @GetMapping
    public ApiResult<PageData<ItemAdminListItemVO>> page(@RequestParam(name = "page", defaultValue = "1") long page,
                                                        @RequestParam(name = "size", defaultValue = "10") long size,
                                                        @RequestParam(name = "q", required = false) String q,
                                                        @RequestParam(name = "kind", required = false) String kind,
                                                        @RequestParam(name = "status", required = false) String status,
                                                        @RequestParam(name = "published", required = false) Integer published) {
        return ApiResult.ok(itemAdminService.page(page, size, q, kind, status, published));
    }

    @Operation(summary = "条目词表")
    @GetMapping("/meta")
    public ApiResult<ItemMetaVO> meta() {
        return ApiResult.ok(itemAdminService.meta());
    }

    @Operation(summary = "条目详情")
    @GetMapping("/{id}")
    public ApiResult<ItemAdminVO> detail(@PathVariable("id") Long id) {
        return ApiResult.ok(itemAdminService.get(id));
    }

    @Operation(summary = "新建草稿")
    @PostMapping
    public ApiResult<ItemAdminVO> create(@RequestBody ItemForm form) {
        return ApiResult.ok(itemAdminService.create(form));
    }

    @Operation(summary = "保存")
    @PutMapping("/{id}")
    public ApiResult<ItemAdminVO> update(@PathVariable("id") Long id, @RequestBody ItemForm form) {
        return ApiResult.ok(itemAdminService.update(id, form));
    }

    @Operation(summary = "发布 / 下架（仅 ADMIN）")
    @PutMapping("/{id}/publish")
    public ApiResult<ItemAdminVO> publish(@PathVariable("id") Long id,
                                         @RequestBody Map<String, Integer> body) {
        StpUtil.checkRole("ADMIN");
        return ApiResult.ok(itemAdminService.publish(id, body == null ? null : body.get("published")));
    }

    @Operation(summary = "软删")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        itemAdminService.delete(id);
        return ApiResult.ok(null);
    }
}
