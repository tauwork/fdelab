<template>
  <div class="roster-page">
    <SiteNav />

    <main>
      <section class="wrap phead">
        <h1>FDE 团队与认证体系</h1>
        <p class="lead"><span>组织里不缺会写代码的人，缺的是<b>能把业务判断写成评测标准的人</b>。</span><span>认证口径：TIIDA（国际数据与人工智能研究院）CFDE 三级体系。</span></p>
        <div class="pstats">
          <div><b>{{ stats.talentCount }}</b><span>FDE 在册</span></div>
          <div><b>{{ stats.levelCount }}</b><span>认证等级</span></div>
          <div><b>{{ stats.industryCount }}</b><span>覆盖行业</span></div>
          <div><b>{{ stats.standardCount }}</b><span>识别标准</span></div>
        </div>
      </section>

      <section class="wrap">
        <div class="levelbar">
          <div v-for="lv in levels" :key="lv.code" class="lv" :class="lv.code.toLowerCase()">
            <div class="lv-top">
              <span class="lv-tag">CFDE-{{ lv.code }}</span>
              <span class="lv-name">{{ lv.label }}</span>
              <span class="lv-num">{{ lv.count }} 人</span>
            </div>
            <p v-html="lv.definition"></p>
          </div>
        </div>
      </section>

      <section class="wrap toolbar">
        <div class="search">
          <span class="mag"></span>
          <input
            v-model="searchInput"
            type="search"
            placeholder="搜姓名、行业、技能"
            autocomplete="off"
            @input="onSearchInput"
          />
        </div>
        <div class="chips">
          <button
            v-for="opt in levelOptions"
            :key="opt.value"
            class="chip"
            :class="{ on: level === opt.value }"
            type="button"
            @click="selectLevel(opt.value)"
          >{{ opt.label }}</button>
        </div>
        <div class="tb-right">
          <select v-model="sort" class="sort" @change="reload">
            <option value="cases">按案例数</option>
            <option value="years">按年资</option>
            <option value="level">按等级</option>
          </select>
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
          <h2>FDE 团队</h2>
          <span class="count">共 {{ pageData.total }} 位</span>
        </div>

        <div class="cards" :hidden="view !== 'cards'">
          <article v-for="item in records" :key="item.slug" class="card">
            <div class="ctop">
              <span class="ava" :class="levelClass(item)">{{ initial(item.name) }}</span>
              <div>
                <div class="nm"><b>{{ item.name }}</b><span class="badge" :class="levelClass(item)">CFDE-{{ item.level }}</span></div>
                <div class="role">{{ item.role }}</div>
              </div>
            </div>
            <p class="bio">{{ item.bio }}</p>
            <div class="tags">
              <span v-for="tag in item.industries" :key="tag" class="ind">{{ tag }}</span>
            </div>
            <div class="skl">
              <span v-for="skill in item.skills.slice(0, 4)" :key="skill">{{ skill }}</span>
            </div>
            <div class="cfoot">
              <span class="cnt">案例 <b>{{ item.caseCount }}</b></span>
              <span class="yr">年资 <b>{{ item.years }}</b></span>
              <RouterLink class="more" :to="`/t/${item.slug}`">查看 <i>→</i></RouterLink>
            </div>
          </article>

          <div v-if="showNote" class="notecard">
            <p>以上人物页为<b>原型示例数据</b>，用于演示页面结构——正式版由 FDE 智研社人才库填充，入库须经认证与项目复核。</p>
          </div>
        </div>

        <div :hidden="view !== 'table'">
          <table class="rt">
            <thead>
              <tr>
                <th style="width:170px">姓名</th>
                <th style="width:112px">等级</th>
                <th>主攻方向</th>
                <th style="width:74px">年资</th>
                <th style="width:104px">案例</th>
                <th style="width:62px"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in records" :key="item.slug">
                <td><div class="tname"><span class="mini" :class="levelClass(item)">{{ initial(item.name) }}</span><b>{{ item.name }}</b></div></td>
                <td><span class="badge" :class="levelClass(item)">CFDE-{{ item.level }}</span></td>
                <td class="dir">{{ item.role }}</td>
                <td class="n">{{ item.years }} 年</td>
                <td class="n">{{ item.caseCount }}</td>
                <td><RouterLink class="go" :to="`/t/${item.slug}`">查看 →</RouterLink></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="!records.length && !loading && !error" class="empty">
          <b>没有符合条件的人</b>
          <span>换个关键词，或切回「全部等级」</span>
        </div>

        <div v-if="hasMore" class="more-row">
          <button class="load-more" type="button" :disabled="loadingMore" @click="loadMore">
            {{ loadingMore ? '加载中…' : '加载更多' }}
          </button>
        </div>

        <p v-if="error" class="notice">{{ error }} <button type="button" @click="reload">重试</button></p>
        <p v-if="loading && !records.length" class="loading-line">正在加载…</p>
        <p v-else-if="loading" class="loading-line soft">更新中…</p>
      </section>
    </main>

    <SiteFooter />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import SiteNav from '../components/SiteNav.vue'
import SiteFooter from '../components/SiteFooter.vue'
import { fetchRoster } from '../api/talent'

const SIZE = 12

const levelOptions = [
  { value: 'all', label: '全部等级' },
  { value: 'M', label: '大师 M' },
  { value: 'P', label: '专业 P' },
  { value: 'A', label: '助理 A' },
]

const searchInput = ref('')
const q = ref('')
const level = ref('all')
const sort = ref('cases')
const view = ref('cards')

const records = ref([])
const stats = ref({ talentCount: 0, levelCount: 0, industryCount: 0, standardCount: 0 })
const levels = ref([])
const pageData = ref({ total: 0, page: 1, size: SIZE, records: [] })

const loading = ref(false)
const loadingMore = ref(false)
const error = ref('')
const currentPage = ref(1)

let searchTimer = 0
let requestSeq = 0

function initial(name) {
  return Array.from(String(name || '').trim())[0] || '?'
}

function levelClass(item) {
  return String(item?.level || '').toLowerCase()
}

const hasMore = computed(() => Number(pageData.value.total) > records.value.length)

const showNote = computed(() => {
  const total = Number(pageData.value.total)
  return view.value === 'cards'
    && !q.value.trim()
    && level.value === 'all'
    && total === records.value.length
    && total % 3 !== 0
})

async function loadPage(targetPage, append) {
  const seq = ++requestSeq
  if (append) loadingMore.value = true
  else loading.value = true
  error.value = ''
  try {
    const data = await fetchRoster({
      page: targetPage,
      size: SIZE,
      q: q.value.trim() || undefined,
      level: level.value,
      sort: sort.value,
    })
    if (seq !== requestSeq) return
    stats.value = data?.stats || stats.value
    levels.value = data?.levels || levels.value
    pageData.value = data?.page || { total: 0, page: targetPage, size: SIZE, records: [] }
    const next = Array.isArray(data?.page?.records) ? data.page.records : []
    records.value = append ? [...records.value, ...next] : next
    currentPage.value = targetPage
  } catch (err) {
    if (seq !== requestSeq) return
    error.value = err?.message || '加载失败'
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

function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    q.value = searchInput.value
    reload()
  }, 300)
}

function selectLevel(value) {
  if (level.value === value) return
  level.value = value
  reload()
}

function loadMore() {
  if (loading.value || loadingMore.value || !hasMore.value) return
  loadPage(currentPage.value + 1, true)
}

onMounted(() => {
  document.title = 'FDE 团队与认证体系 · FDE 智研社'
  reload()
})

onBeforeUnmount(() => {
  clearTimeout(searchTimer)
})
</script>

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

/* hidden 属性需要压过 .cards 的 display:grid，否则视图切换失效 */
[hidden] {
  display: none !important;
}

.roster-page {
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

.pstats {
  display: flex;
  flex-wrap: wrap;
  margin-top: 34px;
  border-top: 1px solid var(--line);
}

.pstats div {
  padding: 20px 38px 20px 0;
  margin-right: 38px;
  border-right: 1px solid var(--line);
}

.pstats div:last-child {
  border-right: 0;
  margin-right: 0;
  padding-right: 0;
}

.pstats b {
  font-family: var(--serif);
  font-size: 30px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.pstats span {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: var(--muted);
  letter-spacing: .05em;
}

/* ===== 认证等级标尺 ===== */
.levelbar {
  margin-top: 42px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  background: rgba(255, 255, 255, .74);
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: 0 18px 40px -32px rgba(80, 110, 200, .32);
  overflow: hidden;
}

.lv {
  padding: 20px 26px 22px;
  position: relative;
}

.lv + .lv {
  border-left: 1px solid var(--line);
}

.lv-top {
  display: flex;
  align-items: center;
  gap: 9px;
}

.lv-tag {
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: .1em;
  padding: 3px 7px;
  border-radius: 5px;
}

.lv.a .lv-tag {
  color: var(--a);
  background: var(--a-bg);
}

.lv.p .lv-tag {
  color: var(--p);
  background: var(--p-bg);
}

.lv.m .lv-tag {
  color: var(--m);
  background: var(--m-bg);
}

.lv-name {
  font-family: var(--serif);
  font-size: 16px;
  color: var(--ink);
}

.lv-num {
  margin-left: auto;
  font-size: 12px;
  color: var(--muted);
  font-variant-numeric: tabular-nums;
}

.lv p {
  margin-top: 10px;
  font-size: 12.5px;
  line-height: 1.75;
  color: var(--muted);
  text-wrap: pretty;
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

.tb-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

select.sort {
  height: 38px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  font-size: 12.5px;
  color: var(--ink);
  padding: 0 10px;
  font-family: var(--sans);
  outline: none;
  cursor: pointer;
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

/* ===== 名录 ===== */
.roster {
  margin-top: 26px;
}

.rhead {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 22px;
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

.cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.card {
  background: rgba(255, 255, 255, .92);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 20px 20px 17px;
  display: flex;
  flex-direction: column;
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
  white-space: nowrap;
  overflow: hidden;
}

.ava.m {
  background: linear-gradient(140deg, #d8606f, var(--m));
  box-shadow: 0 8px 16px -10px rgba(180, 42, 60, .75);
}

.ava.p {
  background: linear-gradient(140deg, #5c8bff, var(--p));
  box-shadow: 0 8px 16px -10px rgba(31, 86, 214, .75);
}

.ava.a {
  background: linear-gradient(140deg, #4bb882, var(--a));
  box-shadow: 0 8px 16px -10px rgba(23, 116, 74, .75);
}

.nm {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nm b {
  font-family: var(--serif);
  font-size: 17px;
  font-weight: 500;
  letter-spacing: .02em;
}

.badge {
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: .07em;
  padding: 2.5px 7px;
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

.role {
  font-size: 12px;
  color: var(--muted);
  margin-top: 5px;
  line-height: 1.5;
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

.cfoot {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: auto;
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.cfoot .cnt {
  font-size: 12px;
  color: var(--muted);
}

.cfoot .cnt b {
  color: var(--ink);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.cfoot .yr {
  font-size: 12px;
  color: var(--muted);
}

.cfoot .yr b {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.cfoot .more {
  margin-left: auto;
  font-size: 12.5px;
  color: var(--accent);
  display: inline-flex;
  gap: 4px;
}

.cfoot .more i {
  font-style: normal;
  transition: transform .2s ease;
}

.card:hover .cfoot .more i {
  transform: translateX(3px);
}

/* 名录人数未满时的补充说明位：只占位，不做点击 */
.notecard {
  grid-column: span 2;
  background: rgba(255, 255, 255, .5);
  border: 1px dashed #d3dcec;
  border-radius: 16px;
  box-shadow: none;
  padding: 26px 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.notecard:hover {
  transform: none;
  border-color: #d3dcec;
  box-shadow: none;
}

.notecard p {
  font-size: 12.5px;
  line-height: 1.95;
  color: var(--muted);
  max-width: 520px;
}

.notecard b {
  color: var(--ink-soft);
  font-weight: 500;
}

/* ===== 列表视图 ===== */
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
}

table.rt tr:hover td {
  background: rgba(255, 255, 255, .72);
}

table.rt .tname {
  display: flex;
  align-items: center;
  gap: 9px;
}

table.rt .mini {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  font-family: var(--serif);
  font-size: 14px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
}

table.rt .mini.m {
  background: linear-gradient(140deg, #d8606f, var(--m));
}

table.rt .mini.p {
  background: linear-gradient(140deg, #5c8bff, var(--p));
}

table.rt .mini.a {
  background: linear-gradient(140deg, #4bb882, var(--a));
}

table.rt .dir {
  color: var(--muted);
}

table.rt .n {
  font-variant-numeric: tabular-nums;
}

table.rt .go {
  color: var(--accent);
}

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

/* ===== 本轮唯一新增的视觉元素：加载更多 ===== */
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
  color: #b42a3c;
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

/* 页脚：白底通栏到屏幕边，内容仍在版心内（SiteFooter 自带通栏背景） */
:deep(.site-foot) {
  margin-top: 70px;
}

@media (max-width: 1080px) {
  .cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .notecard {
    grid-column: span 2;
  }
}

@media (max-width: 760px) {
  h1 {
    font-size: 32px;
  }

  .cards {
    grid-template-columns: 1fr;
  }

  .notecard {
    grid-column: span 1;
  }

  .levelbar {
    grid-template-columns: 1fr;
  }

  .lv + .lv {
    border-left: 0;
    border-top: 1px solid var(--line);
  }

  .tb-right {
    margin-left: 0;
  }
}
</style>
