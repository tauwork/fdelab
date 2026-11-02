package com.fdelab.module.talent.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TalentSlugsTest {

    @Test
    void chineseNameBecomesSubdomainLabel() {
        assertEquals("jiangliwen", TalentSlugs.fromName("蒋利文"));
    }

    @Test
    void collisionAppendsSuffix() {
        String slug = TalentSlugs.unique("jiangliwen", candidate -> "jiangliwen".equals(candidate));
        assertEquals("jiangliwen-2", slug);
        assertFalse(slug.endsWith("-"));
    }
}
