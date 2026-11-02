package com.fdelab.module.talent.vo;

import com.fdelab.module.talent.entity.FdeTalent;
import com.fdelab.module.talent.model.TalentProfile;

public record TalentVO(
        Long id,
        String name,
        String slug,
        String headline,
        String summary,
        String avatarUrl,
        String phone,
        String wechat,
        String website,
        String websiteLabel,
        String location,
        Integer likeCount,
        Integer published,
        String homepageUrl,
        Boolean liked,
        TalentProfile profile
) {
    public static TalentVO of(FdeTalent talent, String homepageUrl, boolean liked, TalentProfile profile, boolean withContact) {
        return new TalentVO(
                talent.getId(),
                talent.getName(),
                talent.getSlug(),
                talent.getHeadline(),
                talent.getSummary(),
                talent.getAvatarUrl(),
                withContact ? talent.getPhone() : null,
                withContact ? talent.getWechat() : null,
                withContact ? talent.getWebsite() : null,
                withContact ? talent.getWebsiteLabel() : null,
                talent.getLocation(),
                talent.getLikeCount() == null ? 0 : talent.getLikeCount(),
                talent.getPublished(),
                homepageUrl,
                liked,
                profile);
    }
}
