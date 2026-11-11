<template>
  <div class="wrap tabsbar" id="tabs">
    <button
      v-for="item in items"
      :key="item.key"
      class="tab"
      :class="{ on: active === item.key }"
      type="button"
      @click="$emit('change', item.key)"
    >
      <b>{{ item.label }}</b>
      <i v-if="item.count">{{ item.count }}</i>
    </button>
  </div>
</template>

<script setup>
defineProps({
  active: { type: String, default: 'products' },
  items: { type: Array, default: () => [] },
})

defineEmits(['change'])
</script>
