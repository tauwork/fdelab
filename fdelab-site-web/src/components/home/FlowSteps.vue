<template>
  <section class="sec">
    <p class="sec-kicker">落地路径</p>
    <h2 class="sec-h">AI 落地，应该这样走。</h2>
    <p class="sec-lead">最后一公里，分五步走完，每一步都有现成的库接住你。</p>
    <div class="flow">
      <RouterLink class="fstep" to="/articles">
        <p class="no">01 · 先验货</p>
        <p class="act">看案例</p>
        <p class="why">别听故事，看硬指标</p>
      </RouterLink>
      <RouterLink class="fstep" to="/talents">
        <p class="no">02 · 找对人</p>
        <p class="act">配 FDE</p>
        <p class="why">落地要有人负责</p>
      </RouterLink>
      <RouterLink class="fstep" to="/products">
        <p class="no">03 · 选路径</p>
        <p class="act">对产品线</p>
        <p class="why">从最轻形态起步</p>
      </RouterLink>
      <RouterLink class="fstep" to="/sites">
        <p class="no">04 · 接本地</p>
        <p class="act">挂网点</p>
        <p class="why">就近落地不遥控</p>
      </RouterLink>
      <a
        v-if="AI_ASSISTANT_URL"
        class="fstep end"
        :href="AI_ASSISTANT_URL"
        target="_blank"
        rel="noopener"
      >
        <p class="no">05 · 拿方案</p>
        <p class="act">AI 生成 ↗</p>
        <p class="why">结构化打法当场出</p>
      </a>
      <span v-else class="fstep end is-off" aria-disabled="true">
        <p class="no">05 · 拿方案</p>
        <p class="act">AI 生成 ↗</p>
        <p class="why">结构化打法当场出</p>
      </span>
    </div>
    <div class="flow-note">
      <span>供应商选型在第三步介入 · 跟场景走，不跟品牌走</span>
      <a
        v-if="AI_ASSISTANT_URL"
        :href="AI_ASSISTANT_URL"
        target="_blank"
        rel="noopener"
      >直接问 AI 助手 ↗</a>
      <span v-else class="off" aria-disabled="true">直接问 AI 助手 ↗</span>
    </div>
  </section>
</template>

<script setup>
import { AI_ASSISTANT_URL } from '../../config/links'
</script>

<style scoped>
.sec {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
  padding-block: 78px 10px;
}

.sec-kicker {
  font-size: 11px;
  letter-spacing: .3em;
  color: var(--accent);
  font-weight: 500;
  margin-bottom: 14px;
}

.sec-h {
  font-family: var(--serif);
  font-size: 34px;
  font-weight: 500;
  color: var(--ink);
  letter-spacing: .01em;
}

.sec-lead {
  color: var(--muted);
  font-size: 13.5px;
  line-height: 1.8;
  margin-top: 10px;
}

.flow {
  display: flex;
  align-items: stretch;
  gap: 12px;
  margin-top: 34px;
}

.fstep {
  flex: 1;
  min-width: 0;
  background: rgba(255, 255, 255, .9);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 25px 22px 24px;
  box-shadow: 0 20px 42px -28px rgba(80, 110, 200, .28), inset 0 1px 0 rgba(255, 255, 255, .95);
  transition: transform .25s ease, box-shadow .25s ease;
}

.fstep:hover {
  transform: translateY(-3px);
  box-shadow: 0 28px 56px -28px rgba(80, 110, 200, .42);
}

.fstep .no {
  font-size: 10.5px;
  letter-spacing: .18em;
  color: var(--accent);
  font-weight: 500;
}

.fstep .act {
  font-family: var(--serif);
  font-size: 22px;
  margin-top: 12px;
  color: var(--ink);
}

.fstep .why {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.6;
  margin-top: 6px;
}

.fstep.end {
  background: linear-gradient(140deg, #3d76ff, var(--accent));
  border: 0;
}

.fstep.end .no,
.fstep.end .act,
.fstep.end .why {
  color: #fff;
}

.fstep.end .why {
  color: rgba(255, 255, 255, .78);
}

.fstep.end.is-off {
  cursor: not-allowed;
}

.flow-note {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
  padding-block: 16px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--muted);
  letter-spacing: .04em;
  flex-wrap: wrap;
}

.flow-note a {
  color: var(--accent);
}

.flow-note .off {
  color: var(--accent);
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .sec-h {
    font-size: 24px;
  }

  .flow {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
