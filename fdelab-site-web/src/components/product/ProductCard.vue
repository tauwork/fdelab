<template>
  <RouterLink class="icard" :to="`/products/item/${item.code}`">
    <div class="top">
      <span class="kind" :class="item.kind">{{ item.kindLabel }}</span>
      <span class="pill" :class="STATUS_TONE[item.status] || 'grey'" style="margin-left: auto">
        {{ item.statusLabel }}
      </span>
    </div>
    <div class="name">{{ item.name }}</div>
    <div class="desc">{{ item.summary }}</div>
    <div v-if="item.source" class="srcbar">
      <span class="runs">移植自 {{ item.source.origin }}</span>
      <span :class="`adapt-${item.source.adapt}`">{{ item.source.adaptLabel }}</span>
      <span>{{ item.source.open }}能接上</span>
      <span class="runs">跑过 {{ item.source.runs }}</span>
    </div>
    <div class="foot">
      <span>{{ item.cycle }}</span>
      <span class="go">→</span>
    </div>
  </RouterLink>
</template>

<script setup>
const STATUS_TONE = { on: 'ok', beta: 'warn', dev: 'grey' }

defineProps({
  item: { type: Object, required: true },
})
</script>
