<template>
  <section class="hero-in">
    <div class="hero-grid">
      <div>
        <p class="kicker">FDE · 前沿部署工程师</p>
        <h1 class="hl">AI 落地的最后一公里，<br>我们陪跑到底。</h1>
        <p class="hl-sub">不仅仅是方法论。</p>
        <p class="lead">针对你的实际问题，FDE 实施可落地的部署方案，从方案到生产全程进驻陪跑。项目结束，留下的资产形态是 Eval——可版本化、可审计、可复用。</p>
        <div class="asset-cards">
          <div class="acard"><b>1 天</b><span>换团队重跑，不是 2 个月</span></div>
          <div class="acard"><b>增值</b><span>模型升级，不是归零</span></div>
        </div>
        <p class="lead">智研社把每一次交付沉淀成公开的知识库：标杆案例、人才画像、产品线打法、AI 方案生成器，人人可查。</p>
        <div class="hero-acts">
          <RouterLink class="hbtn solid" to="/articles">看跑通的案例 →</RouterLink>
          <RouterLink class="hbtn ghost" :to="{ hash: '#ruler' }">读三问检验 →</RouterLink>
        </div>
        <div class="hero-tags">
          <span>陪跑落地</span><span>Eval 可审计</span><span>全部公开</span>
        </div>
      </div>
      <LayerIsometric />
    </div>
  </section>
</template>

<script setup>
import LayerIsometric from './LayerIsometric.vue'
</script>

<style scoped>
.hero-in {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 560px;
  gap: 40px 56px;
  align-items: center;
  padding-block: 76px 76px;
}

.kicker {
  color: var(--accent);
  letter-spacing: .3em;
  font-size: 11px;
  margin-bottom: 20px;
  font-weight: 500;
}

h1.hl {
  font-family: var(--serif);
  font-size: 54px;
  font-weight: 500;
  line-height: 1.16;
  letter-spacing: .01em;
  color: var(--ink);
}

.hl-sub {
  font-family: var(--serif);
  font-size: 30px;
  font-weight: 500;
  line-height: 1.3;
  color: var(--muted);
  margin-top: 10px;
}

.lead {
  color: var(--muted);
  font-size: 14px;
  line-height: 1.95;
  max-width: 520px;
  margin-top: 24px;
}

.lead + .lead {
  margin-top: 12px;
}

.asset-cards {
  display: flex;
  gap: 12px;
  margin-top: 22px;
  max-width: 520px;
}

.acard {
  flex: 1;
  min-width: 0;
  background: rgba(255, 255, 255, .9);
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 20px 42px -26px rgba(80, 110, 200, .28), inset 0 1px 0 rgba(255, 255, 255, .95);
}

.acard b {
  display: block;
  font-family: var(--serif);
  font-size: 20px;
  font-weight: 500;
  color: var(--ink);
}

.acard span {
  display: block;
  margin-top: 5px;
  font-size: 12px;
  color: var(--muted);
  letter-spacing: .04em;
}

.hero-acts {
  display: flex;
  gap: 12px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.hbtn {
  display: inline-flex;
  align-items: center;
  height: 44px;
  padding: 0 26px;
  border-radius: 999px;
  font-size: 13.5px;
  letter-spacing: .03em;
  transition: all .2s ease;
}

.hbtn.solid {
  background: var(--accent);
  color: #fff;
  box-shadow: 0 14px 30px -12px rgba(46, 107, 255, .7);
}

.hbtn.solid:hover {
  background: var(--accent-deep);
  transform: translateY(-1px);
}

.hbtn.ghost {
  border: 1px solid #d5deee;
  color: var(--ink);
  background: rgba(255, 255, 255, .7);
}

.hbtn.ghost:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.hero-tags {
  display: flex;
  gap: 9px;
  margin-top: 26px;
  flex-wrap: wrap;
}

.hero-tags span {
  font-size: 12px;
  letter-spacing: .05em;
  color: #4a5578;
  border: 1px solid #dbe3f2;
  border-radius: 999px;
  padding: 6px 15px;
  background: rgba(255, 255, 255, .75);
}

@media (max-width: 1000px) {
  .hero-grid {
    grid-template-columns: 1fr;
    gap: 40px;
    padding-block: 48px 40px;
  }

  h1.hl {
    font-size: 38px;
  }

  .hl-sub {
    font-size: 24px;
  }
}

@media (max-width: 640px) {
  h1.hl {
    font-size: 31px;
  }

  .hl-sub {
    font-size: 22px;
  }

  .asset-cards {
    flex-direction: column;
  }
}
</style>
