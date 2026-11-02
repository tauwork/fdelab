<template>
  <view class="page">
    <view v-if="error" class="hint">{{ error }}</view>
    <view v-else-if="article" class="card">
      <text class="time">{{ article.createTime }}</text>
      <text class="title">{{ article.title }}</text>
      <text class="summary">{{ article.summary }}</text>
      <text class="content">{{ article.content }}</text>
    </view>
    <view v-else class="hint">加载中</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '../../utils/request'

const article = ref(null)
const error = ref('')

onLoad(async (query) => {
  try {
    article.value = await request({ url: `/api/public/articles/${query.id}` })
  } catch (err) {
    error.value = err.message || '内容加载失败'
  }
})
</script>

<style scoped>
.page {
  padding: 32rpx;
}

.card {
  padding: 32rpx;
  border-radius: 20rpx;
  background: #fffdf8;
}

.time,
.title,
.summary,
.content,
.hint {
  display: block;
}

.time {
  color: #1f6b4a;
  font-size: 24rpx;
}

.title {
  margin-top: 12rpx;
  font-size: 40rpx;
  line-height: 1.4;
}

.summary,
.content,
.hint {
  margin-top: 16rpx;
  line-height: 1.8;
  color: #3d4a43;
}

.hint {
  color: #8a5a2a;
}
</style>
