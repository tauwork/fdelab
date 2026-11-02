package com.fdelab.module.talent.support;

import com.fdelab.common.exception.BizException;
import com.github.promeg.pinyinhelper.Pinyin;

import java.util.Locale;
import java.util.function.Predicate;

public final class TalentSlugs {

    private TalentSlugs() {
    }

    public static String fromName(String name) {
        String raw = name == null ? "" : Pinyin.toPinyin(name, "").toLowerCase(Locale.ROOT);
        String slug = raw.replaceAll("[^a-z0-9]", "");
        if (slug.isBlank()) {
            slug = "fde";
        }
        return slug.length() > 32 ? slug.substring(0, 32) : slug;
    }

    public static String normalize(String slug) {
        if (slug == null || slug.isBlank()) {
            return "";
        }
        String value = slug.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "");
        value = value.replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
        if (value.length() > 32) {
            value = value.substring(0, 32).replaceAll("-$", "");
        }
        return value;
    }

    public static String unique(String base, Predicate<String> taken) {
        String slug = base == null || base.isBlank() ? "fde" : base;
        if (!taken.test(slug)) {
            return slug;
        }
        for (int i = 2; i < 1000; i++) {
            String suffix = "-" + i;
            String trimmed = slug.length() + suffix.length() > 32
                    ? slug.substring(0, 32 - suffix.length())
                    : slug;
            String candidate = trimmed + suffix;
            if (!taken.test(candidate)) {
                return candidate;
            }
        }
        throw new BizException("子域名已被占用");
    }
}
