<template>
  <view class="page">
    <view class="hero">
      <text class="mark">{{ profile.name || 'FDELab' }}</text>
      <text class="slogan">{{ profile.slogan || '实验、内容与业务的统一入口' }}</text>
      <text class="intro">{{ profile.intro || '' }}</text>
    </view>

    <view class="section-title">动态</view>
    <view v-if="error" class="hint">{{ error }}</view>
    <view v-else-if="loading" class="hint">加载中</view>
    <view v-else>
      <view v-for="item in articles" :key="item.id" class="card" @click="openArticle(item.id)">
        <text class="time">{{ item.createTime }}</text>
        <text class="title">{{ item.title }}</text>
        <text class="summary">{{ item.summary }}</text>
      </view>
      <view v-if="articles.length === 0" class="hint">还没有已发布的动态。</view>
    </view>
    <TabBar current="home" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '../../utils/request'
import TabBar from '../../components/TabBar.vue'

const profile = ref({})
const articles = ref([])
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [site, list] = await Promise.all([
      request({ url: '/api/public/profile', quiet: true }),
      request({ url: '/api/public/articles', quiet: true }),
    ])
    profile.value = site || {}
    articles.value = list || []
  } catch {
    error.value = '连不上内容服务。请确认后端已启动，并检查 src/utils/config.js 里的地址。'
  } finally {
    loading.value = false
  }
}

function openArticle(id) {
  uni.navigateTo({ url: `/pages/article/detail?id=${id}` })
}

onShow(load)
</script>

<style scoped>
.page {
  padding: 32rpx 32rpx 140rpx;
}

.hero {
  padding: 36rpx;
  border-radius: 24rpx;
  background: #17241e;
  color: #f4f1ea;
}

.mark,
.slogan,
.intro,
.time,
.title,
.summary {
  display: block;
}

.mark {
  color: #c4a574;
  letter-spacing: 4rpx;
}

.slogan {
  margin-top: 16rpx;
  font-size: 40rpx;
  line-height: 1.4;
}

.intro {
  margin-top: 16rpx;
  color: #d5e0d8;
  line-height: 1.7;
}

.section-title {
  margin: 36rpx 0 16rpx;
  font-size: 34rpx;
}

.card {
  margin-bottom: 16rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fffdf8;
}

.time {
  color: #1f6b4a;
  font-size: 24rpx;
}

.title {
  margin-top: 8rpx;
  font-size: 32rpx;
}

.summary,
.hint {
  margin-top: 8rpx;
  color: #5e6a63;
  line-height: 1.6;
}
</style>
