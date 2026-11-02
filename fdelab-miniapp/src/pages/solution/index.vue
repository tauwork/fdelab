<template>
  <view class="page">
    <view class="card">
      <text class="title">方案</text>
      <text class="line">这里会放可复用的行业方案。人才主页里的实战案例会作为方案素材。</text>
    </view>
    <TabBar current="solution" />
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
