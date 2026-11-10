<template>
  <view class="page">
    <view v-if="error" class="hint">{{ error }}</view>
    <view v-else-if="!talent" class="hint">加载中</view>
    <view v-else>
      <!-- Hero -->
      <view class="hero">
        <view class="hero-top">
          <view class="seal">
            <text class="seal-char">{{ talent.name?.charAt(0) }}</text>
          </view>
          <view class="hero-id">
            <text class="name">{{ talent.name }}</text>
            <text v-if="talent.headline" class="headline">{{ talent.headline }}</text>
          </view>
        </view>
        <text v-if="talent.summary" class="summary">{{ talent.summary }}</text>
        <view v-if="(profile.badges || []).length" class="badges">
          <text v-for="item in profile.badges" :key="item" class="badge">{{ item }}</text>
        </view>
        <view class="hero-foot">
          <text v-if="talent.location" class="loc">{{ talent.location }}</text>
          <view class="like" @click="like">
            <text class="heart">♥</text>
            <text class="count">{{ talent.likeCount }}</text>
            <text class="like-label">{{ talent.liked ? '已赞' : '点赞' }}</text>
          </view>
        </view>
      </view>

      <!-- 身份与行业 -->
      <view v-if="(profile.identityTags || []).length" class="sec">
        <text class="sec-title">身份</text>
        <view class="chip-flow">
          <text v-for="tag in profile.identityTags" :key="tag" class="chip">{{ tag }}</text>
        </view>
      </view>
      <view v-if="(profile.industryTags || []).length" class="sec">
        <text class="sec-title">行业</text>
        <view class="chip-flow">
          <text v-for="tag in profile.industryTags" :key="tag" class="chip gold">{{ tag }}</text>
        </view>
      </view>

      <!-- 条目区块：单栏 -->
      <view v-for="block in blocks" :key="block.title" class="sec">
        <text class="sec-title">{{ block.title }}</text>
        <view class="stack">
          <view v-for="(item, index) in block.items" :key="index" class="entry">
            <text v-if="block.showOrg && item.org" class="entry-kicker">{{ item.org }}</text>
            <text class="entry-title">{{ item.title }}</text>
            <text v-if="item.description" class="entry-desc">{{ item.description }}</text>
          </view>
        </view>
      </view>

      <!-- 案例 -->
      <view v-if="(profile.cases || []).length" class="sec">
        <text class="sec-title">实战案例</text>
        <view class="case" v-for="(item, index) in profile.cases" :key="index">
          <text v-if="item.industry" class="ind">{{ item.industry }}</text>
          <text v-if="item.title" class="case-title">{{ item.title }}</text>
          <text v-if="item.portrait" class="portrait">{{ item.portrait }}</text>
          <view v-if="(item.points || []).length" class="points">
            <text v-for="point in item.points" :key="point" class="point">· {{ point }}</text>
          </view>
          <view v-if="item.delivery" class="delivery-wrap">
            <text class="delivery-label">交付结果</text>
            <text class="delivery">{{ item.delivery }}</text>
          </view>
        </view>
      </view>

      <!-- 联系条 -->
      <view v-if="contacts.length" class="contact-bar">
        <text class="cb-title">合作洽谈</text>
        <view class="cb-grid">
          <view v-for="item in contacts" :key="item.label" class="cb-item">
            <text class="cb-label">{{ item.label }}</text>
            <text class="cb-value">{{ item.value }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '../../utils/request'
import { visitorKey } from '../../utils/visitor'

const talent = ref(null)
const error = ref('')
const profile = computed(() => talent.value?.profile || {})
const contacts = computed(() => {
  const person = talent.value
  if (!person) return []
  return [
    person.phone ? { label: '电话', value: person.phone } : null,
    person.wechat ? { label: '微信', value: person.wechat } : null,
    person.website ? { label: '网站', value: person.websiteLabel || person.website } : null,
  ].filter(Boolean)
})
const blocks = computed(() => [
  { title: '当前角色', showOrg: true, items: profile.value.roles || [] },
  { title: '履历', showOrg: false, items: profile.value.experiences || [] },
  { title: '服务内容', showOrg: false, items: profile.value.services || [] },
  { title: '期望合作', showOrg: false, items: profile.value.collaborations || [] },
].filter((block) => block.items.length))

async function load(slug) {
  try {
    talent.value = await request({
      url: `/api/public/talents/slug/${slug}?visitorKey=${encodeURIComponent(visitorKey())}`,
      quiet: true,
    })
  } catch (err) {
    error.value = err.message || '主页加载失败'
  }
}

async function like() {
  const result = await request({
    url: `/api/public/talents/${talent.value.id}/like`,
    method: 'POST',
    data: { visitorKey: visitorKey() },
  })
  talent.value.liked = result.liked
  talent.value.likeCount = result.likeCount
}

onLoad((query) => load(query.slug))
</script>

<style scoped>
.page {
  padding: 24rpx 32rpx 60rpx;
}

.hint {
  display: block;
  color: #8a7a5e;
  line-height: 1.6;
}

/* ===== Hero ===== */
.hero {
  padding: 36rpx;
  border-radius: 28rpx;
  background: #17241e;
}

.hero-top {
  display: flex;
  align-items: center;
  gap: 26rpx;
}

.seal {
  flex-shrink: 0;
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: rgba(244, 241, 234, 0.06);
  border: 2rpx solid rgba(196, 165, 116, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.seal-char {
  font-family: "Iowan Old Style", "Palatino", "Songti SC", "STSongti-SC", "SimSun", serif;
  font-size: 50rpx;
  color: #c4a574;
  line-height: 1;
}

.hero-id {
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-family: "Iowan Old Style", "Palatino", "Songti SC", "STSongti-SC", "SimSun", serif;
  font-size: 44rpx;
  font-weight: 500;
  color: #f4f1ea;
  line-height: 1.25;
}

.headline {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.5;
  color: #9fb3a7;
}

.summary {
  display: block;
  margin-top: 24rpx;
  font-size: 25rpx;
  line-height: 1.8;
  color: #c8d4cc;
}

.badges {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 24rpx;
}

.badge {
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(196, 165, 116, 0.14);
  color: #c4a574;
  font-size: 21rpx;
}

.hero-foot {
  display: flex;
  align-items: center;
  margin-top: 28rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(244, 241, 234, 0.12);
}

.loc {
  flex: 1;
  font-size: 24rpx;
  color: #9fb3a7;
}

.like {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
  padding: 12rpx 26rpx;
  border-radius: 999rpx;
  background: #f4f1ea;
}

.heart {
  font-size: 28rpx;
  line-height: 1;
  color: #1f6b4a;
}

.count {
  font-size: 26rpx;
  font-weight: 500;
  color: #17241e;
  font-variant-numeric: tabular-nums;
}

.like-label {
  font-size: 22rpx;
  color: #5e6a63;
}

/* ===== 区块 ===== */
.sec {
  margin-top: 56rpx;
}

.sec-title {
  display: block;
  font-family: "Iowan Old Style", "Palatino", "Songti SC", "STSongti-SC", "SimSun", serif;
  font-size: 34rpx;
  font-weight: 500;
  color: #17241e;
  margin-bottom: 20rpx;
}

/* 标签 chips：少量、清晰 */
.chip-flow {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.chip {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  background: #fffdf8;
  box-shadow: 0 4rpx 16rpx rgba(23, 36, 30, 0.06);
  font-size: 24rpx;
  color: #2c3731;
}

.chip.gold {
  color: #8a7a5e;
}

/* 单栏条目 */
.stack {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.entry {
  padding: 26rpx 28rpx;
  border-radius: 24rpx;
  background: #fffdf8;
  box-shadow: 0 4rpx 16rpx rgba(23, 36, 30, 0.06);
}

.entry-kicker {
  display: block;
  font-size: 21rpx;
  color: #1f6b4a;
}

.entry-title {
  display: block;
  margin-top: 6rpx;
  font-size: 29rpx;
  font-weight: 500;
  color: #17241e;
  line-height: 1.4;
}

.entry-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #5e6a63;
}

/* ===== 案例卡 ===== */
.case {
  position: relative;
  margin-bottom: 24rpx;
  padding: 30rpx 30rpx 26rpx;
  border-radius: 24rpx;
  background: #fffdf8;
  box-shadow: 0 4rpx 16rpx rgba(23, 36, 30, 0.06);
}

.case::before {
  content: "";
  position: absolute;
  left: 0;
  top: 30rpx;
  bottom: 30rpx;
  width: 6rpx;
  border-radius: 6rpx;
  background: #1f6b4a;
}

.case > text {
  display: block;
}

.ind {
  font-size: 21rpx;
  color: #1f6b4a;
}

.case-title {
  margin-top: 10rpx;
  font-family: "Iowan Old Style", "Palatino", "Songti SC", "STSongti-SC", "SimSun", serif;
  font-size: 32rpx;
  font-weight: 500;
  line-height: 1.45;
  color: #17241e;
}

.portrait {
  margin-top: 12rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #6d786f;
}

.points {
  margin-top: 12rpx;
}

.point {
  display: block;
  font-size: 24rpx;
  line-height: 1.85;
  color: #46524c;
}

.delivery-wrap {
  margin-top: 14rpx;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  background: rgba(31, 107, 74, 0.06);
}

.delivery-label {
  display: block;
  font-size: 20rpx;
  color: #1f6b4a;
}

.delivery {
  display: block;
  margin-top: 4rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #2c3731;
}

/* ===== 联系条 ===== */
.contact-bar {
  margin-top: 56rpx;
  padding: 32rpx 34rpx;
  border-radius: 28rpx;
  background: #17241e;
}

.cb-title {
  display: block;
  font-family: "Iowan Old Style", "Palatino", "Songti SC", "STSongti-SC", "SimSun", serif;
  font-size: 30rpx;
  color: #f4f1ea;
}

.cb-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx 40rpx;
  margin-top: 20rpx;
}

.cb-item {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.cb-label {
  font-size: 20rpx;
  color: #9fb3a7;
}

.cb-value {
  font-size: 26rpx;
  color: #f4f1ea;
}
</style>
