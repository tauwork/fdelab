<template>
  <canvas ref="fieldCanvas" class="field" aria-hidden="true"></canvas>

  <div class="wrap">
    <div class="topbar">
      <router-link class="brandmark" to="/">FDELab</router-link>
      <span class="breadcrumb">人才 · 名录</span>
    </div>

    <header class="head">
      <p class="kicker">FDELAB · TALENT</p>
      <h1>人才</h1>
      <p class="lead">每位 FDE 有自己的子域名主页，卡上的数字都能追溯到案例库。没有案例的人，我们诚实留白。</p>
      <div class="stats">
        <span><b>{{ stats.people }}</b>位 FDE</span><span class="sep"></span>
        <span><b>{{ stats.industries }}</b>个行业</span><span class="sep"></span>
        <span><b>{{ stats.cases }}</b>个交付案例</span>
      </div>
    </header>

    <div ref="toolbarRef" class="toolbar">
      <div class="tabs" role="tablist">
        <button
          v-for="opt in industryTabs"
          :key="opt.value ?? 'all'"
          class="tab"
          :class="{ on: filterIndustry === opt.value }"
          type="button"
          role="tab"
          :aria-selected="filterIndustry === opt.value"
          @click="selectIndustry(opt.value)"
        >{{ opt.label }}</button>
      </div>

      <div class="right">
        <div class="pick" :class="{ open: openPicker === 'domain', picked: !!filterDomain }">
          <button
            class="pick-btn"
            type="button"
            aria-haspopup="listbox"
            :aria-expanded="openPicker === 'domain'"
            @click.stop="togglePicker('domain')"
          >
            <span class="pick-label">{{ domainLabel }}</span><i class="chev" aria-hidden="true"></i>
          </button>
          <div class="pick-panel" role="listbox" aria-label="能力域">
            <button
              v-for="opt in domainTabs"
              :key="opt.value ?? 'all'"
              class="pick-item"
              :class="{ on: filterDomain === opt.value }"
              type="button"
              role="option"
              :aria-selected="filterDomain === opt.value"
              @click="selectDomain(opt.value)"
            ><i class="tick"></i><span>{{ opt.label }}</span></button>
          </div>
        </div>

        <div class="pick" :class="{ open: openPicker === 'sort' }">
          <button
            class="pick-btn"
            type="button"
            aria-haspopup="listbox"
            :aria-expanded="openPicker === 'sort'"
            @click.stop="togglePicker('sort')"
          >
            <span class="pick-label">{{ sortLabel }}</span><i class="chev" aria-hidden="true"></i>
          </button>
          <div class="pick-panel" role="listbox" aria-label="排序">
            <button
              v-for="opt in sortOptions"
              :key="opt.value"
              class="pick-item"
              :class="{ on: sortKey === opt.value }"
              type="button"
              role="option"
              :aria-selected="sortKey === opt.value"
              @click="selectSort(opt.value)"
            ><i class="tick"></i><span>{{ opt.label }}</span></button>
          </div>
        </div>

        <input v-model="searchInput" class="search" type="search" placeholder="搜名字或能力…" @input="onSearchInput" />

        <span class="viewtoggle">
          <span class="vsep"></span>
          <button class="vbtn" :class="{ on: viewMode === 'card' }" type="button" @click="setView('card')">卡片</button>
          <button class="vbtn" :class="{ on: viewMode === 'list' }" type="button" @click="setView('list')">列表</button>
        </span>

        <span class="count">{{ total }} / {{ people.length }} 位</span>
      </div>
    </div>

    <p v-if="notice" class="notice">{{ notice }}</p>

    <div
      v-show="viewMode === 'card'"
      ref="gridRef"
      class="grid"
      :class="{ fill }"
      :style="{ height: gridHeight }"
    >
      <article
        v-for="person in pageSlice"
        :key="person.id"
        class="card"
        @click="openHomepage(person)"
      >
        <div class="shine" aria-hidden="true"></div>
        <div class="card-top">
          <span class="mark">FDE · IDENTITY</span>
          <span class="city">{{ person.city }}</span>
        </div>
        <h2 class="name">{{ person.name }}</h2>
        <div class="role">{{ person.role }}</div>
        <div class="meta">
          <b>{{ person.cases.length }}</b> 个案例<span class="dotsep">·</span><b>{{ person.industries.length }}</b> 个行业<span class="dotsep">·</span><b>{{ person.likes }}</b> 赞
        </div>
        <div class="tags">
          <template v-for="(tag, index) in tagParts(person)" :key="index">
            <span v-if="index" class="dotsep">·</span>
            <span :class="{ dm: tag.domain }">{{ tag.text }}</span>
          </template>
        </div>
        <div v-if="person.cases[0]" class="case">
          <div class="clabel">代表案例</div>
          <div class="ctitle">{{ person.cases[0].title }}</div>
          <div v-if="caseBudget(person.cases[0])" class="cbudget">{{ caseBudget(person.cases[0]) }}</div>
        </div>
        <div v-else class="case empty">暂无案例，档案整理中——不编数据</div>
        <div class="foot">
          <button class="btn" type="button" @click.stop="openDrawer(person)">预览档案</button>
          <a class="btn solid" :href="homepageOf(person)" target="_blank" rel="noopener" @click.stop>进入主页 →</a>
          <button class="btn like" :class="{ liked: person.liked }" type="button" @click.stop="like(person)">
            {{ person.liked ? '已赞' : '赞' }}
          </button>
        </div>
      </article>
    </div>

    <div v-show="viewMode === 'list'" class="list">
      <div v-for="person in pageSlice" :key="person.id" class="row" @click="openRow(person, $event)">
        <span class="rname">{{ person.name }}</span>
        <span class="rrole">{{ person.role }}</span>
        <span class="rtags">
          <template v-for="(tag, index) in tagParts(person)" :key="index">
            <span v-if="index" class="dotsep">·</span>
            <span :class="{ dm: tag.domain }">{{ tag.text }}</span>
          </template>
        </span>
        <span class="rcase"><b>{{ person.cases.length }}</b> 例</span>
        <span v-if="person.cases[0]" class="rrep">{{ person.cases[0].title }}</span>
        <span v-else class="rrep empty">暂无案例</span>
        <span class="rgo">→</span>
      </div>
    </div>

    <div v-if="total > 0" class="pager">
      <span class="pmeta">第 {{ from + 1 }}–{{ from + shown }} 位 · 共 {{ total }} 位</span>
      <div v-if="totalPages > 1" class="pnums">
        <button class="pnum" type="button" :disabled="page === 1" @click="gotoPage(page - 1)">‹</button>
        <button
          v-for="n in totalPages"
          :key="n"
          class="pnum"
          :class="{ on: n === page }"
          type="button"
          @click="gotoPage(n)"
        >{{ n }}</button>
        <button class="pnum" type="button" :disabled="page === totalPages" @click="gotoPage(page + 1)">›</button>
      </div>
    </div>

    <div v-if="loading" class="empty-state">正在加载人才…</div>
    <div v-else-if="loadError" class="empty-state">{{ loadError }}</div>
    <div v-else-if="total === 0" class="empty-state">没有符合条件的人，换个行业看看。</div>
  </div>

  <div class="minipager" :class="{ show: showMiniPager }">
    <button type="button" aria-label="上一页" :disabled="page === 1" @click="gotoPage(page - 1)">‹</button>
    <span class="mnum"><b>{{ page }}</b> / {{ totalPages }}</span>
    <button type="button" aria-label="下一页" :disabled="page === totalPages" @click="gotoPage(page + 1)">›</button>
  </div>

  <div class="drawer-mask" :class="{ open: drawerOpen }" @click.self="closeDrawer">
    <div v-if="drawerPerson" class="drawer">
      <button class="close" type="button" @click="closeDrawer">✕</button>
      <p class="drawer-kicker">FDE · PROFILE</p>
      <h2>{{ drawerPerson.name }}</h2>
      <p class="drole">{{ drawerPerson.role }}　·　{{ drawerPerson.city }}</p>
      <div class="tags drawer-tags">
        <span v-for="tag in drawerPerson.industries" :key="'i' + tag" class="tag">{{ tag }}</span>
        <span v-for="tag in drawerPerson.domains" :key="'d' + tag" class="tag domain">{{ tag }}</span>
      </div>

      <section class="dsec">
        <h3>交付数据</h3>
        <p><b>{{ drawerPerson.cases.length }} 个案例</b> · 覆盖 {{ drawerPerson.industries.length }} 个行业 · {{ drawerPerson.likes }} 个赞</p>
      </section>

      <section class="dsec">
        <h3>主要身份</h3>
        <template v-if="drawerPerson.roles.length">
          <p v-for="(item, index) in drawerPerson.roles" :key="index"><b>{{ item.org }}</b> · {{ item.title }}<br />{{ item.description }}</p>
        </template>
        <p v-else>待补充</p>
      </section>

      <section v-if="drawerPerson.experiences.length" class="dsec">
        <h3>过往经历</h3>
        <p v-for="(item, index) in drawerPerson.experiences" :key="index"><b>{{ item.title }}</b><br />{{ item.description }}</p>
      </section>

      <section v-if="drawerPerson.services.length" class="dsec">
        <h3>提供的服务</h3>
        <p v-for="(item, index) in drawerPerson.services" :key="index"><b>0{{ index + 1 }}　{{ item.title }}</b><br />{{ item.description }}</p>
      </section>

      <section v-if="drawerPerson.collabs.length" class="dsec">
        <h3>正在寻找的合作</h3>
        <p v-for="(item, index) in drawerPerson.collabs" :key="index"><b>{{ item.title }}</b><br />{{ item.description }}</p>
      </section>

      <section class="dsec">
        <h3>实战案例</h3>
        <template v-if="drawerPerson.cases.length">
          <div v-for="(item, index) in drawerPerson.cases" :key="index" class="dcase">
            <div class="ind">{{ item.industry }}</div>
            <h4>{{ item.title }}</h4>
            <p class="dportrait">{{ item.portrait }}</p>
            <ul>
              <li v-for="(point, pointIndex) in item.points" :key="pointIndex">{{ point }}</li>
            </ul>
            <div v-if="caseMeta(item)" class="meta">{{ caseMeta(item) }}</div>
            <div v-if="item.pitfall" class="meta pitfall">坑 · {{ item.pitfall }}</div>
          </div>
        </template>
        <p v-else class="muted-line">暂无案例。这位的档案还在整理，宁缺不编。</p>
      </section>

      <div class="cta">
        <a class="btn solid" :href="homepageOf(drawerPerson)" target="_blank" rel="noopener">进入完整主页 →</a>
        <button class="btn" type="button" @click="closeDrawer">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { fetchTalentBySlug, fetchTalents, toggleTalentLike } from '../api/talent'
import { visitorKey } from '../utils/host'

const SORT_OPTIONS = [
  { value: 'likes', label: '按点赞' },
  { value: 'cases', label: '按案例数' },
]

const people = ref([])
const loading = ref(true)
const loadError = ref('')
const notice = ref('')

const filterIndustry = ref(null)
const filterDomain = ref(null)
const sortKey = ref('likes')
const keyword = ref('')
const searchInput = ref('')

const viewMode = ref('card')
const page = ref(1)
const pageSize = ref(24)
const fill = ref(false)
const gridHeight = ref('')

const openPicker = ref(null)
const drawerPerson = ref(null)
const drawerOpen = ref(false)

const gridRef = ref(null)
const toolbarRef = ref(null)
const fieldCanvas = ref(null)

let protoH = 0
let fitScheduled = false
let fitToken = 0
let drawerToken = 0
let searchTimer = 0
let resizeTimer = 0
let rafTilt = 0
let rafField = 0
let cleanupCanvas = null

const reduce = typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches

function firstArray(...values) {
  for (const value of values) {
    if (Array.isArray(value) && value.length) return value
  }
  return []
}

function shortCity(location) {
  if (!location) return ''
  const text = String(location).split('·').pop().replace(/[（(].*?[)）]/g, '').trim()
  return text || String(location).trim()
}

function adaptCase(item) {
  return {
    industry: item?.industry || '',
    title: item?.title || '',
    portrait: item?.portrait || '',
    budget: item?.budget || '',
    duration: item?.duration || '',
    points: Array.isArray(item?.points) ? item.points : [],
    delivery: item?.delivery || '',
    pitfall: item?.pitfall || '',
  }
}

function adaptTalent(record) {
  const profile = record?.profile || {}
  const industries = firstArray(profile.industries, profile.industryTags)
  const domains = firstArray(profile.domains, profile.domainTags)
  return {
    id: record?.id,
    name: record?.name || '',
    slug: record?.slug || '',
    city: shortCity(record?.location),
    location: record?.location || '',
    role: record?.headline || '',
    likes: Number(record?.likeCount) || 0,
    liked: Boolean(record?.liked),
    industries,
    domains,
    roles: Array.isArray(profile.roles) ? profile.roles : [],
    experiences: Array.isArray(profile.experiences) ? profile.experiences : [],
    services: Array.isArray(profile.services) ? profile.services : [],
    collabs: Array.isArray(profile.collabs)
      ? profile.collabs
      : (Array.isArray(profile.collaborations) ? profile.collaborations : []),
    cases: Array.isArray(profile.cases) ? profile.cases.map(adaptCase) : [],
  }
}

function mergePerson(base, detail) {
  return {
    ...base,
    ...detail,
    industries: detail.industries.length ? detail.industries : base.industries,
    domains: detail.domains.length ? detail.domains : base.domains,
    roles: detail.roles.length ? detail.roles : base.roles,
    experiences: detail.experiences.length ? detail.experiences : base.experiences,
    services: detail.services.length ? detail.services : base.services,
    collabs: detail.collabs.length ? detail.collabs : base.collabs,
    cases: detail.cases.length ? detail.cases : base.cases,
  }
}

const industryOptions = computed(() => {
  const set = new Set()
  people.value.forEach((person) => person.industries.forEach((item) => set.add(item)))
  return [...set]
})

const domainOptions = computed(() => {
  const set = new Set()
  people.value.forEach((person) => person.domains.forEach((item) => set.add(item)))
  return [...set]
})

const industryTabs = computed(() => [
  { value: null, label: '全部' },
  ...industryOptions.value.map((value) => ({ value, label: value })),
])

const domainTabs = computed(() => [
  { value: null, label: '全部' },
  ...domainOptions.value.map((value) => ({ value, label: value })),
])

const sortOptions = SORT_OPTIONS

const domainLabel = computed(() => (filterDomain.value ? `能力域 · ${filterDomain.value}` : '能力域'))
const sortLabel = computed(() => SORT_OPTIONS.find((item) => item.value === sortKey.value)?.label || '按点赞')

const visiblePeople = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return people.value
    .filter((person) => !filterIndustry.value || person.industries.includes(filterIndustry.value))
    .filter((person) => !filterDomain.value || person.domains.includes(filterDomain.value))
    .filter((person) => !kw
      || person.name.toLowerCase().includes(kw)
      || person.role.toLowerCase().includes(kw)
      || person.industries.concat(person.domains).some((tag) => tag.toLowerCase().includes(kw)))
    .slice()
    .sort((a, b) => (sortKey.value === 'cases'
      ? b.cases.length - a.cases.length || b.likes - a.likes
      : b.likes - a.likes || b.cases.length - a.cases.length))
})

const total = computed(() => visiblePeople.value.length)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const from = computed(() => (page.value - 1) * pageSize.value)
const pageSlice = computed(() => visiblePeople.value.slice(from.value, from.value + pageSize.value))
const shown = computed(() => pageSlice.value.length)
const showMiniPager = computed(() => viewMode.value === 'card' && totalPages.value > 1)

const stats = computed(() => ({
  people: people.value.length,
  industries: industryOptions.value.length,
  cases: people.value.reduce((sum, person) => sum + person.cases.length, 0),
}))

watch(totalPages, (pages) => {
  if (page.value > pages) page.value = pages
})

function caseBudget(item) {
  return [item?.budget, item?.duration].filter(Boolean).join(' · ')
}

function caseMeta(item) {
  const parts = []
  if (item?.budget) parts.push(`投入 ${item.budget}`)
  if (item?.duration) parts.push(`周期 ${item.duration}`)
  if (item?.delivery) parts.push(`交付 ${item.delivery}`)
  return parts.join(' · ')
}

function tagParts(person) {
  return [
    ...person.industries.slice(0, 2).map((text) => ({ text, domain: false })),
    ...person.domains.slice(0, 1).map((text) => ({ text, domain: true })),
  ]
}

function homepageOf(person) {
  return person?.slug ? `/t/${person.slug}` : '/'
}

function openHomepage(person) {
  if (!person?.slug) return
  window.open(homepageOf(person), '_blank', 'noopener')
}

function openRow(person, event) {
  if (event.shiftKey) {
    openDrawer(person)
    return
  }
  openHomepage(person)
}

function togglePicker(name) {
  openPicker.value = openPicker.value === name ? null : name
}

function selectIndustry(value) {
  filterIndustry.value = value
  page.value = 1
  scheduleFit()
}

function selectDomain(value) {
  filterDomain.value = value || null
  openPicker.value = null
  page.value = 1
  scheduleFit()
}

function selectSort(value) {
  sortKey.value = value
  openPicker.value = null
  page.value = 1
  scheduleFit()
}

function setView(mode) {
  if (viewMode.value === mode) return
  viewMode.value = mode
  page.value = 1
  if (mode === 'list') {
    pageSize.value = 24
    fill.value = false
    gridHeight.value = ''
  } else {
    scheduleFit()
  }
}

function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    keyword.value = searchInput.value
    page.value = 1
    scheduleFit()
  }, 140)
}

function gotoPage(next) {
  const target = Math.min(Math.max(1, next), totalPages.value)
  if (target === page.value) return
  page.value = target
  scheduleFit()
  const toolbar = toolbarRef.value
  const top = target === 1 || !toolbar ? 0 : toolbar.getBoundingClientRect().top + window.scrollY - 6
  window.scrollTo({ top: Math.max(0, top), behavior: reduce ? 'auto' : 'smooth' })
}

function scheduleFit() {
  if (fitScheduled) return
  fitScheduled = true
  nextTick(async () => {
    fitScheduled = false
    await fitGridToViewport()
  })
}

async function fitGridToViewport() {
  const grid = gridRef.value
  if (viewMode.value !== 'card' || !grid) {
    fill.value = false
    gridHeight.value = ''
    return
  }

  const token = ++fitToken
  let cards = grid.querySelectorAll('.card')
  if (!cards.length) {
    fill.value = false
    gridHeight.value = ''
    return
  }

  const cols = getComputedStyle(grid).gridTemplateColumns.split(' ').filter(Boolean).length || 1
  const gap = parseFloat(getComputedStyle(grid).rowGap) || 30

  if (!protoH) {
    fill.value = false
    gridHeight.value = ''
    await nextTick()
    if (token !== fitToken) return
    cards = grid.querySelectorAll('.card')
    let max = 0
    cards.forEach((card) => {
      max = Math.max(max, card.offsetHeight)
    })
    protoH = max
  }

  const toolbar = toolbarRef.value
  const toolH = toolbar ? toolbar.offsetHeight : 64
  const avail = window.innerHeight - toolH - 56
  const rows = Math.max(2, Math.floor((avail + gap) / (protoH + gap)))
  const nextSize = cols * rows

  if (nextSize !== pageSize.value) {
    fill.value = false
    gridHeight.value = ''
    pageSize.value = nextSize
    const pages = Math.max(1, Math.ceil(total.value / nextSize))
    if (page.value > pages) page.value = pages
    await nextTick()
    if (token !== fitToken) return
  }

  if (total.value >= cols * 2) {
    gridHeight.value = `${Math.max(avail, rows * protoH + (rows - 1) * gap)}px`
    fill.value = true
  } else {
    gridHeight.value = ''
    fill.value = false
  }
}

async function openDrawer(person) {
  drawerPerson.value = person
  drawerOpen.value = false
  drawerToken += 1
  const token = drawerToken
  await nextTick()
  drawerOpen.value = true
  if (!person?.slug) return
  try {
    const detail = await fetchTalentBySlug(person.slug, visitorKey())
    if (token !== drawerToken) return
    drawerPerson.value = mergePerson(person, adaptTalent(detail))
  } catch {
    if (token !== drawerToken) return
  }
}

function closeDrawer() {
  drawerOpen.value = false
  drawerPerson.value = null
  drawerToken += 1
}

async function like(person) {
  try {
    const result = await toggleTalentLike(person.id, visitorKey())
    person.liked = result.liked
    person.likes = result.likeCount
    notice.value = ''
  } catch (error) {
    notice.value = error.message || '点赞失败'
  }
}

const tilts = new WeakMap()
const MAX_TY = 9
const MAX_TX = 7

function tiltState(card) {
  let state = tilts.get(card)
  if (!state) {
    state = { rx: 0, ry: 0, trx: 0, tRy: 0, s: 1, ts: 1 }
    tilts.set(card, state)
  }
  return state
}

function tiltLoop() {
  const grid = gridRef.value
  let active = false
  if (grid) {
    grid.querySelectorAll('.card').forEach((card) => {
      const state = tilts.get(card)
      if (!state) return
      if (Math.abs(state.trx - state.rx) < 0.01
        && Math.abs(state.tRy - state.ry) < 0.01
        && Math.abs(state.ts - state.s) < 0.0005) return
      state.rx += (state.trx - state.rx) * 0.09
      state.ry += (state.tRy - state.ry) * 0.09
      state.s += (state.ts - state.s) * 0.12
      card.style.transform = `rotateX(${state.rx.toFixed(2)}deg) rotateY(${state.ry.toFixed(2)}deg) scale(${state.s.toFixed(4)})`
      active = true
    })
  }
  rafTilt = active ? requestAnimationFrame(tiltLoop) : 0
}

function kickTilt() {
  if (!rafTilt) rafTilt = requestAnimationFrame(tiltLoop)
}

function onGridPointerMove(event) {
  if (reduce) return
  const card = event.target.closest('.card')
  if (!card || !gridRef.value?.contains(card)) return
  const rect = card.getBoundingClientRect()
  const px = (event.clientX - rect.left) / rect.width
  const py = (event.clientY - rect.top) / rect.height
  const state = tiltState(card)
  state.tRy = (px - 0.5) * MAX_TY * 2
  state.trx = (0.5 - py) * MAX_TX * 2
  state.ts = 1.012
  card.style.setProperty('--mx', `${(px * 100).toFixed(2)}%`)
  card.style.setProperty('--my', `${(py * 100).toFixed(2)}%`)
  kickTilt()
}

function onGridPointerOut(event) {
  if (reduce) return
  const card = event.target.closest('.card')
  if (!card || card.contains(event.relatedTarget)) return
  const state = tiltState(card)
  state.trx = 0
  state.tRy = 0
  state.ts = 1
  card.style.setProperty('--mx', '50%')
  card.style.setProperty('--my', '40%')
  kickTilt()
}

function setupCanvas() {
  const canvas = fieldCanvas.value
  if (!canvas) return null
  const ctx = canvas.getContext('2d')
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  let width = 0
  let height = 0
  let dots = []
  const mouse = { x: -999, y: -999 }

  function resizeCanvas() {
    width = canvas.width = window.innerWidth * dpr
    height = canvas.height = window.innerHeight * dpr
    canvas.style.width = `${window.innerWidth}px`
    canvas.style.height = `${window.innerHeight}px`
    const count = Math.round(Math.min(80, (window.innerWidth * window.innerHeight) / 26000))
    dots = Array.from({ length: count }, () => ({
      x: Math.random() * width,
      y: Math.random() * height,
      vx: (Math.random() - 0.5) * 0.18 * dpr,
      vy: (Math.random() - 0.5) * 0.18 * dpr,
      r: (Math.random() * 1.4 + 0.6) * dpr,
    }))
  }

  function frame() {
    ctx.clearRect(0, 0, width, height)
    const link = 120 * dpr
    for (let i = 0; i < dots.length; i++) {
      const dot = dots[i]
      dot.x += dot.vx
      dot.y += dot.vy
      if (dot.x < 0 || dot.x > width) dot.vx *= -1
      if (dot.y < 0 || dot.y > height) dot.vy *= -1
      const dx = dot.x - mouse.x
      const dy = dot.y - mouse.y
      const distance = Math.hypot(dx, dy)
      if (distance < 140 * dpr && distance > 0) {
        dot.x += (dx / distance) * 0.5
        dot.y += (dy / distance) * 0.5
      }
      ctx.beginPath()
      ctx.arc(dot.x, dot.y, dot.r, 0, Math.PI * 2)
      ctx.fillStyle = 'rgba(31,107,74,.22)'
      ctx.fill()
      for (let j = i + 1; j < dots.length; j++) {
        const other = dots[j]
        const gapDistance = Math.hypot(dot.x - other.x, dot.y - other.y)
        if (gapDistance < link) {
          ctx.beginPath()
          ctx.moveTo(dot.x, dot.y)
          ctx.lineTo(other.x, other.y)
          ctx.strokeStyle = `rgba(31,107,74,${(0.13 * (1 - gapDistance / link)).toFixed(3)})`
          ctx.lineWidth = 0.6 * dpr
          ctx.stroke()
        }
      }
    }
    rafField = requestAnimationFrame(frame)
  }

  const onMouseMove = (event) => {
    mouse.x = event.clientX * dpr
    mouse.y = event.clientY * dpr
  }

  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)
  window.addEventListener('pointermove', onMouseMove)
  if (!reduce) rafField = requestAnimationFrame(frame)

  return () => {
    cancelAnimationFrame(rafField)
    window.removeEventListener('resize', resizeCanvas)
    window.removeEventListener('pointermove', onMouseMove)
  }
}

function onGridResize() {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    protoH = 0
    scheduleFit()
  }, 160)
}

function onDocumentClick() {
  openPicker.value = null
}

function onKeydown(event) {
  if (event.key === 'Escape') {
    closeDrawer()
    openPicker.value = null
    return
  }
  if (viewMode.value !== 'card' || drawerPerson.value || openPicker.value) return
  const tag = event.target?.tagName
  if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return
  if (event.key === 'ArrowRight' || event.key === 'PageDown') {
    event.preventDefault()
    gotoPage(page.value + 1)
  } else if (event.key === 'ArrowLeft' || event.key === 'PageUp') {
    event.preventDefault()
    gotoPage(page.value - 1)
  }
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const data = await fetchTalents({ page: 1, size: 100, visitorKey: visitorKey() })
    people.value = (data?.records || []).map(adaptTalent)
    protoH = 0
  } catch (error) {
    loadError.value = error.message || '人才加载失败'
  } finally {
    loading.value = false
    await nextTick()
    scheduleFit()
  }
}

onMounted(() => {
  document.title = '人才 · FDELab'
  window.addEventListener('keydown', onKeydown)
  window.addEventListener('resize', onGridResize)
  document.addEventListener('click', onDocumentClick)
  gridRef.value?.addEventListener('pointermove', onGridPointerMove)
  gridRef.value?.addEventListener('pointerout', onGridPointerOut)
  cleanupCanvas = setupCanvas()
  load()
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown)
  window.removeEventListener('resize', onGridResize)
  document.removeEventListener('click', onDocumentClick)
  gridRef.value?.removeEventListener('pointermove', onGridPointerMove)
  gridRef.value?.removeEventListener('pointerout', onGridPointerOut)
  cancelAnimationFrame(rafTilt)
  cancelAnimationFrame(rafField)
  clearTimeout(searchTimer)
  clearTimeout(resizeTimer)
  cleanupCanvas?.()
})
</script>

<style scoped>
.field {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

.wrap {
  position: relative;
  z-index: 1;
  max-width: 1320px;
  margin: 0 auto;
  padding: 34px 6vw 90px;
}

.topbar {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 34px;
}

.brandmark {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 22px;
  letter-spacing: .05em;
}

.breadcrumb {
  font-size: 12.5px;
  color: var(--muted);
  letter-spacing: .06em;
}

.head {
  margin-bottom: 30px;
}

.kicker {
  font-size: 11px;
  letter-spacing: .26em;
  color: var(--gold);
  margin: 0 0 12px;
}

h1 {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 54px;
  font-weight: 500;
  margin: 0;
  letter-spacing: .01em;
  line-height: 1.1;
}

.lead {
  margin: 14px 0 0;
  color: var(--muted);
  font-size: 14.5px;
  line-height: 1.75;
  max-width: 660px;
}

.stats {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 16px;
  font-size: 12px;
  color: var(--muted);
  letter-spacing: .06em;
  flex-wrap: wrap;
}

.stats b {
  font-size: 12px;
  color: var(--ink);
  font-weight: 500;
  margin-right: 3px;
  font-variant-numeric: tabular-nums;
}

.stats .sep {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--gold);
  opacity: .6;
}

.toolbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  flex-wrap: wrap;
  gap: 14px 20px;
  align-items: flex-end;
  justify-content: space-between;
  margin: 36px 0 28px;
  padding-bottom: 0;
  border-bottom: 1px solid var(--line);
  background: rgba(244,241,234,.95);
  -webkit-backdrop-filter: blur(10px) saturate(1.05);
  backdrop-filter: blur(10px) saturate(1.05);
}

.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.tab {
  position: relative;
  border: 0;
  background: none;
  padding: 0 0 14px;
  font-family: inherit;
  font-size: 13px;
  color: var(--muted);
  cursor: pointer;
  letter-spacing: .02em;
  transition: color .2s ease;
}

.tab:hover {
  color: var(--ink);
}

.tab.on {
  color: var(--ink);
}

.tab.on::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: var(--accent);
}

.right {
  display: flex;
  gap: 18px;
  align-items: center;
  padding-bottom: 10px;
}

.pick {
  position: relative;
}

.pick-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border: 0;
  background: none;
  padding: 5px 0;
  font-family: inherit;
  font-size: 12.5px;
  color: var(--muted);
  cursor: pointer;
  letter-spacing: .02em;
  transition: color .2s ease;
}

.pick-btn:hover,
.pick.open .pick-btn {
  color: var(--ink);
}

.pick.picked .pick-btn {
  color: var(--accent);
}

.chev {
  width: 6px;
  height: 6px;
  border-right: 1px solid currentColor;
  border-bottom: 1px solid currentColor;
  transform: rotate(45deg) translate(-1px, -1px);
  opacity: .65;
  transition: transform .22s ease, opacity .22s ease;
}

.pick.open .chev {
  transform: rotate(-135deg) translate(-1px, -1px);
  opacity: 1;
}

.pick-panel {
  position: absolute;
  right: 0;
  top: calc(100% + 9px);
  min-width: 208px;
  background: #fffdf8;
  border: 1px solid rgba(227,220,207,.9);
  border-radius: 12px;
  box-shadow: 0 1px 0 rgba(255,255,255,.9) inset, 0 20px 42px -24px rgba(23,36,30,.45);
  padding: 7px;
  z-index: 30;
  display: none;
}

.pick.open .pick-panel {
  display: block;
  animation: pickIn .18s ease-out both;
}

@keyframes pickIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.pick-item {
  display: flex;
  align-items: center;
  gap: 9px;
  width: 100%;
  border: 0;
  background: none;
  text-align: left;
  font-family: inherit;
  font-size: 12.5px;
  color: #4c574f;
  padding: 7px 9px 7px 7px;
  border-radius: 7px;
  cursor: pointer;
  transition: background .14s ease, color .14s ease;
}

.pick-item:hover {
  background: rgba(31,107,74,.07);
  color: var(--ink);
}

.pick-item .tick {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: transparent;
  flex-shrink: 0;
}

.pick-item.on {
  color: var(--ink);
}

.pick-item.on .tick {
  background: var(--gold);
}

.search {
  border: 0;
  border-bottom: 1px solid var(--line);
  background: transparent;
  border-radius: 0;
  padding: 5px 2px;
  font-size: 12.5px;
  width: 150px;
  color: var(--ink);
  font-family: inherit;
  outline: none;
  transition: border-color .2s ease;
}

.search::placeholder {
  color: #a8b0aa;
}

.search:focus {
  border-color: var(--accent);
}

.count {
  font-size: 11.5px;
  color: var(--muted);
  letter-spacing: .08em;
  white-space: nowrap;
}

.notice {
  margin: -12px 0 18px;
  color: #8a5a2a;
  font-size: 12px;
}

.viewtoggle {
  display: inline-flex;
  align-items: center;
  gap: 11px;
}

.vbtn {
  border: 0;
  background: none;
  font-family: inherit;
  font-size: 12px;
  color: var(--muted);
  cursor: pointer;
  padding: 5px 0;
  letter-spacing: .02em;
  position: relative;
  transition: color .18s ease;
}

.vbtn:hover {
  color: var(--ink);
}

.vbtn.on {
  color: var(--ink);
}

.vbtn.on::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: var(--accent);
}

.vsep {
  width: 1px;
  height: 11px;
  background: var(--line);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 30px 28px;
  perspective: 1400px;
}

.grid.fill {
  grid-auto-rows: 1fr;
}

.grid.fill .role {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 0;
}

.grid.fill .case .ctitle {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.grid.fill .tags {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  transform-style: preserve-3d;
  will-change: transform;
  background-color: #fffdf8;
  background-image:
    radial-gradient(ellipse 64% 50% at 18% 8%, rgba(31,107,74,.075), transparent 62%),
    radial-gradient(ellipse 58% 46% at 90% 92%, rgba(196,165,116,.15), transparent 62%),
    linear-gradient(168deg, #fffdf8, #fcf8f0);
  border: 0;
  border-radius: 12px;
  padding: 26px 26px 20px;
  cursor: pointer;
  box-shadow:
    0 1px 2px rgba(23,36,30,.05),
    0 16px 34px -26px rgba(23,36,30,.30),
    0 1px 0 rgba(255,255,255,.9) inset;
  transition: box-shadow .35s ease;
}

.card::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  z-index: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(31,107,74,.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(31,107,74,.045) 1px, transparent 1px);
  background-size: 24px 24px;
  background-position: center;
  -webkit-mask-image: radial-gradient(ellipse 88% 74% at 50% 36%, #000 10%, transparent 78%);
  mask-image: radial-gradient(ellipse 88% 74% at 50% 36%, #000 10%, transparent 78%);
}

.card > *:not(.shine) {
  position: relative;
  z-index: 1;
}

.card .shine {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  transform: translateZ(48px) scale(.95);
  background: radial-gradient(circle 150px at var(--mx, 50%) var(--my, 40%),
    rgba(255,255,255,.85) 0%, rgba(196,165,116,.62) 22%, transparent 62%);
  mix-blend-mode: soft-light;
  opacity: 0;
  transition: opacity .4s ease;
}

.card:hover .shine {
  opacity: .9;
}

.card:hover {
  box-shadow:
    0 1px 2px rgba(23,36,30,.06),
    0 28px 50px -30px rgba(23,36,30,.40),
    0 1px 0 rgba(255,255,255,.9) inset;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  transform: translateZ(18px);
}

.mark {
  font-size: 9.5px;
  letter-spacing: .24em;
  color: var(--gold);
}

.city {
  font-size: 11px;
  color: var(--muted);
  white-space: nowrap;
  letter-spacing: .04em;
}

.name {
  transform: translateZ(26px);
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 29px;
  font-weight: 500;
  margin: 18px 0 6px;
  letter-spacing: .02em;
  line-height: 1.15;
}

.role {
  transform: translateZ(22px);
  font-size: 12.5px;
  color: var(--accent);
  line-height: 1.55;
  min-height: 20px;
}

.meta {
  transform: translateZ(16px);
  margin-top: 14px;
  font-size: 11px;
  color: var(--muted);
  letter-spacing: .1em;
}

.meta b {
  color: var(--ink);
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

.tags {
  transform: translateZ(14px);
  margin-top: 8px;
  font-size: 11.5px;
  color: #6d786f;
  line-height: 1.7;
}

.dotsep {
  color: var(--gold);
  margin: 0 6px;
  opacity: .75;
}

.tags .dm {
  color: #8a7a5e;
}

.case {
  transform: translateZ(8px);
  margin-top: 20px;
  padding-left: 14px;
  border-left: 2px solid rgba(31,107,74,.3);
}

.case .clabel {
  font-size: 9.5px;
  color: var(--gold);
  letter-spacing: .2em;
}

.case .ctitle {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 15px;
  line-height: 1.6;
  margin-top: 7px;
  color: var(--ink);
}

.case .cbudget {
  font-size: 11.5px;
  color: var(--muted);
  margin-top: 7px;
  letter-spacing: .02em;
}

.case.empty {
  border-left-color: rgba(196,165,116,.45);
  color: var(--muted);
  font-size: 12px;
  line-height: 1.7;
}

.foot {
  transform: translateZ(12px);
  display: flex;
  gap: 18px;
  align-items: center;
  margin-top: auto;
  padding-top: 24px;
}

.btn {
  border: 0;
  background: none;
  padding: 0;
  color: var(--muted);
  font-size: 12px;
  cursor: pointer;
  font-family: inherit;
  letter-spacing: .02em;
  transition: color .18s ease;
}

.btn:hover {
  color: var(--accent);
}

.btn.solid {
  color: var(--accent);
  font-weight: 500;
}

.btn.solid:hover {
  background: #1a5b3f;
  color: #fff;
}

.foot .like {
  margin-left: auto;
}

.foot .like.liked {
  color: var(--accent);
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: var(--muted);
  border: 1px dashed var(--line);
  border-radius: 16px;
}

.minipager {
  position: fixed;
  right: 24px;
  bottom: 20px;
  z-index: 25;
  display: none;
  align-items: center;
  gap: 1px;
  background: rgba(255,253,248,.94);
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 4px 6px;
  box-shadow: 0 12px 30px -18px rgba(23,36,30,.45);
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
}

.minipager.show {
  display: flex;
}

.minipager button {
  border: 0;
  background: none;
  cursor: pointer;
  font-family: inherit;
  color: var(--muted);
  font-size: 13px;
  min-width: 24px;
  height: 24px;
  border-radius: 999px;
  line-height: 1;
  transition: all .15s ease;
}

.minipager button:hover:not(:disabled) {
  color: var(--ink);
  background: rgba(31,107,74,.08);
}

.minipager button:disabled {
  opacity: .28;
  cursor: default;
}

.minipager .mnum {
  font-size: 11.5px;
  color: var(--muted);
  padding: 0 5px;
  letter-spacing: .06em;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.minipager .mnum b {
  color: var(--accent);
  font-weight: 500;
  font-size: 12.5px;
}

.list {
  border-top: 1px solid var(--line);
}

.row {
  display: grid;
  grid-template-columns: 92px minmax(150px, 1fr) minmax(170px, 1.05fr) 40px minmax(150px, 1.45fr) 14px;
  gap: 18px;
  align-items: center;
  padding: 15px 6px 15px 0;
  border-bottom: 1px solid rgba(227,220,207,.55);
  cursor: pointer;
  transition: background .16s ease;
}

.row:hover {
  background: rgba(255,253,248,.8);
}

.row .rname {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 16.5px;
  letter-spacing: .02em;
}

.row .rrole {
  font-size: 12px;
  color: var(--accent);
  line-height: 1.45;
}

.row .rtags {
  font-size: 11.5px;
  color: #6d786f;
  line-height: 1.6;
}

.row .rcase {
  font-size: 11.5px;
  color: var(--muted);
  font-variant-numeric: tabular-nums;
  text-align: right;
}

.row .rcase b {
  color: var(--ink);
  font-weight: 500;
}

.row .rrep {
  font-size: 12.5px;
  line-height: 1.5;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row .rrep.empty {
  color: #a8b0aa;
}

.row .rgo {
  color: var(--gold);
  font-size: 13px;
  opacity: 0;
  transition: opacity .16s ease;
}

.row:hover .rgo {
  opacity: 1;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
  flex-wrap: wrap;
}

.pmeta {
  font-size: 11.5px;
  color: var(--muted);
  letter-spacing: .05em;
  font-variant-numeric: tabular-nums;
}

.pnums {
  display: flex;
  align-items: center;
  gap: 3px;
}

.pnum {
  border: 0;
  background: none;
  font-family: inherit;
  font-size: 12px;
  color: var(--muted);
  cursor: pointer;
  min-width: 27px;
  height: 27px;
  padding: 0 7px;
  border-radius: 7px;
  transition: all .16s ease;
  font-variant-numeric: tabular-nums;
}

.pnum:hover:not(:disabled):not(.on) {
  color: var(--ink);
  background: rgba(31,107,74,.07);
}

.pnum.on {
  color: var(--paper);
  background: var(--accent);
}

.pnum:disabled {
  opacity: .28;
  cursor: default;
}

.drawer-mask {
  position: fixed;
  inset: 0;
  background: rgba(23,36,30,.34);
  backdrop-filter: blur(3px);
  display: none;
  z-index: 40;
  align-items: stretch;
  justify-content: flex-end;
}

.drawer-mask.open {
  display: flex;
}

.drawer {
  width: min(520px, 100%);
  background: var(--paper);
  height: 100%;
  overflow-y: auto;
  padding: 30px 32px 60px;
  box-shadow: -30px 0 60px -30px rgba(0,0,0,.4);
  transform: translateX(24px);
  opacity: 0;
  transition: all .26s ease;
}

.drawer-mask.open .drawer {
  transform: none;
  opacity: 1;
}

.drawer .close {
  float: right;
  border: 0;
  background: transparent;
  font-size: 20px;
  color: var(--muted);
  cursor: pointer;
  line-height: 1;
}

.drawer-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: .2em;
  color: #c4a574;
}

.drawer h2 {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 34px;
  font-weight: 500;
  margin: 6px 0 0;
}

.drawer .drole {
  font-size: 13px;
  color: var(--accent);
  margin-top: 4px;
}

.drawer-tags {
  margin-top: 14px;
}

.drawer-tags .tag {
  display: inline-block;
  font-size: 11.5px;
  border-radius: 999px;
  padding: 4px 10px;
  border: 1px solid var(--line);
  color: var(--muted);
  background: rgba(255,255,255,.55);
  margin: 0 6px 6px 0;
}

.drawer-tags .tag.domain {
  border-style: dashed;
  color: #7a6a4f;
  border-color: #e0d3ba;
}

.dsec {
  margin-top: 26px;
}

.dsec h3 {
  font-size: 12px;
  letter-spacing: .12em;
  color: var(--gold);
  margin: 0 0 10px;
  font-weight: 500;
}

.dsec p {
  margin: 0 0 8px;
  font-size: 13.2px;
  line-height: 1.75;
  color: #2c3731;
}

.dsec p b {
  font-weight: 500;
}

.dcase {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 14px 16px;
  background: var(--card);
  margin-bottom: 10px;
}

.dcase .ind {
  font-size: 11px;
  color: var(--accent);
}

.dcase h4 {
  margin: 4px 0 6px;
  font-size: 14.5px;
  font-weight: 500;
}

.dcase .dportrait {
  font-size: 12.6px;
  color: #5e6a63;
  margin: 0;
}

.dcase ul {
  margin: 6px 0 0;
  padding-left: 16px;
  font-size: 12.6px;
  color: #46524c;
  line-height: 1.7;
}

.dcase .meta {
  margin-top: 8px;
  font-size: 11.8px;
  color: var(--muted);
}

.dcase .meta.pitfall {
  color: #8a5a2a;
}

.muted-line {
  color: #5e6a63;
}

.drawer .cta {
  margin-top: 26px;
  display: flex;
  gap: 10px;
}

.drawer .cta .btn {
  border: 1px solid var(--line);
  padding: 8px 15px;
  border-radius: 999px;
  color: var(--ink);
}

.drawer .cta .btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.drawer .cta .btn.solid {
  background: var(--accent);
  border-color: var(--accent);
  color: var(--paper);
}

.drawer .cta .btn.solid:hover {
  background: #164b34;
  color: #fff;
}

@media (max-width: 900px) {
  .row {
    grid-template-columns: 84px 1fr 36px 14px;
    gap: 14px;
  }

  .row .rtags,
  .row .rrep {
    display: none;
  }
}

@media (max-width: 640px) {
  h1 {
    font-size: 38px;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .tabs {
    gap: 16px;
  }

  .right {
    width: 100%;
    flex-wrap: wrap;
    gap: 12px;
    padding-bottom: 12px;
  }

  .pick-panel {
    right: auto;
    left: 0;
  }

  .search {
    width: 100%;
  }

  .card {
    padding: 22px 20px 18px;
  }

  .name {
    font-size: 25px;
  }

  .row {
    grid-template-columns: 1fr 36px;
  }

  .row .rrole {
    display: none;
  }

  .vsep {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .pick.open .pick-panel {
    animation: none;
  }

  .card,
  .drawer,
  .btn,
  .tab,
  .vbtn,
  .pnum,
  .pick-btn,
  .chev,
  .row {
    transition: none;
  }

  .drawer {
    transform: none;
    opacity: 1;
  }
}
</style>
