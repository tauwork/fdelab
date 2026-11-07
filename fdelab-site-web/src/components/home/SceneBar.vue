<template>
  <section class="scene">
    <span class="t">我们选择先打能定义验证器的仗。</span>
    <a
      v-if="SCENARIO_URL"
      :href="SCENARIO_URL"
      target="_blank"
      rel="noopener"
    >看完整的场景优先级判断 →</a>
    <span v-else class="link is-off" aria-disabled="true">看完整的场景优先级判断 →</span>
  </section>
</template>

<script setup>
import { SCENARIO_URL } from '../../config/links'
</script>

<style scoped>
.scene {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
  padding-block: 48px 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.scene .t {
  font-family: var(--serif);
  font-size: 22px;
  color: var(--ink);
  letter-spacing: .02em;
}

.scene a,
.scene .link {
  font-size: 13px;
  color: var(--accent);
  letter-spacing: .03em;
}

.scene a:hover {
  color: var(--accent-deep);
}

.scene .link.is-off {
  cursor: not-allowed;
}
</style>
