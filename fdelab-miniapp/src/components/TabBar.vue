<template>
  <view class="tabbar">
    <view
      v-for="item in tabs"
      :key="item.key"
      class="tab"
      :class="{ on: current === item.key }"
      @click="go(item)"
    >
      {{ item.label }}
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  current: { type: String, required: true },
})

const tabs = [
  { key: 'home', label: '首页', url: '/pages/index/index' },
  { key: 'talent', label: '人才', url: '/pages/talent/list' },
  { key: 'ai', label: 'AI', url: '/pages/ai/index' },
  { key: 'solution', label: '方案', url: '/pages/solution/index' },
  { key: 'mine', label: '我的', url: '/pages/mine/mine' },
]

function go(item) {
  if (item.key === props.current) return
  uni.redirectTo({ url: item.url })
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  display: flex;
  padding: 12rpx 8rpx calc(12rpx + env(safe-area-inset-bottom));
  background: #fffdf8;
  border-top: 1px solid #e3dccf;
}

.tab {
  flex: 1;
  text-align: center;
  color: #5e6a63;
  font-size: 24rpx;
  line-height: 72rpx;
}

.on {
  color: #1f6b4a;
  font-weight: 600;
}
</style>
