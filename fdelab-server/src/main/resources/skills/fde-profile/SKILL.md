---
name: fde-profile
description: >-
  Generates an FDE personal profile and uploads it to the FDELab talent library,
  which assigns a subdomain homepage such as jiangliwen.fdelab.io. Use when the
  user asks for an FDE intro, 个人简介, 人才主页, 子域名, or to upload a profile
  through the FDELab skill library or WorkBuddy.
---

# FDE 个人简介

把一个人的介绍整理成 FDELab 人才档案，并上传。系统按姓名拼音分配子域名，例如蒋利文得到 `jiangliwen.fdelab.io`。同一个人再次上传会更新原主页，点赞数和子域名保持不变。

## 接口

- 目录：`GET {FDELAB_API}/api/public/skills`
- 上传：`POST {FDELAB_API}/api/public/talents/ingest`
- 请求头：`X-Fdelab-Skill-Key: {FDELAB_SKILL_KEY}`

默认本地地址是 `http://127.0.0.1:8080`，默认密钥是 `fdelab-dev-skill-key`。环境变量 `FDELAB_API`、`FDELAB_SKILL_KEY` 优先。

## 步骤

1. 只用用户明确提供或已公开的材料。联系方式不确定就留空，不要编造电话、微信和案例数字。
2. 按下面的结构整理。`name` 和 `headline` 必填。`slug` 可空，空则用姓名拼音。
3. 用 JSON 调用上传接口。成功时把返回的 `homepageUrl` 告诉用户。
4. 上传失败时原样说明 `message`，不要改密钥，也不要换一个人的子域名去重试。

## 档案结构

主页按这个顺序展示：一句话介绍、徽章、联系方式、身份标签、行业标签、主要身份、过往经历、提供的服务、正在寻找的合作、FDE 实战案例。

```json
{
  "name": "蒋利文",
  "slug": "",
  "headline": "企业数智化转型导师 · FDE 智研社主理人",
  "summary": "列表页用的短介绍",
  "phone": "",
  "wechat": "",
  "website": "https://tauwork.ai",
  "websiteLabel": "Tauwork.ai",
  "location": "常驻广东 · 深圳",
  "profile": {
    "badges": ["FDE · AI 落地最后一公里"],
    "identityTags": ["FDE 交付工程师"],
    "industryTags": ["离散制造"],
    "roles": [{ "org": "所在组织", "title": "身份", "description": "在做什么" }],
    "experiences": [{ "title": "经历标题", "description": "一段经历" }],
    "services": [{ "title": "服务名称", "description": "交付方式" }],
    "collaborations": [{ "title": "合作主题", "description": "在找什么样的伙伴" }],
    "cases": [{
      "industry": "行业 · 主题",
      "title": "案例标题",
      "portrait": "客户画像",
      "points": ["做了什么"],
      "delivery": "交付与进展"
    }]
  }
}
```

## 示例

```bash
curl -s -X POST "$FDELAB_API/api/public/talents/ingest" \
  -H "Content-Type: application/json" \
  -H "X-Fdelab-Skill-Key: $FDELAB_SKILL_KEY" \
  -d @profile.json
```

返回 `data.homepageUrl`、`data.slug` 和 `data.id`。
