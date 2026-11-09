<template>
  <div class="profile-page">
    <SiteNav active="talents" />

    <main>
      <section v-if="loading" class="wrap state">
        <p class="state-title">正在加载…</p>
      </section>

      <section v-else-if="!profile" class="wrap state">
        <p class="state-title">{{ error || '还没有这位 FDE 的主页' }}</p>
        <RouterLink class="state-link" to="/talents">FDE 团队</RouterLink>
      </section>

      <template v-else>
        <section class="wrap">
          <div class="crumb"><RouterLink to="/talents">FDE 团队</RouterLink><i>/</i><span>{{ profile.name }}</span></div>
          <div class="phead">
            <h1>{{ profile.name }}</h1>
            <p class="sub">{{ profile.headline }}</p>
          </div>
        </section>

        <section class="wrap profile">
          <div class="pside">

            <div class="card idcard">
              <span class="ava-lg" :class="levelClass">{{ initial }}</span>
              <div class="idmeta">
                <span class="badge" :class="levelClass">{{ profile.level }} {{ profile.levelName }}</span>
                <div class="yrs">{{ profile.years }} 年经验 · {{ firstIndustry }}</div>
              </div>
            </div>

            <div v-if="hasRadar" class="card">
              <h4>能力雷达（五维）</h4>
              <div ref="radarWrap" class="radar-wrap" @pointerleave="hideTip">
                <svg viewBox="0 0 280 230" role="img" :aria-label="radarAria" @pointermove="onRadarMove">
                  <g fill="none" stroke="#e8ecf4" stroke-width="1">
                    <polygon points="140.0,98.5 158.5,112.0 151.5,133.8 128.5,133.8 121.5,112.0" />
                    <polygon points="140.0,79.0 177.1,105.9 162.9,149.6 117.1,149.6 102.9,105.9" />
                    <polygon points="140.0,59.5 195.6,99.9 174.4,165.3 105.6,165.3 84.4,99.9" />
                    <polygon points="140.0,40.0 214.2,93.9 185.8,181.1 94.2,181.1 65.8,93.9" />
                    <path d="M140 118 L140 40 M140 118 L214.2 93.9 M140 118 L185.8 181.1 M140 118 L94.2 181.1 M140 118 L65.8 93.9" />
                  </g>
                  <polygon
                    data-hit="area"
                    :points="radarPolygon"
                    :fill="radarFill"
                    :stroke="radarColor"
                    stroke-width="2"
                    stroke-linejoin="round"
                  />
                  <g :fill="radarColor">
                    <circle v-for="(dot, index) in radarDots" :key="`dot-${index}`" :cx="dot.x" :cy="dot.y" r="2.6" />
                  </g>
                  <g fill="transparent" pointer-events="all">
                    <circle v-for="(dot, index) in radarDots" :key="`hit-${index}`" data-hit="dot" :cx="dot.x" :cy="dot.y" r="13" />
                  </g>
                  <g font-size="10.5" fill="#66708a">
                    <text x="140" y="26" text-anchor="middle">工程实现</text>
                    <text x="222" y="88" text-anchor="start">业务翻译</text>
                    <text x="192" y="202" text-anchor="start">评测设计</text>
                    <text x="88" y="202" text-anchor="end">抽象复用</text>
                    <text x="58" y="88" text-anchor="end">现场推进</text>
                  </g>
                </svg>
                <div ref="tipEl" class="radar-tip" :class="{ show: tipVisible }">
                  <div class="rt-title">{{ profile.name }} {{ profile.level }}</div>
                  <ul>
                    <li v-for="(dim, index) in profile.radarDims" :key="dim">
                      <span>{{ dim }}</span><b>{{ profile.radar[index] }}</b>
                    </li>
                  </ul>
                </div>
              </div>
              <p class="cap">{{ profile.radarDims.join(' · ') }}（0–10）</p>
            </div>

            <div class="card">
              <h4>行业与技能</h4>
              <div class="tags">
                <span v-for="industry in profile.industries" :key="industry" class="ind">{{ industry }}</span>
              </div>
              <div class="skl">
                <span v-for="skill in profile.skills" :key="skill">{{ skill }}</span>
              </div>
            </div>

          </div>

          <div>

            <section class="sec">
              <h2>简介</h2>
              <div class="quote">{{ profile.summary }}</div>
            </section>

            <section class="sec">
              <h2>项目经历</h2>
              <table class="rt">
                <thead><tr><th style="width:42%">项目</th><th style="width:20%">角色</th><th>结果</th></tr></thead>
                <tbody>
                  <tr v-for="(project, index) in profile.projects" :key="index">
                    <td><b>{{ project.title }}</b></td>
                    <td>{{ project.role }}</td>
                    <td>{{ project.delivery }}</td>
                  </tr>
                </tbody>
              </table>
            </section>

            <section class="sec">
              <h2>能力说明</h2>
              <div class="grid2">
                <div v-if="hasStrength" class="card acc">
                  <h4>强项场景</h4>
                  <p>
                    <template v-for="(part, index) in strengthParts" :key="index">
                      <b v-if="part.bold">{{ part.text }}</b>
                      <template v-else>{{ part.text }}</template>
                    </template>
                  </p>
                </div>
                <div class="card nv">
                  <h4>认证与复核</h4>
                  <p>认证机构：TIIDA（国际数据与人工智能研究院）。本页为原型示例数据；正式版人才页由认证记录 + 项目复盘双重复核生成，项目指标沿用案例库同源口径与可信度分级。</p>
                </div>
              </div>
            </section>

          </div>
        </section>
      </template>
    </main>

    <SiteFooter />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SiteNav from '../components/SiteNav.vue'
import SiteFooter from '../components/SiteFooter.vue'
import { fetchTalentProfile } from '../api/talent'

const CENTER = { x: 140, y: 118 }
const OUTER = [
  { x: 140, y: 40 },
  { x: 214.2, y: 93.9 },
  { x: 185.8, y: 181.1 },
  { x: 94.2, y: 181.1 },
  { x: 65.8, y: 93.9 },
]
const LEVEL_COLORS = {
  m: { stroke: '#b42a3c', fill: 'rgba(180,42,60,.13)' },
  p: { stroke: '#1f56d6', fill: 'rgba(31,86,214,.13)' },
  a: { stroke: '#17744a', fill: 'rgba(23,116,74,.13)' },
}

const route = useRoute()
const loading = ref(true)
const error = ref('')
const profile = ref(null)
const radarWrap = ref(null)
const tipEl = ref(null)
const tipVisible = ref(false)

const levelClass = computed(() => {
  const level = String(profile.value?.level || '')
  const code = level.slice(level.lastIndexOf('-') + 1).toLowerCase()
  return LEVEL_COLORS[code] ? code : ''
})

const initial = computed(() => Array.from(String(profile.value?.name || '').trim())[0] || '?')
const firstIndustry = computed(() => profile.value?.industries?.[0] || '')
const radarColor = computed(() => LEVEL_COLORS[levelClass.value]?.stroke || LEVEL_COLORS.m.stroke)
const radarFill = computed(() => LEVEL_COLORS[levelClass.value]?.fill || LEVEL_COLORS.m.fill)
const hasRadar = computed(() => Array.isArray(profile.value?.radar)
  && profile.value.radar.length === 5
  && Array.isArray(profile.value?.radarDims)
  && profile.value.radarDims.length === 5)

const radarDots = computed(() => {
  const values = hasRadar.value ? profile.value.radar : []
  return values.map((value, index) => {
    const ratio = Number(value) / 10
    return {
      x: round1(CENTER.x + (OUTER[index].x - CENTER.x) * ratio),
      y: round1(CENTER.y + (OUTER[index].y - CENTER.y) * ratio),
    }
  })
})

const radarPolygon = computed(() => radarDots.value.map((dot) => `${dot.x},${dot.y}`).join(' '))

const radarAria = computed(() => {
  const dims = profile.value?.radarDims || []
  const values = profile.value?.radar || []
  return `五维能力雷达：${dims.map((dim, index) => `${dim} ${values[index]}`).join('、')}`
})

const strengthParts = computed(() => {
  const text = profile.value?.strengthText || ''
  const dim = profile.value?.strengthDim || ''
  if (!text) return []
  const index = dim ? text.indexOf(dim) : -1
  if (index < 0) return [{ text, bold: false }]
  return [
    { text: text.slice(0, index), bold: false },
    { text: dim, bold: true },
    { text: text.slice(index + dim.length), bold: false },
  ].filter((part) => part.text)
})

const hasStrength = computed(() => hasRadar.value && Boolean(profile.value?.strengthText))

function round1(value) {
  return Math.round(value * 10) / 10
}

function placeTip(event) {
  const wrap = radarWrap.value
  const tip = tipEl.value
  if (!wrap || !tip) return
  const box = wrap.getBoundingClientRect()
  const w = tip.offsetWidth
  const h = tip.offsetHeight
  const mx = event.clientX - box.left
  const my = event.clientY - box.top
  let x = mx + 14
  let y = my + 14
  if (x + w > box.width - 2) x = mx - w - 14
  x = Math.max(2, Math.min(x, box.width - w - 2))
  y = Math.max(2, Math.min(y, box.height - h - 2))
  tip.style.left = x + 'px'
  tip.style.top = y + 'px'
}

function onRadarMove(event) {
  if (!(event.target instanceof Element) || !event.target.closest('[data-hit]')) {
    tipVisible.value = false
    return
  }
  tipVisible.value = true
  placeTip(event)
}

function hideTip() {
  tipVisible.value = false
}

async function load(slug) {
  loading.value = true
  error.value = ''
  profile.value = null
  try {
    const data = await fetchTalentProfile(slug)
    profile.value = data
    document.title = `${data.name} · FDE 智研社`
  } catch (err) {
    error.value = err?.message || '主页加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load(String(route.params.slug || ''))
})
</script>

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.profile-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  color: var(--ink);
  font-family: var(--sans);
  background:
    radial-gradient(ellipse 46% 30% at 14% -6%, #e3ecfc 0%, transparent 62%),
    radial-gradient(ellipse 40% 26% at 88% 0%, #ece7fc 0%, transparent 58%),
    var(--paper);
  -webkit-font-smoothing: antialiased;
}

a {
  text-decoration: none;
  color: inherit;
}

.wrap {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
}

main {
  flex: 1 0 auto;
}

/* ===== 加载 / 空态 ===== */
.state {
  padding-top: 80px;
  text-align: center;
}

.state-title {
  color: var(--muted);
  font-size: 13.5px;
  line-height: 1.9;
}

.state-link {
  display: inline-block;
  margin-top: 12px;
  color: var(--accent);
  font-size: 12.5px;
  letter-spacing: .04em;
}

/* ===== 页头 ===== */
.crumb {
  margin-top: 24px;
  font-size: 12px;
  color: var(--muted);
  letter-spacing: .03em;
}

.crumb a {
  color: var(--accent);
}

.crumb i {
  font-style: normal;
  margin: 0 8px;
  color: #b9c2d6;
}

.phead {
  padding-top: 26px;
}

h1 {
  font-family: var(--serif);
  font-size: 46px;
  font-weight: 500;
  line-height: 1.26;
  letter-spacing: .01em;
  color: var(--ink);
}

.phead .sub {
  color: var(--muted);
  font-size: 14px;
  margin-top: 10px;
  letter-spacing: .02em;
}

/* ===== 两栏主体 ===== */
.profile {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
  margin-top: 30px;
}

.pside {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: rgba(255, 255, 255, .92);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 20px 44px -34px rgba(80, 110, 200, .34), inset 0 1px 0 rgba(255, 255, 255, .95);
}

.card h4 {
  font-family: var(--serif);
  font-size: 15px;
  font-weight: 500;
  letter-spacing: .02em;
  color: var(--ink);
  margin-bottom: 14px;
}

/* 身份卡：头像方块沿用 6.2 规格，详情页放大到 88px */
.idcard {
  display: flex;
  gap: 16px;
  align-items: center;
}

.ava-lg {
  flex: 0 0 88px;
  width: 88px;
  height: 88px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  font-family: var(--serif);
  font-size: 34px;
  font-weight: 500;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
}

.ava-lg.m {
  background: linear-gradient(140deg, #d8606f, var(--m));
  box-shadow: 0 12px 24px -14px rgba(180, 42, 60, .75);
}

.ava-lg.p {
  background: linear-gradient(140deg, #5c8bff, var(--p));
  box-shadow: 0 12px 24px -14px rgba(31, 86, 214, .75);
}

.ava-lg.a {
  background: linear-gradient(140deg, #4bb882, var(--a));
  box-shadow: 0 12px 24px -14px rgba(23, 116, 74, .75);
}

.idmeta .badge {
  display: inline-block;
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: .07em;
  padding: 3px 8px;
  border-radius: 5px;
  font-variant-numeric: tabular-nums;
}

.badge.m {
  color: var(--m);
  background: var(--m-bg);
  border: 1px solid var(--m-line);
}

.badge.p {
  color: var(--p);
  background: var(--p-bg);
  border: 1px solid var(--p-line);
}

.badge.a {
  color: var(--a);
  background: var(--a-bg);
  border: 1px solid var(--a-line);
}

.idmeta .yrs {
  font-size: 12px;
  color: var(--muted);
  margin-top: 10px;
  line-height: 1.6;
}

/* 雷达 */
.radar-wrap {
  width: 100%;
  position: relative;
}

.radar-wrap svg {
  display: block;
  width: 100%;
  height: auto;
}

.radar-wrap svg text {
  font-family: var(--sans);
}

/* 悬浮能力展示：交互对齐线上 ECharts tooltip（卡内跟随、触边翻转、只由数据区触发），视觉保持本站浅色 */
.radar-tip {
  position: absolute;
  z-index: 20;
  pointer-events: none;
  min-width: 143px;
  padding: 9px 13px 8px;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 10px;
  box-shadow: 0 16px 34px -18px rgba(23, 28, 46, .42);
  visibility: hidden;
  opacity: 0;
}

.radar-tip.show {
  visibility: visible;
  opacity: 1;
}

.radar-tip .rt-title {
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: .04em;
  color: var(--ink);
  padding-bottom: 6px;
  margin-bottom: 4px;
  border-bottom: 1px solid var(--line);
}

.radar-tip ul {
  list-style: none;
}

.radar-tip li {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  font-size: 11.5px;
  line-height: 1.7;
  color: var(--muted);
}

.radar-tip li b {
  font-weight: 500;
  color: var(--ink-soft);
  font-variant-numeric: tabular-nums;
}

.cap {
  font-size: 11px;
  color: var(--muted);
  line-height: 1.65;
  margin-top: 8px;
}

/* 行业与技能：沿用定稿名录页卡片的标签语言 */
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ind {
  font-size: 11px;
  color: #5b6480;
  background: #f2f5fa;
  border-radius: 5px;
  padding: 3px 7px;
}

.skl {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

.skl span {
  font-size: 11px;
  color: var(--p);
  background: var(--blue-soft);
  border-radius: 5px;
  padding: 3px 7px;
}

/* ===== 右栏区块 ===== */
.sec + .sec {
  margin-top: 32px;
}

.sec h2 {
  font-family: var(--serif);
  font-size: 21px;
  font-weight: 500;
  letter-spacing: .01em;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 18px;
}

.quote {
  font-size: 14px;
  line-height: 1.95;
  color: var(--ink-soft);
  background: rgba(255, 255, 255, .72);
  border-left: 3px solid var(--accent);
  border-radius: 0 10px 10px 0;
  padding: 18px 20px;
  max-width: 760px;
}

table.rt {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

table.rt th {
  text-align: left;
  font-size: 11px;
  font-weight: 500;
  color: var(--muted);
  letter-spacing: .12em;
  padding: 0 12px 12px 0;
  border-bottom: 1px solid var(--line);
}

table.rt td {
  font-size: 13px;
  line-height: 1.75;
  padding: 15px 12px 15px 0;
  border-bottom: 1px solid var(--line);
  vertical-align: top;
  color: var(--ink-soft);
}

table.rt td b {
  font-weight: 500;
  color: var(--ink);
}

table.rt tr:hover td {
  background: rgba(255, 255, 255, .72);
}

.grid2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.grid2 .card.acc {
  border-left: 3px solid var(--accent);
}

.grid2 .card.nv {
  border-left: 3px solid #8a94ad;
}

.grid2 .card p {
  font-size: 12.5px;
  line-height: 1.95;
  color: var(--muted);
}

.grid2 .card p b {
  color: var(--accent);
  font-weight: 500;
}

/* ===== 页脚（与定稿名录页同款：白底通栏，内容在版心） ===== */
:deep(.site-foot) {
  margin-top: 70px;
}

@media (max-width: 1080px) {
  .profile {
    grid-template-columns: 1fr;
  }

  .pside {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .pside .card:first-child {
    grid-column: span 2;
  }
}

@media (max-width: 760px) {
  h1 {
    font-size: 32px;
  }

  .pside,
  .grid2 {
    grid-template-columns: 1fr;
  }

  .pside .card:first-child {
    grid-column: span 1;
  }

  table.rt {
    table-layout: auto;
  }
}
</style>
