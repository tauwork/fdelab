<template>
  <div class="roster-page">
    <div class="case-body">
      <section class="wrap phead">
        <h1>FDE 标杆案例库</h1>
        <p class="lead">
          <span>每个案例都回答同一组问题：<b>智能沉淀在哪一侧、值多少钱、能不能复制</b>。</span>
          <span>硬指标逐项标注口径与可信度。</span>
        </p>
      </section>

      <section class="wrap toolbar">
        <div class="search">
          <span class="mag"></span>
          <input
            v-model="searchInput"
            type="search"
            placeholder="搜案例、行业、技术"
            autocomplete="off"
            @input="onSearchInput"
          />
        </div>

        <div ref="industryDd" class="dd" :class="{ open: industryOpen }">
          <button
            class="dd-trigger"
            :class="{ on: industryOpen }"
            type="button"
            @click.stop="industryOpen = !industryOpen"
          >
            <span>行业</span>
            <span class="cv">{{ industry }}</span>
            <span class="arw"></span>
          </button>
          <div class="dd-panel">
            <template v-for="(opt, i) in industryOptions" :key="opt.name">
              <div v-if="i === 1" class="sep"></div>
              <button
                class="opt"
                :class="{ on: industry === opt.name }"
                type="button"
                @click="selectIndustry(opt.name)"
              >
                <span class="nm">{{ opt.name }}</span>
                <span class="ct">{{ opt.count }}</span>
              </button>
            </template>
          </div>
        </div>

        <div class="chips">
          <button
            v-for="opt in credOptions"
            :key="opt.code"
            class="chip"
            :class="{ on: cred === opt.code }"
            type="button"
            @click="selectCred(opt.code)"
          >
            {{ opt.code === '全部' ? '全部可信度' : opt.code }}
            <span v-if="opt.code === '全部'" class="legend">
              <div v-for="lv in CRED_LEVELS" :key="lv[0]">
                <span class="tb" :class="lv[0].toLowerCase()">{{ lv[0] }}</span>{{ lv[1] }}
              </div>
            </span>
          </button>
        </div>

        <div class="tb-right">
          <div class="viewsw">
            <button
              :class="{ on: view === 'cards' }"
              type="button"
              title="卡片视图"
              @click="view = 'cards'"
            >
              <svg width="14" height="14" viewBox="0 0 14 14" fill="currentColor">
                <rect x="0" y="0" width="6" height="6" rx="1.5" /><rect x="8" y="0" width="6" height="6" rx="1.5" />
                <rect x="0" y="8" width="6" height="6" rx="1.5" /><rect x="8" y="8" width="6" height="6" rx="1.5" />
              </svg>
            </button>
            <button
              :class="{ on: view === 'table' }"
              type="button"
              title="列表视图"
              @click="view = 'table'"
            >
              <svg width="14" height="14" viewBox="0 0 14 14" fill="currentColor">
                <rect x="0" y="1" width="14" height="2" rx="1" /><rect x="0" y="6" width="14" height="2" rx="1" />
                <rect x="0" y="11" width="14" height="2" rx="1" />
              </svg>
            </button>
          </div>
        </div>
      </section>

      <section class="wrap roster">
        <div class="rhead">
          <h2>全部案例</h2>
          <span class="count">共 {{ pageData.total }} 个</span>
        </div>

        <div class="cards" :hidden="view !== 'cards'">
          <RouterLink
            v-for="item in records"
            :key="item.code"
            class="card"
            :to="`/articles/${item.code}`"
          >
            <div class="ctop">
              <span class="ava" :class="credClass(item)">{{ initial(item) }}</span>
              <div class="ctxt">
                <div class="nm">
                  <b>{{ item.title }}</b>
                  <span class="cred" :class="credClass(item)">
                    {{ item.cred }}
                    <span class="tnote">
                      <b>{{ credNoteParts(item.credNote)[0] }}</b>
                      <span v-if="credNoteParts(item.credNote)[1]">{{ credNoteParts(item.credNote)[1] }}</span>
                    </span>
                  </span>
                </div>
                <div class="role">{{ item.subtitle }}</div>
                <div v-if="metaParts(item).length" class="meta">
                  <template v-for="(part, i) in metaParts(item)" :key="i">
                    <span v-if="i > 0">　·　</span>
                    <span v-if="part.label">{{ part.label }}<b>{{ part.value }}</b></span>
                    <span v-else>{{ part.value }}</span>
                  </template>
                </div>
              </div>
            </div>

            <p class="bio">{{ item.pain }}</p>

            <div class="tags">
              <span class="ind">{{ item.industry }}</span>
            </div>

            <div class="skl">
              <span v-for="tech in item.tech || []" :key="tech">{{ tech }}</span>
            </div>

            <div v-if="(item.kpis || []).length" class="kpi">
              <div v-for="(kpi, i) in item.kpis" :key="i" class="kv">
                <div class="v" :class="{ r: kpi.r === true }">{{ kpi.v }}</div>
                <div class="k">{{ kpi.k }}</div>
              </div>
            </div>

            <div class="cfoot">
              <span class="cnt">硬指标 <b>{{ item.metricCount }}</b></span>
              <span class="more">查看 <i>→</i></span>
            </div>
          </RouterLink>
        </div>

        <div :hidden="view !== 'table'">
          <table class="rt">
            <thead>
              <tr>
                <th style="width: 96px">编号</th>
                <th>案例</th>
                <th style="width: 150px">行业</th>
                <th style="width: 96px">可信度</th>
                <th style="width: 112px">交付模式</th>
                <th style="width: 128px">规模</th>
                <th style="width: 62px"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in records" :key="item.code">
                <td class="cid-sm">{{ item.code }}</td>
                <td>
                  <div class="tname"><b>{{ item.title }}</b></div>
                  <div class="dir" style="font-size: 12px; margin-top: 2px">{{ item.subtitle }}</div>
                </td>
                <td class="dir">{{ item.industry }}</td>
                <td>
                  <span class="cred" :class="credClass(item)">
                    {{ item.cred }} {{ credMeaning(item.cred) }}
                  </span>
                </td>
                <td class="dir">{{ item.fdeMode }}</td>
                <td class="dir">{{ item.scale }}</td>
                <td>
                  <RouterLink class="go" :to="`/articles/${item.code}`">查看 →</RouterLink>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="!records.length && !loading && !error" class="empty">
          <b>没有匹配的案例</b>
          换个筛选条件试试。<br />
          <small>案例库正在建设：当前实收 {{ facets.total }} 个标杆案例，后续按 Palantir 系 / 前沿实验室 / AI Native / 企业内部四组持续扩充。</small>
        </div>

        <div v-if="hasMore" class="more-row">
          <button class="load-more" type="button" :disabled="loadingMore" @click="loadMore">
            {{ loadingMore ? '加载中…' : '加载更多' }}
          </button>
        </div>

        <!-- TODO 文案待确认：错误态与加载态 -->
        <p v-if="error" class="notice">{{ error }} <button type="button" @click="reload">重试</button></p>
        <p v-if="loading && !records.length" class="loading-line">正在加载…</p>
        <p v-else-if="loading" class="loading-line soft">更新中…</p>
      </section>

      <section class="wrap">
        <div class="principles">
          <div class="pr">
            <span class="no">①</span>
            <h3>不虚报数量</h3>
            <p>实收多少写多少。对外的数字红线：<b>正在建设 FDE 标杆案例库</b>，补齐后再报数。</p>
          </div>
          <div class="pr">
            <span class="no">②</span>
            <h3>口径先于数字</h3>
            <p>每个指标标注口径定义、时间窗与可信度。<b>「识别出的节省」≠「已实现的收益」</b>，引用必须带前缀。</p>
          </div>
          <div class="pr">
            <span class="no">③</span>
            <h3>说破短板</h3>
            <p>主动指出 Palantir 官方材料<b>未使用 FDE 头衔</b>、案例时代错配、平台侧指标是规模而非效益——先说破比被追问更显专业。</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { fetchCaseFacets, fetchCases } from '../api/case'

const SIZE = 12

const CRED_LEVELS = [
  ['A', '客户侧证实'],
  ['B', '厂商具名'],
  ['C', '厂商匿名'],
  ['D', '二手转述'],
]

const searchInput = ref('')
const q = ref('')
const industry = ref('全部')
const cred = ref('全部')
const view = ref('cards')
const industryOpen = ref(false)
const industryDd = ref(null)

const records = ref([])
const pageData = ref({ total: 0, page: 1, size: SIZE, records: [] })
const facets = ref({ industries: [], creds: [], total: 0 })
const loading = ref(false)
const loadingMore = ref(false)
const error = ref('')
const currentPage = ref(1)

let searchTimer = 0
let requestSeq = 0

const hasMore = computed(() => Number(pageData.value.total) > records.value.length)

const industryOptions = computed(() => [
  { name: '全部', count: Number(facets.value.total) || 0 },
  ...(facets.value.industries || []).map((item) => ({
    name: item.name,
    count: Number(item.count) || 0,
  })),
])

const credOptions = computed(() => [
  { code: '全部', count: Number(facets.value.total) || 0 },
  ...(facets.value.creds || [])
    .filter((item) => Number(item.count) > 0)
    .map((item) => ({ code: item.code, count: Number(item.count) })),
])

function initial(item) {
  const source = String(item?.partner || item?.title || '').trim()
  const firstWord = source.split(/\s+/)[0] || ''
  return Array.from(firstWord)[0]?.toUpperCase() || '?'
}

function credClass(item) {
  return String(item?.cred || '').toLowerCase() || 'd'
}

function credMeaning(code) {
  return CRED_LEVELS.find((item) => item[0] === code)?.[1] || ''
}

function credNoteParts(note) {
  const match = /^(.*?)（(.*?)）$/.exec(String(note || ''))
  return match ? [match[1], match[2]] : [String(note || ''), '']
}

function metaParts(item) {
  const parts = []
  if (item?.clientName) {
    parts.push({ label: '客户：', value: item.clientName })
  }
  if (item?.occurYear) {
    parts.push({ value: item.occurYear })
  }
  if (item?.func) {
    parts.push({ value: item.func })
  }
  return parts
}

async function loadFacets() {
  try {
    const data = await fetchCaseFacets()
    facets.value = data || { industries: [], creds: [], total: 0 }
  } catch {
    // 筛选项失败不阻断列表；空态计数会退化为 0。
  }
}

async function loadPage(targetPage, append) {
  const seq = ++requestSeq
  if (append) loadingMore.value = true
  else loading.value = true
  error.value = ''
  try {
    const data = await fetchCases({
      page: targetPage,
      size: SIZE,
      q: q.value.trim() || undefined,
      industry: industry.value === '全部' ? undefined : industry.value,
      cred: cred.value === '全部' ? undefined : cred.value,
      sort: 'code',
    })
    if (seq !== requestSeq) return
    const page = data || { total: 0, page: targetPage, size: SIZE, records: [] }
    pageData.value = page
    const next = Array.isArray(page.records) ? page.records : []
    records.value = append ? [...records.value, ...next] : next
    currentPage.value = targetPage
  } catch (err) {
    if (seq !== requestSeq) return
    error.value = err?.message || '加载失败'
    if (!append) {
      records.value = []
      pageData.value = { total: 0, page: targetPage, size: SIZE, records: [] }
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false
      loadingMore.value = false
    }
  }
}

function reload() {
  currentPage.value = 1
  loadPage(1, false)
}

function loadMore() {
  if (loading.value || loadingMore.value || !hasMore.value) return
  loadPage(currentPage.value + 1, true)
}

function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    q.value = searchInput.value
    reload()
  }, 300)
}

function selectIndustry(name) {
  industryOpen.value = false
  if (industry.value === name) return
  industry.value = name
  reload()
}

function selectCred(code) {
  if (cred.value === code) return
  cred.value = code
  reload()
}

function onDocumentClick(event) {
  if (industryDd.value && !industryDd.value.contains(event.target)) {
    industryOpen.value = false
  }
}

onMounted(() => {
  document.title = 'FDE 标杆案例库 · FDE 智研社'
  document.addEventListener('click', onDocumentClick)
  loadFacets()
  reload()
})

onBeforeUnmount(() => {
  clearTimeout(searchTimer)
  document.removeEventListener('click', onDocumentClick)
})
</script>

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

[hidden] {
  display: none !important;
}

.roster-page {
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

.case-body {
  flex: 1 0 auto;
}

/* ===== 页头 ===== */
.phead {
  padding-top: 34px;
}

h1 {
  font-family: var(--serif);
  font-size: 46px;
  font-weight: 500;
  line-height: 1.26;
  letter-spacing: .01em;
  color: var(--ink);
}

.phead .lead {
  color: var(--muted);
  font-size: 14px;
  line-height: 1.95;
  max-width: 640px;
  margin-top: 20px;
}

.phead .lead b {
  color: var(--ink);
  font-weight: 500;
}

.phead .lead span {
  display: block;
}

/* ===== 可信度四色 ===== */
.cred {
  display: inline-block;
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: .07em;
  padding: 2px 8px;
  border-radius: 5px;
  font-variant-numeric: tabular-nums;
}

.cred.a {
  color: var(--a);
  background: var(--a-bg);
  border: 1px solid var(--a-line);
}

.cred.b {
  color: var(--p);
  background: var(--p-bg);
  border: 1px solid var(--p-line);
}

.cred.c {
  color: #a3690b;
  background: #fdf4e3;
  border: 1px solid #f0dcbb;
}

.cred.d {
  color: var(--muted);
  background: #f0f3f8;
  border: 1px solid #dfe4ee;
}

/* 卡片可信度角标悬浮：主标题 + 副标题 */
.card .cred {
  position: relative;
  z-index: 3;
  cursor: default;
}

.cred .tnote {
  position: absolute;
  top: calc(100% + 9px);
  left: 0;
  z-index: 30;
  width: 216px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  box-shadow: 0 18px 38px -20px rgba(80, 110, 200, .5);
  padding: 11px 13px;
  display: none;
  text-align: left;
  cursor: default;
}

.cred .tnote::before {
  content: "";
  position: absolute;
  top: -5px;
  left: 12px;
  width: 8px;
  height: 8px;
  background: #fff;
  border-left: 1px solid var(--line);
  border-top: 1px solid var(--line);
  transform: rotate(45deg);
}

.cred .tnote::after {
  content: "";
  position: absolute;
  top: -10px;
  left: 0;
  right: 0;
  height: 10px;
}

.cred:hover .tnote {
  display: block;
}

.cred .tnote b {
  display: block;
  font-family: var(--serif);
  font-weight: 500;
  font-size: 13px;
  color: var(--ink);
  letter-spacing: .02em;
}

.cred .tnote span {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: var(--muted);
  line-height: 1.65;
  letter-spacing: .02em;
}

/* ===== 工具条 ===== */
.toolbar {
  margin-top: 44px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search {
  position: relative;
  flex: 0 0 258px;
}

.search input {
  width: 100%;
  height: 38px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  padding: 0 12px 0 33px;
  font-size: 13px;
  color: var(--ink);
  font-family: var(--sans);
  outline: none;
  transition: border-color .2s, box-shadow .2s;
}

.search input:focus {
  border-color: #b9cdf5;
  box-shadow: 0 0 0 3px rgba(46, 107, 255, .1);
}

.search input::placeholder {
  color: #9aa3b8;
}

.search .mag {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 13px;
  height: 13px;
  border: 1.6px solid #9aa3b8;
  border-radius: 50%;
}

.search .mag::after {
  content: "";
  position: absolute;
  left: 10px;
  top: 11px;
  width: 6px;
  height: 1.6px;
  background: #9aa3b8;
  transform: rotate(45deg);
}

.chips {
  display: flex;
  gap: 7px;
  flex-wrap: wrap;
}

.chip {
  position: relative;
  font-size: 12.5px;
  color: var(--muted);
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 8px 15px;
  cursor: pointer;
  letter-spacing: .03em;
  transition: all .18s ease;
  font-family: var(--sans);
}

.chip:hover {
  color: var(--ink);
  border-color: #ccd6e8;
}

.chip.on {
  color: #fff;
  background: var(--ink);
  border-color: var(--ink);
  box-shadow: 0 8px 18px -12px rgba(23, 28, 46, .9);
}

.chip .legend {
  position: absolute;
  top: calc(100% + 9px);
  left: 0;
  z-index: 40;
  width: 196px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  box-shadow: 0 18px 38px -20px rgba(80, 110, 200, .5);
  padding: 9px 10px;
  display: none;
  text-align: left;
  cursor: default;
}

.chip .legend::before {
  content: "";
  position: absolute;
  top: -5px;
  left: 16px;
  width: 8px;
  height: 8px;
  background: #fff;
  border-left: 1px solid var(--line);
  border-top: 1px solid var(--line);
  transform: rotate(45deg);
}

.chip .legend::after {
  content: "";
  position: absolute;
  top: -10px;
  left: 0;
  right: 0;
  height: 10px;
}

.chip:hover .legend {
  display: block;
}

.chip .legend div {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11.5px;
  color: var(--muted);
  letter-spacing: .02em;
  padding: 5px 6px;
  border-radius: 6px;
  line-height: 1.5;
  white-space: nowrap;
}

.chip .legend .tb {
  flex: 0 0 auto;
  font-size: 9.5px;
  padding: 1px 6px;
  letter-spacing: .05em;
}

.chip .legend .tb.a {
  color: var(--a);
  background: var(--a-bg);
  border: 1px solid var(--a-line);
}

.chip .legend .tb.b {
  color: var(--p);
  background: var(--p-bg);
  border: 1px solid var(--p-line);
}

.chip .legend .tb.c {
  color: #a3690b;
  background: #fdf4e3;
  border: 1px solid #f0dcbb;
}

.chip .legend .tb.d {
  color: var(--muted);
  background: #f0f3f8;
  border: 1px solid #dfe4ee;
}

.dd {
  position: relative;
  flex: 0 0 auto;
}

.dd-trigger {
  height: 38px;
  display: inline-flex;
  align-items: center;
  gap: 9px;
  font-size: 12.5px;
  color: var(--muted);
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 0 13px;
  cursor: pointer;
  letter-spacing: .03em;
  font-family: var(--sans);
  transition: border-color .2s, box-shadow .2s;
}

.dd-trigger:hover {
  border-color: #ccd6e8;
  color: var(--ink);
}

.dd-trigger.on {
  border-color: #b9cdf5;
  box-shadow: 0 0 0 3px rgba(46, 107, 255, .1);
  color: var(--ink);
}

.dd-trigger .cv {
  color: var(--ink);
  font-weight: 500;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dd-trigger .arw {
  width: 7px;
  height: 7px;
  border-right: 1.5px solid #9aa3b8;
  border-bottom: 1.5px solid #9aa3b8;
  transform: rotate(45deg) translateY(-2px);
  transition: transform .2s;
}

.dd-trigger.on .arw {
  transform: rotate(225deg) translateY(-1px);
}

.dd-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  z-index: 45;
  width: 246px;
  max-height: 296px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  box-shadow: 0 20px 42px -20px rgba(80, 110, 200, .5);
  padding: 7px;
  display: none;
}

.dd.open .dd-panel {
  display: block;
}

.dd-panel .opt {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  border-radius: 8px;
  padding: 9px 10px;
  font-size: 12.5px;
  color: var(--ink-soft);
  font-family: var(--sans);
  text-align: left;
  cursor: pointer;
  letter-spacing: .02em;
  transition: background .15s;
}

.dd-panel .opt:hover {
  background: #f4f7fd;
}

.dd-panel .opt.on {
  background: var(--blue-soft);
  color: var(--accent-deep);
  font-weight: 500;
}

.dd-panel .opt .nm {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dd-panel .opt .ct {
  font-size: 11px;
  color: var(--muted);
  font-variant-numeric: tabular-nums;
}

.dd-panel .sep {
  height: 1px;
  background: var(--line);
  margin: 6px 4px;
}

.tb-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.viewsw {
  display: flex;
  border: 1px solid var(--line);
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.viewsw button {
  width: 38px;
  height: 36px;
  border: 0;
  background: transparent;
  cursor: pointer;
  display: grid;
  place-items: center;
  color: var(--muted);
  transition: all .18s ease;
}

.viewsw button.on {
  background: var(--ink);
  color: #fff;
}

/* ===== 名录区 ===== */
.roster {
  margin-top: 26px;
}

.rhead {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 14px;
}

.rhead h2 {
  font-family: var(--serif);
  font-size: 21px;
  font-weight: 500;
  letter-spacing: .01em;
}

.rhead .count {
  font-size: 12px;
  color: var(--muted);
  letter-spacing: .04em;
}

/* ===== 案例卡 ===== */
.cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.card {
  background: rgba(255, 255, 255, .92);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 20px 20px 17px;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 0 20px 44px -34px rgba(80, 110, 200, .34), inset 0 1px 0 rgba(255, 255, 255, .95);
  transition: transform .25s ease, box-shadow .25s ease, border-color .25s ease;
}

.card:hover {
  transform: translateY(-3px);
  border-color: #dbe4f4;
  box-shadow: 0 28px 56px -32px rgba(80, 110, 200, .44);
}

.ctop {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.ctop .ctxt {
  flex: 1;
  min-width: 0;
}

.ava {
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-family: var(--serif);
  font-size: 19px;
  color: #fff;
  letter-spacing: .02em;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  overflow: hidden;
}

.ava.a {
  background: linear-gradient(140deg, #4bb882, #17744a);
  box-shadow: 0 8px 16px -10px rgba(23, 116, 74, .75);
}

.ava.b {
  background: linear-gradient(140deg, #5c8bff, #1f56d6);
  box-shadow: 0 8px 16px -10px rgba(31, 86, 214, .75);
}

.ava.c {
  background: linear-gradient(140deg, #e0b25e, #a3690b);
  box-shadow: 0 8px 16px -10px rgba(163, 105, 11, .75);
}

.ava.d {
  background: linear-gradient(140deg, #9aa3b8, #66708a);
  box-shadow: 0 8px 16px -10px rgba(102, 112, 138, .75);
}

.nm {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.nm b {
  font-family: var(--serif);
  font-size: 18px;
  font-weight: 400;
  letter-spacing: .02em;
  color: var(--ink);
}

.role {
  font-size: 12.5px;
  color: var(--muted);
  margin-top: 5px;
  line-height: 1.5;
}

.meta {
  font-size: 11.5px;
  color: var(--muted);
  margin-top: 7px;
  line-height: 1.6;
}

.meta b {
  color: var(--ink-soft);
  font-weight: 500;
}

.bio {
  font-size: 12.5px;
  line-height: 1.8;
  color: var(--ink-soft);
  margin-top: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
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
  gap: 0;
  margin-top: 8px;
  padding-bottom: 10px;
}

.skl span {
  font-size: 11px;
  color: var(--accent);
}

.skl span + span::before {
  content: "·";
  color: #c3cde0;
  margin: 0 6px;
}

.kpi {
  display: flex;
  gap: 16px;
  margin-top: 2px;
  padding-top: 13px;
  border-top: 1px solid var(--line);
}

.kpi .kv {
  flex: 1;
  min-width: 0;
}

.kpi .v {
  font-family: var(--serif);
  font-size: 19px;
  font-weight: 500;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.kpi .v.r {
  color: var(--m);
}

.kpi .k {
  font-size: 10.5px;
  color: var(--muted);
  margin-top: 4px;
  letter-spacing: .02em;
  line-height: 1.5;
}

.cfoot {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: auto;
  padding-top: 12px;
}

.cfoot .cnt {
  font-size: 12px;
  color: var(--muted);
  white-space: nowrap;
}

.cfoot .cnt b {
  font-family: var(--serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
  margin-left: 4px;
  font-variant-numeric: tabular-nums;
}

.cfoot .more {
  margin-left: auto;
  font-size: 12.5px;
  color: var(--accent);
  display: inline-flex;
  gap: 5px;
}

.cfoot .more i {
  font-style: normal;
  transition: transform .2s ease;
}

.card:hover .cfoot .more i {
  transform: translateX(3px);
}

/* ===== 表格视图 ===== */
table.rt {
  width: 100%;
  border-collapse: collapse;
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
  padding: 13px 12px 13px 0;
  border-bottom: 1px solid var(--line);
  vertical-align: top;
}

table.rt tr:hover td {
  background: rgba(255, 255, 255, .72);
}

table.rt .tname {
  display: flex;
  align-items: baseline;
  gap: 9px;
}

table.rt .tname b {
  font-family: var(--serif);
  font-weight: 500;
  font-size: 14px;
}

table.rt .cid-sm {
  font-size: 11px;
  color: var(--accent);
  letter-spacing: .08em;
  font-variant-numeric: tabular-nums;
}

table.rt .dir {
  color: var(--muted);
}

table.rt .go {
  color: var(--accent);
}

/* ===== 空态 ===== */
.empty {
  padding: 64px 0;
  text-align: center;
  color: var(--muted);
  font-size: 13.5px;
  line-height: 1.9;
}

.empty b {
  display: block;
  font-family: var(--serif);
  font-size: 17px;
  color: var(--ink);
  margin-bottom: 6px;
  font-weight: 500;
}

.empty small {
  font-size: 12px;
}

/* ===== 三个诚实原则 ===== */
.principles {
  margin-top: 42px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  background: rgba(255, 255, 255, .74);
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: 0 18px 40px -32px rgba(80, 110, 200, .32);
  overflow: hidden;
}

.pr {
  padding: 20px 26px 22px;
  position: relative;
}

.pr + .pr {
  border-left: 1px solid var(--line);
}

.pr .no {
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: .1em;
  color: var(--accent);
}

.pr h3 {
  font-family: var(--serif);
  font-size: 16px;
  font-weight: 500;
  color: var(--ink);
  margin-top: 8px;
}

.pr p {
  margin-top: 10px;
  font-size: 12.5px;
  line-height: 1.75;
  color: var(--muted);
  text-wrap: pretty;
}

.pr p b {
  color: var(--ink-soft);
  font-weight: 500;
}

/* ===== 加载更多 ===== */
.more-row {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

.load-more {
  border: 0;
  background: none;
  padding: 8px 12px;
  font-family: var(--sans);
  font-size: 12.5px;
  letter-spacing: .04em;
  color: var(--accent);
  cursor: pointer;
}

.load-more:disabled {
  color: var(--muted);
  cursor: default;
}

/* ===== 加载 / 错误 ===== */
.loading-line {
  margin-top: 14px;
  text-align: center;
  color: var(--muted);
  font-size: 12px;
  letter-spacing: .04em;
}

.loading-line.soft {
  opacity: .72;
}

.notice {
  margin-top: 16px;
  text-align: center;
  color: var(--m);
  font-size: 12.5px;
  line-height: 1.8;
}

.notice button {
  border: 0;
  background: none;
  padding: 0 2px;
  color: var(--accent);
  font-family: var(--sans);
  font-size: 12.5px;
  cursor: pointer;
}

:deep(.site-foot) {
  margin-top: 70px;
}

@media (max-width: 1080px) {
  .cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1000px) {
  .principles {
    grid-template-columns: 1fr;
  }

  .pr + .pr {
    border-left: 0;
    border-top: 1px solid var(--line);
  }
}

@media (max-width: 760px) {
  h1 {
    font-size: 32px;
  }

  .tb-right {
    margin-left: 0;
  }
}
</style>
