<template>
  <view class="page">
    <view v-if="error" class="hint">{{ error }}</view>
    <view v-else-if="loading" class="hint">加载中</view>
    <view v-else>
      <view v-for="item in records" :key="item.id" class="row" @click="open(item)">
        <view class="seal">
          <text class="seal-char">{{ item.name?.charAt(0) }}</text>
        </view>
        <view class="main">
          <text class="name">{{ item.name }}</text>
          <text class="headline">{{ item.headline }}</text>
          <text v-if="industryLine(item)" class="inds">{{ industryLine(item) }}</text>
        </view>
        <view class="like" :class="{ liked: item.liked }" @click.stop="like(item)">
          <text class="heart">♥</text>
          <text class="count">{{ item.likeCount }}</text>
        </view>
      </view>
      <view v-if="records.length === 0" class="hint">还没有发布的人才。</view>
    </view>
    <TabBar current="talent" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import TabBar from '../../components/TabBar.vue'
import { request } from '../../utils/request'
import { visitorKey } from '../../utils/visitor'

const records = ref([])
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await request({
      url: `/api/public/talents?page=1&size=50&visitorKey=${encodeURIComponent(visitorKey())}`,
      quiet: true,
    })
    records.value = data?.records || []
  } catch {
    error.value = '人才加载失败。请确认后端已启动。'
  } finally {
    loading.value = false
  }
}

function industryLine(item) {
  const tags = item?.profile?.industryTags || []
  if (!tags.length) return ''
  const shown = tags.slice(0, 2).join(' · ')
  return tags.length > 2 ? `${shown} 等 ${tags.length} 个领域` : shown
}

function open(item) {
  uni.navigateTo({ url: `/pages/talent/detail?slug=${item.slug}` })
}

async function like(item) {
  const result = await request({
    url: `/api/public/talents/${item.id}/like`,
    method: 'POST',
    data: { visitorKey: visitorKey() },
  })
  item.liked = result.liked
  item.likeCount = result.likeCount
  records.value = [...records.value].sort((a, b) => b.likeCount - a.likeCount || a.id - b.id)
}

onShow(load)
</script>

<style scoped>
.page {
  padding: 24rpx 32rpx 140rpx;
}

.hint {
  display: block;
  color: #8a7a5e;
  line-height: 1.6;
}

.row {
  display: flex;
  align-items: center;
  gap: 26rpx;
  margin-bottom: 24rpx;
  padding: 30rpx;
  border-radius: 28rpx;
  background: #fffdf8;
  box-shadow: 0 6rpx 24rpx rgba(23, 36, 30, 0.06);
}

/* 印章：视觉锚点 */
.seal {
  flex-shrink: 0;
  width: 92rpx;
  height: 92rpx;
  border-radius: 50%;
  background: #17241e;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(196, 165, 116, 0.5);
}

.seal-char {
  font-family: "Iowan Old Style", "Palatino", "Songti SC", "STSongti-SC", "SimSun", serif;
  font-size: 42rpx;
  color: #c4a574;
  line-height: 1;
}

.main {
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-family: "Iowan Old Style", "Palatino", "Songti SC", "STSongti-SC", "SimSun", serif;
  font-size: 36rpx;
  font-weight: 500;
  color: #17241e;
  line-height: 1.3;
}

.headline {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #5e6a63;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.inds {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8a7a5e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 点赞：轻量，无底色 */
.like {
  flex-shrink: 0;
  width: 88rpx;
  height: 88rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2rpx;
  border-radius: 50%;
}

.heart {
  font-size: 34rpx;
  line-height: 1;
  color: #c4a574;
}

.count {
  font-size: 22rpx;
  color: #5e6a63;
  font-variant-numeric: tabular-nums;
}

.like.liked .heart {
  color: #d85a30;
}

.like.liked .count {
  color: #1f6b4a;
  font-weight: 500;
}
</style>
