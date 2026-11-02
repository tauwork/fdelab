<template>
  <view class="page">
    <view class="card">
      <text class="title">AI</text>
      <text class="line">这里会放 FDE 可调用的智能能力。个人简介技能已经可以从技能库上传人才档案。</text>
    </view>
    <TabBar current="ai" />
  </view>
</template>

<script setup>
import TabBar from '../../components/TabBar.vue'
</script>

<style scoped>
.page { padding: 32rpx 32rpx 140rpx; }
.card { padding: 32rpx; border-radius: 20rpx; background: #fffdf8; }
.title, .line { display: block; }
.title { font-size: 40rpx; }
.line { margin-top: 16rpx; color: #5e6a63; line-height: 1.7; }
</style>
