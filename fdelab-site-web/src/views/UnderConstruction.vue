<template>
  <section class="uc">
    <p class="uc-title">{{ title }} · 建设中</p>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '')
</script>

<style scoped>
.uc {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
  padding-block: 80px;
  min-height: 50vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.uc-title {
  font-family: var(--serif);
  font-size: 32px;
  font-weight: 500;
  color: var(--ink);
  letter-spacing: .02em;
  text-align: center;
}
</style>
