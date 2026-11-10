package com.fdelab.module.cases.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.cases.admin.dto.CustomerCreateRequest;
import com.fdelab.module.cases.entity.FdeCustomer;
import com.fdelab.module.cases.mapper.FdeCustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 客户后台读写：搜索、内联新建、编号生成。
 */
@Service
@RequiredArgsConstructor
public class CustomerAdminService {

    private static final Pattern CUSTOMER_CODE = Pattern.compile("^CUST-(\\d+)$");

    private final FdeCustomerMapper customerMapper;

    public List<FdeCustomer> search(String keyword) {
        String text = trimToNull(keyword);
        return customerMapper.selectList(Wrappers.<FdeCustomer>lambdaQuery()
                .like(text != null, FdeCustomer::getDisplayName, text)
                .orderByAsc(FdeCustomer::getDisplayName)
                .last("LIMIT 20"));
    }

    @Transactional
    public FdeCustomer create(CustomerCreateRequest request) {
        String displayName = request == null ? null : trimToNull(request.displayName());
        if (displayName == null) {
            throw new BizException("请填写客户名称");
        }
        return createOrReuse(displayName, request.isMasked());
    }

    /**
     * 同名（完全相等且未删除）直接复用，否则新建。
     */
    @Transactional
    public FdeCustomer createOrReuse(String displayName, Integer isMasked) {
        String name = trimToNull(displayName);
        if (name == null) {
            throw new BizException("请填写客户名称");
        }
        FdeCustomer existing = customerMapper.selectOne(Wrappers.<FdeCustomer>lambdaQuery()
                .eq(FdeCustomer::getDisplayName, name)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }

        FdeCustomer customer = new FdeCustomer();
        customer.setDisplayName(name);
        customer.setIsMasked(isMasked == null ? 0 : isMasked);
        customer.setPublished(1);
        customer.setCode(nextCode());
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                customerMapper.insert(customer);
                return customer;
            } catch (DuplicateKeyException ex) {
                if (attempt == 1) {
                    throw new BizException("客户编号冲突，请重试");
                }
                customer.setId(null);
                customer.setCode(nextCode());
            }
        }
        throw new BizException("客户编号冲突，请重试");
    }

    public String nextCode() {
        int max = 0;
        for (String code : customerMapper.selectAllCodes()) {
            if (code == null) {
                continue;
            }
            Matcher matcher = CUSTOMER_CODE.matcher(code.trim());
            if (matcher.matches()) {
                max = Math.max(max, Integer.parseInt(matcher.group(1)));
            }
        }
        return String.format("CUST-%03d", max + 1);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
