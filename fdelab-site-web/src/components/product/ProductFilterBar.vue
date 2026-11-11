<template>
  <div class="fbar">
    <button class="fchip" :class="{ on: active === 'all' }" type="button" @click="$emit('change', 'all')">
      全部<span class="n">{{ total }}</span>
    </button>
    <button
      v-for="chip in CHIPS"
      :key="chip.kind"
      class="fchip"
      :class="{ on: active === chip.kind }"
      type="button"
      @click="$emit('change', chip.kind)"
    >
      {{ chip.label }}<span class="n">{{ counts[chip.kind] || 0 }}</span>
    </button>
  </div>
</template>

<script setup>
// 产品页 chip 固定四类；m 在产品页叫「方法资产」，不是「方法论」。
const CHIPS = [
  { kind: 'de', label: '数字员工' },
  { kind: 'ag', label: 'Agent' },
  { kind: 'sa', label: 'SaaS' },
  { kind: 'm', label: '方法资产' },
]

defineProps({
  active: { type: String, default: 'all' },
  total: { type: Number, default: 0 },
  counts: { type: Object, default: () => ({}) },
})

defineEmits(['change'])
</script>
