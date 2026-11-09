package com.fdelab.module.cases.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdelab.module.cases.entity.FdeCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FdeCaseMapper extends BaseMapper<FdeCase> {

    /**
     * 公开案例分页。
     * <ul>
     *   <li>q 只匹配 title / subtitle / partner / industry，不参与客户名匹配</li>
     *   <li>industry / cred 为精确匹配</li>
     *   <li>sort=occurYear 时取 occur_year 前 4 位数字作为起始年降序，取不到 4 位数字的排在最后</li>
     * </ul>
     */
    @Select("""
            <script>
            SELECT id, code, title, subtitle, industry, func, occur_year, partner,
                   cred, cred_note, fde_mode, scale, model_pct, pain, goal,
                   verdict, testimony, content_json, primary_fde_id, contributor_ids,
                   customer_id, published, deleted, create_time, update_time
            FROM fde_case
            WHERE deleted = 0
              AND published = 1
            <if test="q != null and q != ''">
              AND (
                title LIKE CONCAT('%', #{q}, '%')
                OR subtitle LIKE CONCAT('%', #{q}, '%')
                OR partner LIKE CONCAT('%', #{q}, '%')
                OR industry LIKE CONCAT('%', #{q}, '%')
              )
            </if>
            <if test="industry != null and industry != ''">
              AND industry = #{industry}
            </if>
            <if test="cred != null and cred != ''">
              AND cred = #{cred}
            </if>
            <choose>
              <when test="sort == 'occurYear'">
                ORDER BY CASE
                           WHEN occur_year REGEXP '^[0-9]{4}' THEN CAST(LEFT(occur_year, 4) AS UNSIGNED)
                           ELSE NULL
                         END DESC,
                         code ASC
              </when>
              <otherwise>
                ORDER BY code ASC
              </otherwise>
            </choose>
            </script>
            """)
    Page<FdeCase> selectPublicPage(Page<FdeCase> page,
                                   @Param("q") String q,
                                   @Param("industry") String industry,
                                   @Param("cred") String cred,
                                   @Param("sort") String sort);

    /**
     * 行业 facet：基于全量已发布数据，不受当前筛选影响。
     */
    @Select("""
            SELECT industry AS name, COUNT(*) AS cnt
            FROM fde_case
            WHERE deleted = 0
              AND published = 1
              AND industry IS NOT NULL
              AND industry <> ''
            GROUP BY industry
            ORDER BY cnt DESC, name ASC
            """)
    List<FacetRow> selectIndustryFacets();

    /**
     * 可信度 facet：固定按 A→D 在 service 层排序。
     */
    @Select("""
            SELECT cred AS code, COUNT(*) AS cnt
            FROM fde_case
            WHERE deleted = 0
              AND published = 1
              AND cred IS NOT NULL
              AND cred <> ''
            GROUP BY cred
            """)
    List<FacetRow> selectCredFacets();

    @Select("""
            SELECT COUNT(*)
            FROM fde_case
            WHERE deleted = 0
              AND published = 1
            """)
    long countPublic();
}
