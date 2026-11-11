<template>
  <div class="ck">
    <span class="cid">{{ item.title }} —— {{ item.subtitle }}</span>
    <span class="credc">{{ item.cred }}</span>
  </div>
  <div class="ckpis">
    <div v-for="kpi in item.kpis" :key="kpi.k" class="kv">
      <div class="v" :class="{ hard: kpi.hard }">{{ kpi.v }}</div>
      <div class="n">{{ kpi.k }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  item: { type: Object, required: true },
})
</script>
