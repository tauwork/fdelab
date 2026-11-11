<template>
  <div class="sl-wrap">
    <div class="sl-label">怎么选：按客户状态推荐，点状态定位到对应的服务运作：</div>
    <div class="stepline">
      <div
        v-for="form in forms"
        :key="form.code"
        class="scell"
        tabindex="0"
        role="button"
        @click="$emit('jump', form.code)"
        @keydown.enter.prevent="$emit('jump', form.code)"
        @keydown.space.prevent="$emit('jump', form.code)"
      >
        <div class="st">{{ STATE[form.code] }}</div>
        <span class="to">{{ shortName(form.name) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
// 「怎么选」的客户状态文案来自 demo EXTRA，属页面文案，不来自接口。
const STATE = {
  F1: '还没启动 AI，一号位好奇但谨慎',
  F2: '已有 POC 在跑，但上线即下线',
  F3: '多 BU 都在各自试点，重复造轮子',
  F4: '想建自己的 FDE 编制',
}

defineProps({
  forms: { type: Array, default: () => [] },
})

defineEmits(['jump'])

function shortName(name) {
  return String(name || '').replace(/（.*）/, '')
}
</script>
