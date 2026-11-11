<template>
  <header class="nav">
    <div class="nav-in">
      <RouterLink class="brand" to="/">FDE 智研社</RouterLink>
      <nav class="hnav">
        <RouterLink to="/" exact-active-class="on">首页</RouterLink>
        <RouterLink to="/articles" active-class="on">案例库</RouterLink>
        <RouterLink to="/talents" active-class="on" :class="{ on: active === 'talents' }">FDE 团队</RouterLink>
        <RouterLink to="/products" active-class="on">产品与服务</RouterLink>
        <RouterLink to="/sites" active-class="on">网点</RouterLink>
        <RouterLink to="/vendors" active-class="on">供应商</RouterLink>
      </nav>
      <a
        v-if="AI_ASSISTANT_URL"
        class="nav-cta"
        :href="AI_ASSISTANT_URL"
        target="_blank"
        rel="noopener"
      >AI 助手</a>
      <span v-else class="nav-cta is-off" aria-disabled="true">AI 助手</span>
    </div>
  </header>
</template>

<script setup>
import { AI_ASSISTANT_URL } from '../config/links'

defineProps({
  active: { type: String, default: '' },
})
</script>

<style scoped>
.nav {
  position: sticky;
  top: 0;
  z-index: 60;
  background: rgba(255, 255, 255, .82);
  -webkit-backdrop-filter: blur(16px) saturate(1.4);
  backdrop-filter: blur(16px) saturate(1.4);
  border-bottom: 1px solid var(--line);
}

.nav-in {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
  height: 60px;
  display: flex;
  align-items: center;
  gap: 38px;
}

.brand {
  font-family: var(--serif);
  color: var(--ink);
  font-size: 20px;
  letter-spacing: .1em;
}

.brand::before {
  content: "";
  display: inline-block;
  width: 18px;
  height: 18px;
  margin-right: 10px;
  border-radius: 5px;
  background: linear-gradient(140deg, #4d84ff, #2e6bff);
  transform: translateY(2px);
  box-shadow: 0 4px 10px -3px rgba(46, 107, 255, .55);
}

.hnav {
  display: flex;
  gap: 26px;
  margin-right: auto;
}

.hnav a {
  color: var(--muted);
  font-size: 13px;
  letter-spacing: .04em;
  transition: color .2s ease;
}

.hnav a:hover,
.hnav a.on {
  color: var(--accent);
}

.nav-cta {
  font-size: 12.5px;
  color: #fff;
  background: var(--accent);
  padding: 8px 17px;
  border-radius: 999px;
  letter-spacing: .04em;
  font-weight: 500;
  box-shadow: 0 8px 18px -8px rgba(46, 107, 255, .65);
  transition: all .2s ease;
}

.nav-cta:hover {
  background: var(--accent-deep);
  transform: translateY(-1px);
}

.nav-cta.is-off {
  cursor: not-allowed;
}

@media (max-width: 1000px) {
  .hnav {
    display: none;
  }
}
</style>
