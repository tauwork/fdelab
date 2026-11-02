package com.fdelab.module.site.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.fdelab.common.exception.BizException;
import com.fdelab.module.site.entity.SiteArticle;
import com.fdelab.module.site.mapper.SiteArticleMapper;
import com.fdelab.module.site.vo.ArticleVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteArticleService extends ServiceImpl<SiteArticleMapper, SiteArticle> {

    public List<ArticleVO> listPublished() {
        return list(Wrappers.<SiteArticle>lambdaQuery()
                .eq(SiteArticle::getPublished, 1)
                .orderByDesc(SiteArticle::getCreateTime))
                .stream()
                .map(ArticleVO::summaryOf)
                .toList();
    }

    public ArticleVO getPublished(Long id) {
        SiteArticle article = getOne(Wrappers.<SiteArticle>lambdaQuery()
                .eq(SiteArticle::getId, id)
                .eq(SiteArticle::getPublished, 1));
        if (article == null) {
            throw new BizException(404, "内容不存在");
        }
        return ArticleVO.detailOf(article);
    }
}
