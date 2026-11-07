<template>
  <footer class="site-foot">
    <span class="fbrand">FDE 智研社</span>
    <span>把 AI 落地的经验，沉淀成人人可查的公开知识库</span>
    <span>© 2026 FDE智研社</span>
  </footer>
</template>

<style scoped>
.site-foot {
  padding-block: 34px;
  padding-inline: max(var(--gut), calc((100% - var(--wrap)) / 2));
  background: rgba(255, 255, 255, .9);
  border-top: 1px solid var(--line);
  color: var(--muted);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.site-foot .fbrand {
  font-family: var(--serif);
  color: var(--ink);
  font-size: 17px;
  letter-spacing: .09em;
}

.site-foot span {
  font-size: 12px;
  letter-spacing: .05em;
}
</style>
