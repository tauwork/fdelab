<template>
  <view class="page">
    <view class="head">人才</view>
    <view class="lead">一位一行，按点赞从高到低。</view>
    <view v-if="error" class="hint">{{ error }}</view>
    <view v-else-if="loading" class="hint">加载中</view>
    <view v-else>
      <view v-for="item in records" :key="item.id" class="row" @click="open(item)">
        <view class="main">
          <text class="name">{{ item.name }}</text>
          <text class="headline">{{ item.headline }}</text>
          <text class="meta">{{ item.location || item.slug + '.fdelab.io' }}</text>
        </view>
        <view class="like" @click.stop="like(item)">{{ item.liked ? '已赞' : '点赞' }} {{ item.likeCount }}</view>
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
  padding: 32rpx 32rpx 140rpx;
}

.head {
  font-size: 44rpx;
}

.lead,
.hint,
.headline,
.meta {
  display: block;
  color: #5e6a63;
  line-height: 1.6;
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 16rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fffdf8;
}

.main {
  flex: 1;
}

.name {
  display: block;
  font-size: 34rpx;
}

.like {
  align-self: center;
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: #1f6b4a;
  color: #f4f1ea;
  font-size: 24rpx;
}
</style>
