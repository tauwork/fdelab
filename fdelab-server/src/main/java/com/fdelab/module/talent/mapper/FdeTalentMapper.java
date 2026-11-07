package com.fdelab.module.talent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdelab.module.talent.entity.FdeTalent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FdeTalentMapper extends BaseMapper<FdeTalent> {

    /**
     * 名录分页查询：筛选与排序全部落到 SQL，分页由 MyBatis-Plus 处理。
     * <p>q 匹配 name、headline，以及 profile_json 里的文本（行业/技能等）；
     * sort 的取值已在 service 层白名单收敛。</p>
     */
    @Select("""
            <script>
            SELECT id, name, slug, headline, summary, level_code, years, case_count,
                   profile_json, published, deleted, create_time, update_time
            FROM fde_talent
            WHERE deleted = 0
              AND published = 1
              AND level_code IS NOT NULL
            <if test="q != null and q != ''">
              AND (
                name LIKE CONCAT('%', #{q}, '%')
                OR headline LIKE CONCAT('%', #{q}, '%')
                OR JSON_SEARCH(profile_json, 'one', CONCAT('%', #{q}, '%')) IS NOT NULL
              )
            </if>
            <if test="level != null and level != '' and level != 'all'">
              AND level_code = #{level}
            </if>
            <choose>
              <when test="sort == 'years'">
                ORDER BY years DESC, id ASC
              </when>
              <when test="sort == 'level'">
                ORDER BY FIELD(level_code, 'M', 'P', 'A'), years DESC, id ASC
              </when>
              <otherwise>
                ORDER BY case_count DESC, years DESC, id ASC
              </otherwise>
            </choose>
            </script>
            """)
    Page<FdeTalent> selectRosterPage(Page<FdeTalent> page,
                                     @Param("q") String q,
                                     @Param("level") String level,
                                     @Param("sort") String sort);
}
