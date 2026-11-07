<template>
  <section class="statband">
    <div class="statband-in">
      <div class="sitem">
        <div class="row"><b>2</b><span class="name">实收标杆案例</span></div>
        <p class="caliber">0 个包装，全部含硬指标</p>
      </div>
      <div class="sitem">
        <div class="row"><b>100%</b><span class="name">案例公开率</span></div>
        <p class="caliber">含能力归属检验</p>
      </div>
      <div class="sitem">
        <div class="row"><b>5 库</b><span class="name">全部可检索</span></div>
        <p class="caliber">案例 / FDE / 产品 / 供应商 / 网点</p>
      </div>
      <a
        v-if="AI_ASSISTANT_URL"
        class="sitem action"
        :href="AI_ASSISTANT_URL"
        target="_blank"
        rel="noopener"
      >
        <div class="row"><b>1</b><span class="name">AI 助手 · 对话直达五库</span></div>
        <p class="caliber">问一句，五库尽答 ↗</p>
      </a>
      <div v-else class="sitem action is-off" aria-disabled="true">
        <div class="row"><b>1</b><span class="name">AI 助手 · 对话直达五库</span></div>
        <p class="caliber">问一句，五库尽答 ↗</p>
      </div>
    </div>
    <div class="bandnote">
      <span>每个数字都可点进源 · 持续扩充，不虚报</span>
      <RouterLink to="/caliber">数据口径说明 →</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { AI_ASSISTANT_URL } from '../../config/links'
</script>

<style scoped>
.statband {
  background: rgba(255, 255, 255, .72);
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
}

.statband-in {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  align-items: stretch;
}

.sitem {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 26px 38px;
  border-right: 1px solid var(--line);
}

.sitem:first-child {
  padding-left: 0;
}

.sitem:last-child {
  border-right: 0;
}

.sitem .row {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.sitem b {
  font-family: var(--serif);
  font-size: 36px;
  font-weight: 500;
  color: var(--ink);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.sitem .name {
  font-size: 13.5px;
  color: var(--ink);
  font-weight: 500;
  letter-spacing: .02em;
}

.sitem .caliber {
  margin-top: 8px;
  font-size: 11.5px;
  color: var(--muted);
  letter-spacing: .05em;
}

.sitem.action {
  background: var(--blue-soft);
  border-radius: 14px;
  padding: 22px 26px;
  margin: 14px 0;
}

.sitem.action b {
  color: var(--accent);
}

.sitem.action .name {
  color: var(--accent);
}

.sitem.action .caliber {
  color: #5b78c9;
}

.sitem.action.is-off {
  cursor: not-allowed;
}

.bandnote {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
  padding-block: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--muted);
  letter-spacing: .04em;
}

.bandnote a {
  color: var(--accent);
}

@media (max-width: 1000px) {
  .statband-in {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .sitem {
    padding: 20px 24px;
  }

  .sitem:first-child {
    padding-left: 0;
  }

  .sitem:nth-child(2) {
    border-right: 0;
  }
}
</style>
