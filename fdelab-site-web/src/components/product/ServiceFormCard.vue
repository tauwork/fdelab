<template>
  <RouterLink
    :id="`svc-${form.code}`"
    class="scard"
    :class="{ flash }"
    :to="`/products/form/${form.code}`"
  >
    <div class="no">{{ form.label }}</div>
    <div class="nm">{{ form.name }}</div>
    <div class="ld">{{ brief }}</div>
    <div class="meta">
      <span>周期 {{ form.cycle }}</span>
      <span>{{ form.pricing }}</span>
      <span class="go">看这一类怎么合作 →</span>
    </div>
  </RouterLink>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  form: { type: Object, required: true },
  flash: { type: Boolean, default: false },
})

// 与 demo 一致：截 72 字再加省略号
const brief = computed(() => {
  const desc = String(props.form.desc || '')
  return `${desc.slice(0, 72)}…`
})
</script>
