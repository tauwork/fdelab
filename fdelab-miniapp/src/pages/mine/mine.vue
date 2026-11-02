<template>
  <view class="page">
    <view class="card">
      <text class="name">FDELab</text>
      <text class="line">微信小程序骨架</text>
      <text class="line">接口地址 {{ apiBase }}</text>
      <text class="line">会员登录后续接入。人才主页由技能库上传，也可在管理后台维护。</text>
    </view>
    <TabBar current="mine" />
  </view>
</template>

<script setup>
import TabBar from '../../components/TabBar.vue'
import { API_BASE } from '../../utils/config'

const apiBase = API_BASE
</script>

<style scoped>
.page {
  padding: 32rpx 32rpx 140rpx;
}

.card {
  padding: 32rpx;
  border-radius: 20rpx;
  background: #fffdf8;
}

.name,
.line {
  display: block;
}

.name {
  font-size: 40rpx;
}

.line {
  margin-top: 16rpx;
  color: #5e6a63;
  line-height: 1.7;
}
</style>
