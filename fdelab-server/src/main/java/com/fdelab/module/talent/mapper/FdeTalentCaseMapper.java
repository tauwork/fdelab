package com.fdelab.module.talent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fdelab.module.cases.entity.FdeCase;
import com.fdelab.module.talent.entity.FdeTalentCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FdeTalentCaseMapper extends BaseMapper<FdeTalentCase> {

    /**
     * 详情页案例：关联表按 sort_no 在前，primary_fde_id 命中的案例按 code 追加在后，
     * 一次 join 查完；同一案例只出现一次。
     */
    @Select("""
            SELECT c.id, c.code, c.title, c.subtitle, c.industry, c.pain, c.cred, c.cred_note,
                   c.verdict, c.content_json, c.primary_fde_id, c.published, c.deleted
            FROM fde_case c
            LEFT JOIN fde_talent_case tc
                   ON tc.case_id = c.id AND tc.talent_id = #{talentId}
            WHERE c.deleted = 0
              AND c.published = 1
              AND (tc.id IS NOT NULL OR c.primary_fde_id = #{talentId})
            ORDER BY (tc.id IS NULL) ASC, tc.sort_no ASC, tc.id ASC, c.code ASC
            """)
    List<FdeCase> selectVisibleCases(@Param("talentId") Long talentId);

    /** 与 selectVisibleCases 同口径，用于 case_count 回写。 */
    @Select("""
            SELECT COUNT(DISTINCT c.id)
            FROM fde_case c
            LEFT JOIN fde_talent_case tc
                   ON tc.case_id = c.id AND tc.talent_id = #{talentId}
            WHERE c.deleted = 0
              AND c.published = 1
              AND (tc.id IS NOT NULL OR c.primary_fde_id = #{talentId})
            """)
    long countVisibleCases(@Param("talentId") Long talentId);

    @Select("SELECT DISTINCT talent_id FROM fde_talent_case WHERE case_id = #{caseId}")
    List<Long> selectTalentIdsByCase(@Param("caseId") Long caseId);
}
