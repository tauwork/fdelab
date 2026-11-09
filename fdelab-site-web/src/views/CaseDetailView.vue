<template>
  <div ref="pageRef" class="case-detail-page">
    <div ref="progressRef" class="progress"></div>

    <!-- TODO 文案待确认：加载态 -->
    <p v-if="loading" class="wrap status">正在加载…</p>
    <div v-else-if="error" class="wrap state">
      <p class="state-title">{{ error }}</p>
      <!-- TODO 文案待确认：错误态返回链接 -->
      <RouterLink class="state-link" to="/articles">← 返回案例库</RouterLink>
    </div>

    <template v-else-if="item">
      <div class="wrap">
        <section class="hero rv">
          <h1>{{ item.title }}</h1>
          <p class="h1-sub">{{ item.subtitle }}</p>

          <div class="fp">
            <span v-if="item.industry" class="chip">
              <em>行业</em><b>{{ item.industry }}</b>
            </span>
            <span v-if="item.industry" class="fp-sep"></span>
            <span v-if="item.fdeMode" class="chip">
              <em>模式</em><b>{{ item.fdeMode }}</b>
            </span>
            <span v-if="item.scale" class="chip">
              <em>规模</em><b>{{ item.scale }}</b>
            </span>
            <span v-if="item.cred" class="chip">
              <em>可信度</em><b><span class="badge" :class="credClass(item.cred)">{{ item.cred }}</span></b>
            </span>
            <span v-if="hasModelPct" class="chip">
              <em>模型含量</em><b>≈{{ item.modelPct }}%</b>
            </span>
          </div>
        </section>
      </div>

      <div class="anchor-bar">
        <nav class="anchor-in">
          <a
            v-for="(anchor, index) in ANCHORS"
            :key="anchor.id"
            :href="`#${anchor.id}`"
            :class="{ on: activeAnchor === index }"
            @click="onAnchorClick(anchor.id, $event)"
          >{{ anchor.label }}</a>
        </nav>
      </div>

      <div class="wrap">
        <div class="body">
          <aside ref="sideRef" class="side">
            <div class="card">
              <dl class="meta">
                <div v-for="row in metaRows" :key="row.label">
                  <dt>{{ row.label }}</dt>
                  <dd>{{ row.value }}</dd>
                </div>
              </dl>

              <div v-for="group in tagGroups" :key="group.title" class="tagset">
                <p>{{ group.title }}</p>
                <div class="tags">
                  <span v-for="tag in group.items" :key="tag" :class="group.cls">{{ tag }}</span>
                </div>
              </div>
            </div>
          </aside>

          <div class="main-col">
            <!-- 01 -->
            <section id="s1" class="sec rv">
              <div class="sec-head">
                <span class="no">01</span>
                <h2>触发事件与痛点</h2>
              </div>
              <div v-if="item.pain" class="warnbox"><b>{{ item.pain }}</b></div>
              <h3>业务目标（可量化）</h3>
              <div v-if="item.goal" class="quote">{{ item.goal }}</div>
            </section>

            <!-- 02 -->
            <section id="s2" class="sec rv">
              <div class="sec-head">
                <span class="no">02</span>
                <h2>解决方案：流程 · 工具 · AI</h2>
              </div>

              <div v-if="timeline.length" class="tl">
                <div v-for="(row, index) in timeline" :key="index" class="tl-i">
                  <div class="t">{{ row[0] }}</div>
                  <div class="d">{{ row[1] }}</div>
                </div>
              </div>

              <h3>组织与角色（谁上场）</h3>
              <table>
                <thead>
                  <tr>
                    <th style="width: 26%">角色</th>
                    <th style="width: 20%">配置</th>
                    <th>职责</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, index) in org" :key="index">
                    <td><b>{{ row[0] }}</b></td>
                    <td class="num accent">{{ row[1] }}</td>
                    <td>{{ row[2] }}</td>
                  </tr>
                </tbody>
              </table>

              <h3>能力归属检验（对结果负责的边界）</h3>
              <div class="card verdict-card">
                <dl class="vrows">
                  <div v-for="row in VERDICT_ROWS" :key="row.key" class="vrow">
                    <dt>{{ row.label }}</dt>
                    <dd><b>{{ verdictValue(row.key) }}</b></dd>
                  </div>
                </dl>
              </div>
            </section>

            <!-- 03 -->
            <section id="s3" class="sec rv">
              <div class="sec-head">
                <span class="no">03</span>
                <h2>业务效果</h2>
              </div>

              <div class="table-scroll">
                <table class="metrics-table">
                  <thead>
                    <tr>
                      <th style="width: 4%">#</th>
                      <th style="width: 17%">指标</th>
                      <th style="width: 24%">口径定义</th>
                      <th style="width: 13%">基线</th>
                      <th style="width: 19%">结果</th>
                      <th style="width: 13%">时间窗</th>
                      <th style="width: 10%">可信度</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(row, index) in metrics" :key="index">
                      <td class="num">{{ index + 1 }}</td>
                      <td><b>{{ row[0] }}</b></td>
                      <td>{{ row[1] }}</td>
                      <td class="num">{{ row[2] }}</td>
                      <td><span class="after">{{ row[3] }}</span></td>
                      <td>{{ row[4] }}</td>
                      <td><span class="badge" :class="credClass(row[5])">{{ row[5] }}</span></td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <template v-if="testimony.length">
                <h3>客户侧证言（具名，价值最高）</h3>
                <div class="quotes">
                  <div v-for="(row, index) in testimony" :key="index" class="quote">
                    {{ row[0] }}
                    <span class="src">— {{ row[1] }}</span>
                  </div>
                </div>
              </template>

              <div v-if="item.credNote" class="note">
                <b>口径说明</b> · 可信度 <b>{{ item.cred }}</b>：{{ item.credNote }}
              </div>
            </section>

            <!-- 04 -->
            <section id="s4" class="sec rv">
              <div class="sec-head">
                <span class="no">04</span>
                <h2>关键成功要素（CSF）</h2>
              </div>

              <div v-for="group in csfGroups" :key="group.dim" class="csf-group">
                <div class="csf-gt"><span>{{ group.dim }}</span></div>
                <div class="csf-grid">
                  <div v-for="(row, index) in group.items" :key="index" class="csf-card">
                    <div class="f">{{ row[1] }}</div>
                    <div class="w">
                      <i>✓ 为什么关键</i>
                      <span>{{ row[2] }}</span>
                    </div>
                    <div class="l">
                      <i>✗ 若缺失</i>
                      <span>{{ row[3] }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </section>

            <!-- 05 -->
            <section id="s5" class="sec rv">
              <div class="sec-head">
                <span class="no">05</span>
                <h2>一句话结论</h2>
              </div>

              <div class="dark">
                <p class="dk">ANSWER</p>
                <p class="q">客户拿到更强模型后，还需要请 FDE 吗？</p>
                <p>{{ item.verdict?.answer }}</p>
              </div>

              <template v-if="quotes.length">
                <h3>金句 / 原话存档</h3>
                <div class="quotes">
                  <div v-for="(row, index) in quotes" :key="index" class="quote">
                    「{{ row[0] }}」
                    <span class="src">— {{ row[1] }}</span>
                  </div>
                </div>
              </template>
            </section>

            <!-- 06 -->
            <section id="s6" class="sec rv">
              <div class="sec-head">
                <span class="no">06</span>
                <h2>相关产品与数据来源</h2>
              </div>

              <div class="two">
                <div class="card accent-card">
                  <h4>本案例对应的融合形态</h4>
                  <template v-if="products.length">
                    <div v-for="(product, index) in products" :key="index" class="prod">
                      <div class="f">{{ product?.form }}</div>
                      <div class="n">{{ product?.name }}</div>
                      <div class="m">{{ product?.cycle }} · {{ product?.pricing }}</div>
                    </div>
                  </template>
                  <p v-else class="empty-note">本案例为方法论基础，见产品线。</p>
                </div>

                <div class="card">
                  <h4>数据来源（全部可回查）</h4>
                  <div class="srcs">
                    <template v-for="(row, index) in sources" :key="index">
                      <a
                        v-if="isSafeUrl(row[1])"
                        :href="row[1]"
                        target="_blank"
                        rel="noopener"
                      >
                        {{ row[0] }}
                        <span>{{ row[2] }}</span>
                      </a>
                      <div v-else class="src-row">
                        {{ row[0] }}
                        <span>{{ row[2] }}</span>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCaseDetail } from '../api/case'

const route = useRoute()

const item = ref(null)
const error = ref('')
const loading = ref(false)

const pageRef = ref(null)
const progressRef = ref(null)
const sideRef = ref(null)
const activeAnchor = ref(0)

let requestSeq = 0
let observer = null

const BAR_BOTTOM = 106

const ANCHORS = [
  { id: 's1', label: '痛点与目标' },
  { id: 's2', label: '解决方案' },
  { id: 's3', label: '业务效果' },
  { id: 's4', label: '关键成功要素' },
  { id: 's5', label: '一句话结论' },
  { id: 's6', label: '产品与来源' },
]

const VERDICT_ROWS = [
  { key: 'deposit', label: '能力沉淀在哪一侧' },
  { key: 'modelPct', label: '模型含量估算' },
  { key: 'reusable', label: '可复用资产 / 产品化率' },
  { key: 'economics', label: '单位经济学走向' },
  { key: 'survive', label: '会被下一代模型冲掉吗' },
]

const timeline = computed(() => (Array.isArray(item.value?.timeline) ? item.value.timeline : []))
const org = computed(() => (Array.isArray(item.value?.org) ? item.value.org : []))
const metrics = computed(() => (Array.isArray(item.value?.metrics) ? item.value.metrics : []))
const testimony = computed(() => (Array.isArray(item.value?.testimony) ? item.value.testimony : []))
const quotes = computed(() => (Array.isArray(item.value?.quotes) ? item.value.quotes : []))
const sources = computed(() => (Array.isArray(item.value?.sources) ? item.value.sources : []))
const products = computed(() => (Array.isArray(item.value?.products) ? item.value.products : []))

const hasModelPct = computed(
  () => item.value?.modelPct !== null && item.value?.modelPct !== undefined,
)

const metaRows = computed(() => {
  const data = item.value
  if (!data) return []
  const rows = []
  const partner = data.partner || data.primaryFde?.name
  const client = data.customer?.displayName || data.clientName
  if (partner) rows.push({ label: '交付方 / FDE 主体', value: partner })
  if (client) rows.push({ label: '客户 / 场景方', value: client })
  if (data.occurYear) rows.push({ label: '发生时间', value: data.occurYear })
  if (data.func) rows.push({ label: '业务职能', value: data.func })
  rows.push({
    label: '硬指标数',
    value: `${metrics.value.length} 项（口径逐项标注）`,
  })
  return rows
})

const tagGroups = computed(() => {
  const data = item.value
  if (!data) return []
  return [
    { title: '技术标签', items: data.tech || [], cls: '' },
    { title: 'AI 能力', items: data.ai || [], cls: 'ai' },
    { title: '管理标签', items: data.mgmt || [], cls: 'md' },
  ].filter((group) => Array.isArray(group.items) && group.items.length)
})

const csfGroups = computed(() => {
  const groups = []
  const byDim = new Map()
  for (const row of item.value?.csf || []) {
    const dim = row[0]
    if (!byDim.has(dim)) {
      const group = { dim, items: [] }
      byDim.set(dim, group)
      groups.push(group)
    }
    byDim.get(dim).items.push(row)
  }
  return groups
})

function credClass(code) {
  const value = String(code || '').toLowerCase()
  return ['a', 'b', 'c', 'd'].includes(value) ? value : 'd'
}

function verdictValue(key) {
  return item.value?.verdict?.[key] ?? ''
}

function isSafeUrl(url) {
  return /^https?:\/\//i.test(String(url || ''))
}

function onAnchorClick(id, event) {
  event.preventDefault()
  const section = pageRef.value?.querySelector(`#${id}`)
  if (!section) return
  const reduced = typeof window.matchMedia === 'function'
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches
  section.scrollIntoView({ behavior: reduced ? 'auto' : 'smooth', block: 'start' })
  if (window.history?.replaceState) {
    window.history.replaceState(null, '', `#${id}`)
  }
}

function updateProgress() {
  const doc = document.documentElement
  const max = doc.scrollHeight - doc.clientHeight
  const percent = max > 0 ? (doc.scrollTop / max) * 100 : 0
  if (progressRef.value) {
    progressRef.value.style.width = `${percent}%`
  }
}

function syncToc() {
  if (!pageRef.value) return
  let index = 0
  ANCHORS.forEach((anchor, i) => {
    const section = pageRef.value.querySelector(`#${anchor.id}`)
    if (section && section.getBoundingClientRect().top < 140) {
      index = i
    }
  })
  activeAnchor.value = index
}

function centerSide() {
  const side = sideRef.value
  if (!side) return
  if (window.innerWidth <= 1080) {
    side.style.top = ''
    return
  }
  const centered = BAR_BOTTOM + Math.max(24, (window.innerHeight - BAR_BOTTOM - side.offsetHeight) / 2)
  let documentTop = 0
  let node = side
  while (node) {
    documentTop += node.offsetTop
    node = node.offsetParent
  }
  side.style.top = `${Math.round(Math.min(centered, documentTop))}px`
}

function setupReveal() {
  if (observer) {
    observer.disconnect()
    observer = null
  }
  const root = pageRef.value
  if (!root) return

  const elements = Array.from(root.querySelectorAll('.rv'))
  const reduced = typeof window.matchMedia === 'function'
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches

  if (reduced || !('IntersectionObserver' in window)) {
    elements.forEach((el) => el.classList.add('in'))
    return
  }

  elements.forEach((el) => {
    if (el.getBoundingClientRect().top < window.innerHeight - 40) {
      el.classList.add('in')
    }
  })

  observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('in')
        observer?.unobserve(entry.target)
      }
    })
  }, { rootMargin: '0px 0px -8% 0px', threshold: 0.06 })

  elements.forEach((el) => {
    if (!el.classList.contains('in')) {
      observer.observe(el)
    }
  })
}

function onScroll() {
  updateProgress()
  syncToc()
}

function onResize() {
  centerSide()
}

async function load(code) {
  const seq = ++requestSeq
  if (!code) {
    item.value = null
    error.value = '案例不存在'
    loading.value = false
    return
  }

  if (observer) {
    observer.disconnect()
    observer = null
  }

  loading.value = true
  error.value = ''
  item.value = null

  try {
    const data = await getCaseDetail(code)
    if (seq !== requestSeq) return
    item.value = data
    document.title = `${data.title} · FDE 智研社`
  } catch (err) {
    if (seq !== requestSeq) return
    error.value = err?.message || '加载失败'
    document.title = '案例详情 · FDE 智研社'
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }

  if (seq !== requestSeq) return
  await nextTick()
  updateProgress()
  if (item.value) {
    setupReveal()
    syncToc()
    centerSide()
  }
}

watch(
  () => route.params.id,
  (id) => load(String(id || '')),
  { immediate: true },
)

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('resize', onResize)
  updateProgress()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', onResize)
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<style scoped>
.case-detail-page {
  color: var(--ink);
  font-family: var(--sans);
  -webkit-font-smoothing: antialiased;
}

a {
  color: inherit;
  text-decoration: none;
}

.wrap {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
}

/* 进度条 */
.progress {
  position: fixed;
  top: 0;
  left: 0;
  height: 2px;
  width: 0;
  z-index: 200;
  background: linear-gradient(90deg, var(--accent), var(--accent-deep));
  box-shadow: 0 0 12px rgba(46, 107, 255, .6);
}

/* 页头 */
.hero {
  padding-top: 44px;
  padding-bottom: 48px;
}

h1 {
  font-family: var(--serif);
  font-size: clamp(31px, 4.2vw, 50px);
  font-weight: 400;
  line-height: 1.16;
  letter-spacing: .01em;
}

.h1-sub {
  font-family: var(--serif);
  font-size: clamp(18px, 2.1vw, 25px);
  font-weight: 400;
  line-height: 1.4;
  color: var(--muted);
  margin-top: 12px;
}

.fp {
  display: flex;
  gap: 10px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.chip {
  display: inline-flex;
  align-items: baseline;
  gap: 7px;
  font-size: 12px;
  letter-spacing: .02em;
  padding: 7px 12px;
  border-radius: 7px;
  background: rgba(255, 255, 255, .9);
  border: 1px solid var(--line);
  color: var(--ink);
}

.chip em {
  font-style: normal;
  font-size: 10px;
  letter-spacing: .12em;
  color: var(--muted);
}

.chip b {
  font-weight: 500;
  color: var(--ink);
}

.fp-sep {
  align-self: center;
  width: 1px;
  height: 18px;
  background: var(--line);
  margin: 0 2px;
}

/* 锚点条 */
.anchor-bar {
  position: sticky;
  top: 59px;
  z-index: 55;
  background: rgba(255, 255, 255, .85);
  -webkit-backdrop-filter: blur(14px) saturate(1.3);
  backdrop-filter: blur(14px) saturate(1.3);
  border-bottom: 1px solid var(--line);
}

.anchor-in {
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
  display: flex;
  gap: 6px;
  height: 46px;
  align-items: stretch;
  overflow-x: auto;
}

.anchor-in a {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 0 13px;
  font-size: 12.5px;
  letter-spacing: .02em;
  color: var(--muted);
  position: relative;
  white-space: nowrap;
  transition: color .2s;
}

.anchor-in a:hover {
  color: var(--ink);
}

.anchor-in a.on {
  color: var(--p);
}

.anchor-in a.on::after {
  content: "";
  position: absolute;
  left: 13px;
  right: 13px;
  bottom: 0;
  height: 2px;
  border-radius: 2px 2px 0 0;
  background: var(--accent);
}

/* 主体两栏 */
.body {
  display: grid;
  grid-template-columns: 316px minmax(0, 1fr);
  gap: 20px 44px;
  align-items: start;
  padding-top: 40px;
  padding-bottom: 20px;
}

.side {
  position: sticky;
  top: 128px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: rgba(255, 255, 255, .92);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--shadow);
}

.card h4 {
  font-family: var(--serif);
  font-size: 15px;
  font-weight: 400;
  letter-spacing: .02em;
  margin-bottom: 14px;
}

.meta {
  display: flex;
  flex-direction: column;
  gap: 9px;
  font-size: 12px;
}

.meta div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  line-height: 1.6;
}

.meta dt {
  color: var(--muted);
  letter-spacing: .02em;
  flex: 0 0 auto;
}

.meta dd {
  color: var(--ink);
  text-align: right;
  font-weight: 500;
}

.tagset {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.tagset:first-of-type {
  margin-top: 14px;
  padding-top: 0;
  border-top: 0;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tags span {
  font-size: 11px;
  color: var(--g);
  background: var(--g-bg);
  border-radius: 5px;
  padding: 3px 7px;
}

.tags span.ai {
  color: var(--p);
  background: var(--blue-soft);
}

.tags span.md {
  color: var(--md);
  background: var(--md-bg);
}

/* 区块 */
.main-col {
  min-width: 0;
}

.sec {
  padding-top: 6px;
  scroll-margin-top: 126px;
}

.sec + .sec {
  margin-top: 46px;
}

.sec-head {
  display: flex;
  align-items: baseline;
  gap: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 22px;
}

.sec-head .no {
  font-family: var(--serif);
  font-size: 13px;
  color: var(--accent);
  letter-spacing: .1em;
  font-variant-numeric: tabular-nums;
}

.sec-head h2 {
  font-family: var(--serif);
  font-size: 24px;
  font-weight: 400;
  letter-spacing: .01em;
}

h3 {
  font-family: var(--serif);
  font-size: 16px;
  font-weight: 400;
  margin: 24px 0 12px;
  letter-spacing: .02em;
}

/* 痛点 / 目标 */
.warnbox {
  background: var(--warn-bg);
  border: 1px solid var(--warn-line);
  border-left: 3px solid var(--warn);
  border-radius: 0 12px 12px 0;
  padding: 18px 20px;
  font-size: 13.5px;
  line-height: 1.95;
  color: var(--warn);
}

.quote {
  background: rgba(255, 255, 255, .72);
  border-left: 3px solid var(--accent);
  border-radius: 0 12px 12px 0;
  padding: 18px 20px;
  font-size: 13.5px;
  line-height: 1.95;
  color: var(--ink-soft);
}

.quote .src {
  display: block;
  margin-top: 8px;
  font-size: 11.5px;
  color: var(--muted);
  letter-spacing: .03em;
}

/* 时间线 */
.tl {
  position: relative;
  padding-left: 26px;
}

.tl::before {
  content: "";
  position: absolute;
  left: 5px;
  top: 6px;
  bottom: 6px;
  width: 1px;
  background: var(--line);
}

.tl-i {
  position: relative;
  padding: 0 0 20px;
}

.tl-i:last-child {
  padding-bottom: 0;
}

.tl-i::before {
  content: "";
  position: absolute;
  left: -25px;
  top: 6px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--card);
  border: 1.5px solid var(--g-line);
}

.tl-i .t {
  font-family: var(--serif);
  font-size: 16px;
  font-weight: 400;
  line-height: 1.5;
}

.tl-i .d {
  font-size: 12.5px;
  color: var(--muted);
  line-height: 1.8;
  margin-top: 4px;
}

/* 表格 */
table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

th {
  text-align: left;
  font-size: 10.5px;
  font-weight: 500;
  color: var(--muted);
  letter-spacing: .12em;
  padding: 0 12px 11px 0;
  border-bottom: 1px solid var(--line);
}

td {
  font-size: 12.8px;
  line-height: 1.75;
  padding: 14px 12px 14px 0;
  border-bottom: 1px solid var(--line);
  vertical-align: top;
  color: var(--ink-soft);
}

td b {
  font-weight: 500;
  color: var(--ink);
}

.num {
  font-variant-numeric: tabular-nums;
}

.accent {
  color: var(--accent);
}

.after {
  font-family: var(--serif);
  font-size: 16px;
  font-weight: 400;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
}

.badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: .06em;
  padding: 2px 7px;
  border-radius: 5px;
}

.badge.a {
  color: var(--a);
  background: var(--a-bg);
  border: 1px solid var(--a-line);
}

.badge.b {
  color: var(--p);
  background: var(--p-bg);
  border: 1px solid var(--p-line);
}

.badge.c {
  color: var(--c);
  background: var(--c-bg);
  border: 1px solid var(--c-line);
}

.badge.d {
  color: var(--g);
  background: var(--g-bg);
  border: 1px solid var(--g-line);
}

/* 归属检验 */
.verdict-card {
  border-left: 3px solid var(--accent);
}

.vrows {
  display: flex;
  flex-direction: column;
}

.vrow {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr);
  gap: 16px;
  padding: 12px 0;
  border-top: 1px solid var(--line);
  font-size: 12.8px;
}

.vrow:first-child {
  border-top: 0;
}

.vrow dt {
  color: var(--muted);
  letter-spacing: .02em;
  font-size: 12px;
}

.vrow dd {
  color: var(--ink);
  line-height: 1.7;
}

/* CSF */
.csf-group + .csf-group {
  margin-top: 20px;
}

.csf-gt {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.csf-gt span {
  font-size: 10.5px;
  letter-spacing: .14em;
  color: #fff;
  background: var(--ink-soft);
  border-radius: 5px;
  padding: 3px 8px;
}

.csf-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.csf-grid .csf-card:last-child:nth-child(odd) {
  grid-column: 1 / -1;
}

.csf-card {
  background: rgba(255, 255, 255, .9);
  border: 1px solid var(--line);
  border-left: 3px solid var(--g-line);
  border-radius: 0 12px 12px 0;
  padding: 14px 16px;
  box-shadow: var(--shadow);
  transition: .22s;
}

.csf-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 40px -30px rgba(80, 110, 200, .55);
}

.csf-card .f {
  font-size: 13px;
  font-weight: 500;
  line-height: 1.6;
}

.csf-card .w,
.csf-card .l {
  display: flex;
  gap: 8px;
  font-size: 12px;
  line-height: 1.7;
  margin-top: 9px;
}

.csf-card .w span {
  color: var(--ink-soft);
}

.csf-card .l span {
  color: var(--muted);
}

.csf-card .w i,
.csf-card .l i {
  flex: 0 0 auto;
  font-style: normal;
  font-size: 9.5px;
  letter-spacing: .08em;
  line-height: 1;
  padding: 4px 7px;
  border-radius: 4px;
  color: var(--muted);
  background: var(--g-bg);
  white-space: nowrap;
}

/* 结论深色块 */
.dark {
  border-radius: 20px;
  padding: 40px 44px;
  background: linear-gradient(120deg, var(--dark-1), var(--dark-2) 62%);
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 30px 60px -34px rgba(23, 28, 46, .65);
}

.dark::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 44% 38% at 88% 4%, rgba(90, 130, 255, .28), transparent 64%),
    radial-gradient(ellipse 40% 36% at 4% 100%, rgba(64, 98, 214, .24), transparent 66%);
}

.dark > * {
  position: relative;
  z-index: 1;
}

.dark .dk {
  font-size: 11px;
  letter-spacing: .3em;
  color: #7ea6ff;
  margin-bottom: 14px;
}

.dark .q {
  font-family: var(--serif);
  font-size: 21px;
  font-weight: 400;
  line-height: 1.6;
  margin-bottom: 16px;
}

.dark p {
  font-size: 13px;
  line-height: 2;
  color: rgba(255, 255, 255, .72);
  max-width: 800px;
}

/* 引用 */
.quotes {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quotes .quote {
  background: rgba(255, 255, 255, .9);
  box-shadow: var(--shadow);
  border-left-color: var(--g);
}

/* 产品 / 来源 */
.two {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.accent-card {
  border-left: 3px solid var(--accent);
}

.empty-note {
  font-size: 12.5px;
  color: var(--muted);
  line-height: 1.8;
}

.prod {
  display: block;
  padding: 16px 18px;
  border-radius: 12px;
  background: var(--blue-soft);
  border: 1px solid var(--p-line);
  transition: .2s;
  margin-bottom: 10px;
}

.prod:last-child {
  margin-bottom: 0;
}

.prod:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 34px -26px rgba(46, 107, 255, .6);
}

.prod .f {
  font-size: 10.5px;
  letter-spacing: .12em;
  color: var(--accent);
  font-weight: 600;
}

.prod .n {
  font-size: 15px;
  font-weight: 500;
  margin: 5px 0 4px;
}

.prod .m {
  font-size: 11.5px;
  color: var(--muted);
}

.srcs {
  display: flex;
  flex-direction: column;
}

.srcs a,
.srcs .src-row {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 10px 0;
  border-bottom: 1px solid var(--line);
  font-size: 12px;
  line-height: 1.6;
  transition: .2s;
}

.srcs a:last-child,
.srcs .src-row:last-child {
  border-bottom: 0;
}

.srcs a:hover {
  color: var(--accent);
}

.srcs span {
  color: var(--muted);
  font-size: 10.5px;
  flex: 0 0 auto;
  letter-spacing: .03em;
}

/* 口径说明 */
.note {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.85;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.note b {
  color: var(--ink-soft);
  font-weight: 500;
}

/* 状态 */
.status {
  padding-top: 48px;
  padding-bottom: 60px;
  text-align: center;
  color: var(--muted);
  font-size: 12.5px;
}

.state {
  padding-top: 72px;
  padding-bottom: 88px;
  text-align: center;
}

.state-title {
  font-family: var(--serif);
  font-size: 20px;
  color: var(--ink);
}

.state-link {
  display: inline-block;
  margin-top: 14px;
  font-size: 13px;
  color: var(--accent);
}

/* 进场 */
.rv {
  opacity: 0;
  transform: translateY(14px);
  transition: opacity .6s ease, transform .6s cubic-bezier(.2, .7, .3, 1);
}

.rv.in {
  opacity: 1;
  transform: none;
}

@media (prefers-reduced-motion: reduce) {
  .rv {
    opacity: 1;
    transform: none;
    transition: none;
  }
}

@media (max-width: 1080px) {
  .body {
    grid-template-columns: 1fr;
  }

  .side {
    position: static;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .side .card:first-child {
    grid-column: span 2;
  }

  .fp-sep {
    display: none;
  }
}

@media (max-width: 768px) {
  .anchor-in {
    gap: 2px;
  }

  .csf-grid,
  .two {
    grid-template-columns: 1fr;
  }

  .side {
    grid-template-columns: 1fr;
  }

  .side .card:first-child {
    grid-column: span 1;
  }

  .table-scroll {
    overflow-x: auto;
  }

  .metrics-table {
    min-width: 640px;
    table-layout: fixed;
  }

  .dark {
    padding: 28px 22px;
  }

  .vrow {
    grid-template-columns: 1fr;
    gap: 4px;
  }
}
</style>
