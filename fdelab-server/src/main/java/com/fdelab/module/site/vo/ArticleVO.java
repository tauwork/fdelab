package com.fdelab.module.site.vo;

import com.fdelab.module.site.entity.SiteArticle;

import java.time.LocalDateTime;

public record ArticleVO(
        Long id,
        String title,
        String summary,
        String content,
        String coverUrl,
        LocalDateTime createTime
) {

    public static ArticleVO summaryOf(SiteArticle article) {
        return of(article, null);
    }

    public static ArticleVO detailOf(SiteArticle article) {
        return of(article, article.getContent());
    }

    private static ArticleVO of(SiteArticle article, String content) {
        return new ArticleVO(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                content,
                article.getCoverUrl(),
                article.getCreateTime());
    }
}
