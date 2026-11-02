<template>
  <view class="page">
    <view v-if="error" class="hint">{{ error }}</view>
    <view v-else-if="!talent" class="hint">加载中</view>
    <view v-else>
      <text class="domain">{{ talent.slug }}.fdelab.io</text>
      <text class="name">{{ talent.name }}</text>
      <text class="headline">{{ talent.headline }}</text>
      <view class="badges">
        <text v-for="item in profile.badges || []" :key="item" class="badge">{{ item }}</text>
      </view>
      <view class="like" @click="like">{{ talent.liked ? '已赞' : '点赞' }} {{ talent.likeCount }}</view>
      <view v-if="contacts.length" class="card">
        <text v-for="item in contacts" :key="item.label" class="line">{{ item.label }} {{ item.value }}</text>
      </view>
      <view v-if="(profile.identityTags || []).length" class="card">
        <text class="label">身份</text>
        <text class="line">{{ profile.identityTags.join(' · ') }}</text>
        <text v-if="(profile.industryTags || []).length" class="line">{{ profile.industryTags.join(' · ') }}</text>
      </view>
      <view v-for="block in blocks" :key="block.title" class="section">
        <text class="label">{{ block.title }}</text>
        <view v-for="(item, index) in block.items" :key="index" class="card">
          <text v-if="item.org" class="kicker">{{ item.org }}</text>
          <text v-if="item.industry" class="kicker">{{ item.industry }}</text>
          <text class="title">{{ item.title }}</text>
          <text v-if="item.description" class="line">{{ item.description }}</text>
          <text v-if="item.portrait" class="line">{{ item.portrait }}</text>
          <text v-for="point in item.points || []" :key="point" class="line">· {{ point }}</text>
          <text v-if="item.delivery" class="line">交付与进展：{{ item.delivery }}</text>
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
    person.phone ? { label: '手机', value: person.phone } : null,
    person.wechat ? { label: '微信', value: person.wechat } : null,
    person.website ? { label: '网站', value: person.websiteLabel || person.website } : null,
    person.location ? { label: '所在', value: person.location } : null,
  ].filter(Boolean)
})
const blocks = computed(() => [
  { title: '主要身份', items: profile.value.roles || [] },
  { title: '过往经历', items: profile.value.experiences || [] },
  { title: '提供的服务', items: profile.value.services || [] },
  { title: '正在寻找的合作', items: profile.value.collaborations || [] },
  { title: 'FDE 实战案例', items: profile.value.cases || [] },
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
  padding: 32rpx;
}

.domain,
.name,
.headline,
.line,
.label,
.title,
.kicker,
.badge {
  display: block;
}

.domain,
.kicker {
  color: #1f6b4a;
  font-size: 24rpx;
}

.name {
  margin-top: 8rpx;
  font-size: 48rpx;
}

.headline,
.line {
  margin-top: 10rpx;
  color: #3d4a43;
  line-height: 1.7;
}

.badges {
  margin-top: 16rpx;
}

.badge,
.like {
  display: inline-block;
  margin: 8rpx 8rpx 0 0;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #17241e;
  color: #f4f1ea;
  font-size: 24rpx;
}

.like {
  margin-top: 20rpx;
  background: #1f6b4a;
}

.card,
.section {
  margin-top: 20rpx;
}

.card {
  padding: 24rpx;
  border-radius: 20rpx;
  background: #fffdf8;
}

.label {
  margin-bottom: 8rpx;
  font-size: 32rpx;
}

.title {
  margin-top: 6rpx;
  font-size: 30rpx;
}

.hint {
  color: #8a5a2a;
}
</style>
