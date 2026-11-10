<template>
  <div class="profile-page">
    <SiteNav active="talents" />

    <div v-if="profile" class="dock" :class="{ show: dockVisible }">
      <div class="dock-in">
        <span class="dock-ava" :class="levelClass">{{ initial }}</span>
        <span class="dock-name">
          {{ profile.name }}
          <span v-if="profile.levelCode" class="badge">{{ profile.levelCode }} {{ profile.levelName }}</span>
        </span>
        <template v-if="profile.accepting === 1">
          <span class="status"><i />{{ COPY.accepting }}</span>
          <span class="dock-sp" />
          <button class="btn btn-p" type="button" @click="openDevDialog('')">{{ COPY.ctaMain }}</button>
        </template>
        <template v-else>
          <span class="status off"><i />{{ COPY.paused }}</span>
          <span class="dock-sp" />
          <button class="btn btn-g" type="button" disabled>{{ COPY.ctaPaused }}</button>
        </template>
      </div>
      <nav v-if="sections.length" class="dock-anchors">
        <a
          v-for="(section, index) in sections"
          :key="`${section.key}-${index}`"
          href="javascript:void(0)"
          :class="{ on: activeAnchor === index }"
          @click.prevent="scrollToSection(index)"
        >{{ section.title }}</a>
      </nav>
    </div>

    <main>
      <section v-if="loading" class="wrap state">
        <p class="state-title">{{ COPY.loading }}</p>
      </section>

      <section v-else-if="!profile" class="wrap state">
        <p class="state-title">{{ error || COPY.notFound }}</p>
        <RouterLink class="state-link" to="/talents">{{ COPY.backToList }}</RouterLink>
      </section>

      <template v-else>
        <section class="wrap">
          <div class="crumb"><RouterLink to="/talents">{{ COPY.crumb }}</RouterLink><i>/</i><span>{{ profile.name }}</span></div>

          <header ref="heroEl" class="hero">
            <div class="ava" :class="levelClass">
              <img v-if="profile.avatarUrl" :src="profile.avatarUrl" :alt="profile.name" />
              <template v-else>{{ initial }}</template>
            </div>
            <div class="hero-body">
              <div class="hero-name">
                {{ profile.name }}
                <span v-if="profile.levelCode" class="badge">{{ profile.levelCode }} {{ profile.levelName }}</span>
              </div>
              <div v-if="profile.headline" class="hero-head">{{ profile.headline }}</div>
              <p v-if="profile.summary" class="hero-sum">{{ profile.summary }}</p>
              <div v-if="hasTags" class="tgroups">
                <div v-if="profile.identityTags.length" class="tgroup">
                  <span class="tlabel">身份</span>
                  <span v-for="tag in profile.identityTags" :key="`idt-${tag}`" class="tag idt">{{ tag }}</span>
                </div>
                <div v-if="profile.industryTags.length" class="tgroup">
                  <span class="tlabel">行业</span>
                  <span v-for="tag in profile.industryTags" :key="`ind-${tag}`" class="tag ind">{{ tag }}</span>
                </div>
                <div v-if="profile.skills.length" class="tgroup">
                  <span class="tlabel">技能</span>
                  <span v-for="tag in profile.skills" :key="`skl-${tag}`" class="tag skl">{{ tag }}</span>
                </div>
              </div>
            </div>
            <div class="hero-side">
              <template v-if="profile.accepting === 1">
                <button class="btn btn-p" type="button" @click="openDevDialog('')">{{ COPY.ctaMain }}</button>
                <div class="status"><i />{{ COPY.accepting }}</div>
              </template>
              <template v-else>
                <button class="btn btn-g" type="button" disabled>{{ COPY.ctaPaused }}</button>
                <div class="status off"><i />{{ COPY.paused }}</div>
              </template>
              <button class="like" type="button" :disabled="likeBusy" @click="onLike">
                {{ liked ? COPY.liked : COPY.like }} <span>{{ likeCount }}</span>
              </button>
              <span v-if="likeError" class="like-err">{{ likeError }}</span>
            </div>
          </header>
        </section>

        <section class="wrap cols">
          <aside ref="asideEl" class="aside" :style="{ top: asideTop }">
            <div class="panel idcard">
              <span class="ava-sm" :class="levelClass">
                <img v-if="profile.avatarUrl" :src="profile.avatarUrl" :alt="profile.name" />
                <template v-else>{{ initial }}</template>
              </span>
              <div class="idmeta">
                <span v-if="profile.levelCode" class="badge">{{ profile.levelCode }} {{ profile.levelName }}</span>
                <div v-if="profile.years != null" class="yrs">{{ COPY.years(profile.years) }}</div>
                <div v-if="profile.industryTags.length" class="yrs">{{ profile.industryTags[0] }}</div>
              </div>
            </div>

            <div v-if="radarModel" class="panel">
              <div class="ptitle">{{ COPY.radarTitle }}</div>
              <div ref="radarWrap" class="radar-wrap" @pointerleave="hideTip">
                <svg viewBox="0 0 260 260" role="img" :aria-label="radarAria" @pointermove="onRadarMove">
                  <g fill="none" stroke="#e8ecf4" stroke-width="1">
                    <polygon v-for="(ring, index) in radarModel.rings" :key="`ring-${index}`" :points="ring" />
                  </g>
                  <g stroke="#eef1f7" stroke-width="1">
                    <line
                      v-for="(axis, index) in radarModel.axes"
                      :key="`axis-${index}`"
                      :x1="radarModel.cx"
                      :y1="radarModel.cy"
                      :x2="axis.x"
                      :y2="axis.y"
                    />
                  </g>
                  <polygon :points="radarModel.area" fill="rgba(46,107,255,.13)" stroke="#2e6bff" stroke-width="1.5" />
                  <g fill="#2e6bff">
                    <circle v-for="(dot, index) in radarModel.dots" :key="`dot-${index}`" :cx="dot.x" :cy="dot.y" r="2.6" />
                  </g>
                  <g fill="transparent" pointer-events="all">
                    <circle
                      v-for="(dot, index) in radarModel.dots"
                      :key="`hit-${index}`"
                      data-hit="1"
                      :data-index="index"
                      :cx="dot.x"
                      :cy="dot.y"
                      r="13"
                    />
                  </g>
                  <g fill="#66708a" font-size="11">
                    <text
                      v-for="(label, index) in radarModel.labels"
                      :key="`label-${index}`"
                      :x="label.x"
                      :y="label.y"
                      :text-anchor="label.anchor"
                      dominant-baseline="central"
                    >{{ label.text }}</text>
                  </g>
                </svg>
                <div ref="tipEl" class="radar-tip" :class="{ show: tipVisible }">
                  <div class="rt-title">{{ radarTipTitle }}</div>
                  <ul>
                    <li v-for="(dim, index) in profile.radarDims" :key="dim">
                      <span>{{ dim }}</span><b>{{ profile.radar[index] }}</b>
                    </li>
                  </ul>
                </div>
              </div>
              <p class="cap">{{ COPY.radarCap }}<b>{{ radarStrongest.dim }}</b> {{ radarStrongest.value }}/10{{ COPY.radarCapSuffix }}</p>
            </div>

            <div class="panel">
              <div class="ptitle">{{ COPY.certTitle }}</div>
              <div class="cert">
                <div>{{ COPY.certLevel }}　<b :class="{ muted: !profile.levelName }">{{ profile.levelName || COPY.certNone }}</b></div>
                <div>{{ COPY.certReview }}　<span class="muted">{{ reviewMonth || COPY.certNone }}</span></div>
              </div>
            </div>
          </aside>

          <div>
            <section
              v-for="(section, index) in sections"
              :id="`sec-${index}`"
              :key="`${section.key}-${index}`"
              class="sec"
            >
              <h3><span class="n">{{ String(index + 1).padStart(2, '0') }}</span>{{ section.title }}</h3>

              <template v-if="section.key === 'roles'">
                <div class="roles">
                  <div v-for="(role, roleIndex) in profile.roles" :key="`role-${roleIndex}`" class="role">
                    <b v-if="role.org">{{ role.org }}</b>
                    <span v-if="role.title" class="rt">{{ role.title }}</span>
                    <p v-if="role.description">{{ role.description }}</p>
                  </div>
                </div>
              </template>

              <template v-else-if="section.key === 'cases'">
                <article v-for="item in profile.cases" :key="item.code" class="case">
                  <div v-if="item.industry" class="cind">{{ item.industry }}</div>
                  <h4>
                    <span>{{ item.title }}</span>
                    <a class="clink" :href="item.url">{{ COPY.casesLink }}</a>
                  </h4>
                  <p v-if="item.pain" class="portrait">{{ item.pain }}</p>
                  <ul v-if="item.points.length">
                    <li v-for="(point, pointIndex) in item.points" :key="`point-${pointIndex}`">{{ point }}</li>
                  </ul>
                  <template v-if="item.delivery">
                    <div class="clabel">{{ COPY.deliveryLabel }}</div>
                    <p class="delivery"><b>{{ COPY.deliveryPrefix }}</b>{{ item.delivery }}</p>
                  </template>
                  <div v-if="item.cred" class="cred" :title="item.credNote || ''">可信度 {{ item.cred }}</div>
                </article>
                <p class="foot-note">{{ COPY.casesNote }}</p>
              </template>

              <template v-else-if="section.key === 'services'">
                <div v-for="(service, serviceIndex) in profile.services" :key="`svc-${serviceIndex}`" class="svc">
                  <div class="svc-b">
                    <div v-if="service.title" class="svc-t">{{ service.title }}</div>
                    <div v-if="service.description" class="svc-d">{{ service.description }}</div>
                  </div>
                  <button
                    class="svc-a"
                    type="button"
                    :disabled="profile.accepting !== 1"
                    @click="openDevDialog(service.title)"
                  >{{ COPY.servicesCta }}</button>
                </div>
              </template>

              <template v-else-if="section.key === 'collaborations'">
                <div class="collab">
                  <div v-for="(item, collabIndex) in profile.collaborations" :key="`collab-${collabIndex}`" class="collab-i">
                    <span class="dot" />
                    <div>
                      <b v-if="item.title">{{ item.title }}</b>
                      <span v-if="item.description">{{ item.description }}</span>
                    </div>
                  </div>
                </div>
              </template>

              <template v-else-if="section.key === 'projects'">
                <div class="table-scroll">
                  <table class="rt">
                    <thead>
                      <tr>
                        <th style="width: 44%">{{ COPY.projectHeaders.title }}</th>
                        <th style="width: 18%">{{ COPY.projectHeaders.role }}</th>
                        <th>{{ COPY.projectHeaders.delivery }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(project, projectIndex) in profile.projects" :key="`project-${projectIndex}`">
                        <td><b>{{ project.title }}</b></td>
                        <td>{{ project.role }}</td>
                        <td>{{ project.delivery }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </template>

              <template v-else-if="section.key === 'experiences'">
                <div class="fold" :class="{ open: expOpen }">
                  <button class="fold-h" type="button" @click="expOpen = !expOpen">
                    <span>{{ COPY.expSummary(profile.experiences.length) }}</span>
                    <em>{{ expOpen ? COPY.expClose : COPY.expOpen }}
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M6 9l6 6 6-6" />
                      </svg>
                    </em>
                  </button>
                  <div v-if="expOpen" class="fold-b">
                    <template v-for="(item, expIndex) in profile.experiences" :key="`exp-${expIndex}`">
                      <div v-if="item.title" class="exp-t">{{ item.title }}</div>
                      <div v-if="item.description" class="exp-d">{{ item.description }}</div>
                    </template>
                  </div>
                </div>
              </template>
            </section>
          </div>
        </section>
      </template>
    </main>

    <div v-if="devDialog.open" class="mask" @click.self="closeDevDialog">
      <div class="modal">
        <div class="m-head">
          <div>
            <h2>{{ COPY.devTitle }}</h2>
            <p>{{ devDialog.context ? `「${devDialog.context}」：` : '' }}{{ COPY.devBody }}</p>
          </div>
          <button class="m-x" type="button" @click="closeDevDialog">×</button>
        </div>
        <div class="m-foot">
          <RouterLink class="btn-ghost" :to="devDialog.url || '/articles'">{{ COPY.devCasesLink }}</RouterLink>
          <button class="btn btn-p" type="button" @click="closeDevDialog">{{ COPY.devClose }}</button>
        </div>
      </div>
    </div>

    <SiteFooter />
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import SiteNav from '../components/SiteNav.vue'
import SiteFooter from '../components/SiteFooter.vue'
import { fetchTalentProfile, toggleTalentLike } from '../api/talent'
import { visitorKey } from '../utils/host'
import { PROFILE_COPY as COPY } from '../constants/copy'

const RADAR_CX = 130
const RADAR_CY = 138
const RADAR_R = 88

const route = useRoute()
const loading = ref(true)
const error = ref('')
const profile = ref(null)
const liked = ref(false)
const likeBusy = ref(false)
const likeError = ref('')

const heroEl = ref(null)
const asideEl = ref(null)
const asideTop = ref('20px')
const dockVisible = ref(false)
const activeAnchor = ref(0)

const radarWrap = ref(null)
const tipEl = ref(null)
const tipVisible = ref(false)

const expOpen = ref(false)
const devDialog = reactive({ open: false, context: '', url: '' })

const initial = computed(() => Array.from(String(profile.value?.name || '').trim())[0] || '?')
const levelClass = computed(() => String(profile.value?.levelCode || '').toLowerCase())
const sections = computed(() => profile.value?.sections || [])
const likeCount = computed(() => Number(profile.value?.likeCount || 0))
const reviewMonth = computed(() => String(profile.value?.reviewTime || '').slice(0, 7))
const hasTags = computed(() => {
  const data = profile.value
  if (!data) return false
  return Boolean(data.identityTags?.length || data.industryTags?.length || data.skills?.length)
})

const hasRadar = computed(() => {
  const data = profile.value
  return Array.isArray(data?.radar)
    && data.radar.length === 5
    && Array.isArray(data?.radarDims)
    && data.radarDims.length === 5
})

function radarPoint(index, radius) {
  const angle = -Math.PI / 2 + (index * 2 * Math.PI) / 5
  return {
    x: RADAR_CX + Math.cos(angle) * radius,
    y: RADAR_CY + Math.sin(angle) * radius,
  }
}

function polygonAt(radius) {
  return Array.from({ length: 5 }, (_, index) => {
    const point = radarPoint(index, radius)
    return `${point.x.toFixed(1)},${point.y.toFixed(1)}`
  }).join(' ')
}

const radarModel = computed(() => {
  if (!hasRadar.value) return null
  const values = profile.value.radar
  return {
    cx: RADAR_CX,
    cy: RADAR_CY,
    rings: [0.33, 0.66, 1].map((ratio) => polygonAt(RADAR_R * ratio)),
    axes: Array.from({ length: 5 }, (_, index) => radarPoint(index, RADAR_R)),
    area: values.map((value, index) => {
      const point = radarPoint(index, (RADAR_R * Number(value)) / 10)
      return `${point.x.toFixed(1)},${point.y.toFixed(1)}`
    }).join(' '),
    dots: values.map((value, index) => {
      const point = radarPoint(index, (RADAR_R * Number(value)) / 10)
      return { x: point.x.toFixed(1), y: point.y.toFixed(1) }
    }),
    labels: Array.from({ length: 5 }, (_, index) => {
      const point = radarPoint(index, RADAR_R + 22)
      const anchor = Math.abs(point.x - RADAR_CX) < 6 ? 'middle' : point.x > RADAR_CX ? 'start' : 'end'
      return { x: point.x.toFixed(1), y: point.y.toFixed(1), anchor, text: profile.value.radarDims[index] }
    }),
  }
})

const radarStrongest = computed(() => {
  if (!hasRadar.value) return { dim: '', value: '' }
  const values = profile.value.radar.map((value) => Number(value))
  const strongest = Math.max(...values)
  const index = values.indexOf(strongest)
  return {
    dim: profile.value.strengthDim || profile.value.radarDims[index] || '',
    value: strongest,
  }
})

const radarTipTitle = computed(() => {
  const data = profile.value
  if (!data) return ''
  return data.levelName ? `${data.name} · ${data.levelName}` : data.name
})

const radarAria = computed(() => {
  const data = profile.value
  if (!hasRadar.value) return ''
  return `五维能力雷达：${data.radarDims.map((dim, index) => `${dim} ${data.radar[index]}`).join('、')}`
})

function placeTip(event) {
  const wrap = radarWrap.value
  const tip = tipEl.value
  if (!wrap || !tip) return
  const box = wrap.getBoundingClientRect()
  const width = tip.offsetWidth
  const height = tip.offsetHeight
  const mx = event.clientX - box.left
  const my = event.clientY - box.top
  let x = mx + 14
  let y = my + 14
  if (x + width > box.width - 2) x = mx - width - 14
  x = Math.max(2, Math.min(x, box.width - width - 2))
  y = Math.max(2, Math.min(y, box.height - height - 2))
  tip.style.left = `${x}px`
  tip.style.top = `${y}px`
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

let likeErrorTimer = 0

async function onLike() {
  if (!profile.value || likeBusy.value) return
  likeBusy.value = true
  likeError.value = ''
  try {
    const result = await toggleTalentLike(profile.value.id, visitorKey())
    liked.value = Boolean(result?.liked)
    profile.value.likeCount = Number(result?.count ?? profile.value.likeCount)
  } catch {
    likeError.value = COPY.likeFailed
    clearTimeout(likeErrorTimer)
    likeErrorTimer = setTimeout(() => {
      likeError.value = ''
    }, 2400)
  } finally {
    likeBusy.value = false
  }
}

function openDevDialog(context) {
  devDialog.context = context || ''
  devDialog.url = profile.value?.cases?.[0]?.url || '/articles'
  devDialog.open = true
}

function closeDevDialog() {
  devDialog.open = false
}

function scrollToSection(index) {
  const element = document.getElementById(`sec-${index}`)
  if (!element) return
  const top = element.getBoundingClientRect().top + window.scrollY - 96
  window.scrollTo({ top, behavior: 'smooth' })
}

function updateAsideTop() {
  if (!asideEl.value) return
  asideTop.value = `${Math.max(16, (window.innerHeight - asideEl.value.offsetHeight) / 2)}px`
}

function onScroll() {
  dockVisible.value = Boolean(heroEl.value && heroEl.value.getBoundingClientRect().bottom < 0)
  let current = 0
  sections.value.forEach((_, index) => {
    const element = document.getElementById(`sec-${index}`)
    if (element && element.getBoundingClientRect().top <= 170) current = index
  })
  activeAnchor.value = current
}

function onResize() {
  updateAsideTop()
  onScroll()
}

async function load(slug) {
  loading.value = true
  error.value = ''
  profile.value = null
  try {
    const data = await fetchTalentProfile(slug)
    profile.value = data
    liked.value = false
    expOpen.value = false
    document.title = `${data.name} · FDE 智研社`
    await nextTick()
    updateAsideTop()
    onScroll()
  } catch (err) {
    error.value = err?.message || COPY.detailFailed
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load(String(route.params.slug || ''))
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', onResize)
  clearTimeout(likeErrorTimer)
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

button {
  font-family: inherit;
  cursor: pointer;
  border: 0;
  background: none;
  color: inherit;
  font-size: inherit;
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

/* ===== 粘性双行导航 ===== */
.dock {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 70;
  background: rgba(255, 255, 255, .95);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--line);
  transform: translateY(-100%);
  transition: transform .25s ease;
}

.dock.show {
  transform: none;
}

.dock-in {
  max-width: 1120px;
  margin: 0 auto;
  padding: 10px var(--gut);
  display: flex;
  align-items: center;
  gap: 12px;
}

.dock-ava {
  flex: none;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-family: var(--serif);
  font-size: 17px;
  font-weight: 500;
  color: #fff;
  background: linear-gradient(140deg, #5c8bff, var(--accent-deep));
  overflow: hidden;
}

.dock-ava.m {
  background: linear-gradient(140deg, #d8606f, var(--m));
}

.dock-ava.p {
  background: linear-gradient(140deg, #5c8bff, var(--p));
}

.dock-ava.a {
  background: linear-gradient(140deg, #4bb882, var(--a));
}

.dock-name {
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.dock-sp {
  flex: 1;
}

.dock-anchors {
  max-width: 1120px;
  margin: 0 auto;
  padding: 2px var(--gut) 8px;
  display: flex;
  gap: 2px;
  overflow-x: auto;
}

.dock-anchors a {
  font-size: 12px;
  color: var(--muted);
  padding: 4px 11px;
  border-radius: 6px;
  white-space: nowrap;
  transition: .15s ease;
}

.dock-anchors a:hover {
  color: var(--accent-deep);
  background: var(--blue-soft);
}

.dock-anchors a.on {
  color: var(--accent-deep);
  background: var(--blue-soft);
  font-weight: 500;
}

/* ===== 页头 ===== */
.crumb {
  margin-top: 26px;
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

.hero {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 28px 32px;
  margin-top: 18px;
  display: flex;
  gap: 22px;
  align-items: flex-start;
}

.ava {
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
  background: linear-gradient(140deg, #5c8bff, var(--accent-deep));
  box-shadow: 0 12px 24px -14px rgba(31, 86, 214, .75);
  overflow: hidden;
}

.ava.m {
  background: linear-gradient(140deg, #d8606f, var(--m));
  box-shadow: 0 12px 24px -14px rgba(180, 42, 60, .75);
}

.ava.p {
  background: linear-gradient(140deg, #5c8bff, var(--p));
}

.ava.a {
  background: linear-gradient(140deg, #4bb882, var(--a));
  box-shadow: 0 12px 24px -14px rgba(23, 116, 74, .75);
}

.ava img,
.ava-sm img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-body {
  flex: 1;
  min-width: 0;
}

.hero-name {
  font-family: var(--serif);
  font-size: 26px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.badge {
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: .06em;
  padding: 3px 9px;
  border-radius: 5px;
  background: var(--blue-soft);
  color: var(--accent-deep);
  border: 1px solid #d5e2fc;
  font-variant-numeric: tabular-nums;
}

.hero-head {
  font-size: 13.5px;
  color: var(--accent-deep);
  margin-top: 6px;
}

.hero-sum {
  font-size: 13px;
  color: var(--muted);
  margin-top: 8px;
  line-height: 1.75;
  max-width: 640px;
}

.tgroups {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.tgroup {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.tlabel {
  font-size: 11px;
  color: #98a1b8;
  letter-spacing: .08em;
  flex: none;
  width: 32px;
  padding-top: 3px;
}

.tag {
  font-size: 11px;
  padding: 2px 9px;
  border-radius: 5px;
  background: #f2f5fa;
  color: #5b6480;
}

.tag.idt {
  background: var(--blue-soft);
  color: var(--accent-deep);
  border: 1px solid #d5e2fc;
}

.tag.ind {
  background: var(--paper);
  border: 1px solid #edf0f7;
}

.tag.skl {
  background: #fff;
  border: 1px solid #d5e2fc;
  color: var(--accent-deep);
}

.hero-side {
  flex: none;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 22px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 500;
  transition: .15s ease;
  border: 1px solid transparent;
  white-space: nowrap;
}

.btn-p {
  background: var(--accent);
  color: #fff;
  box-shadow: 0 10px 22px -10px rgba(46, 107, 255, .75);
}

.btn-p:hover {
  background: var(--accent-deep);
}

.btn-g {
  background: #f2f5fa;
  border-color: var(--line);
  color: var(--muted);
  cursor: not-allowed;
}

.status {
  font-size: 11.5px;
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--a);
}

.status i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--a);
}

.status.off {
  color: var(--muted);
}

.status.off i {
  background: #a8b0c4;
}

.like {
  font-size: 12px;
  color: var(--muted);
  border: 1px solid var(--line);
  border-radius: 20px;
  padding: 4px 12px;
  background: #fff;
  transition: .15s ease;
}

.like:hover {
  border-color: #c8d6f4;
  color: var(--accent-deep);
}

.like:disabled {
  opacity: .6;
  cursor: default;
}

.like span {
  font-variant-numeric: tabular-nums;
}

.like-err {
  font-size: 11px;
  color: #b42a3c;
}

/* ===== 两栏 ===== */
.cols {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
  margin-top: 20px;
  padding-bottom: 90px;
}

.aside {
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
}

.panel {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: var(--shadow);
  padding: 18px 20px;
}

.ptitle {
  font-size: 12.5px;
  font-weight: 500;
  font-family: var(--serif);
  letter-spacing: .02em;
  margin-bottom: 12px;
}

.idcard {
  display: flex;
  gap: 14px;
  align-items: center;
}

.ava-sm {
  flex: 0 0 56px;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-family: var(--serif);
  font-size: 22px;
  color: #fff;
  background: linear-gradient(140deg, #5c8bff, var(--accent-deep));
  overflow: hidden;
}

.ava-sm.m {
  background: linear-gradient(140deg, #d8606f, var(--m));
}

.ava-sm.p {
  background: linear-gradient(140deg, #5c8bff, var(--p));
}

.ava-sm.a {
  background: linear-gradient(140deg, #4bb882, var(--a));
}

.idmeta .yrs {
  font-size: 12px;
  color: var(--muted);
  margin-top: 6px;
}

/* 雷达 */
.radar-wrap {
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

.radar-tip {
  position: absolute;
  z-index: 20;
  pointer-events: none;
  min-width: 150px;
  padding: 9px 13px 8px;
  background: #fff;
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
  font-weight: 500;
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

.cap b {
  color: var(--ink-soft);
  font-weight: 500;
}

.cert {
  font-size: 12px;
  color: #4a5169;
  line-height: 2;
}

.cert b {
  font-weight: 500;
  color: var(--ink-soft);
}

.cert .muted {
  color: var(--muted);
}

.cert b.muted {
  font-weight: 400;
}

/* ===== 右栏区块 ===== */
.sec {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: var(--shadow);
  padding: 22px 26px;
  margin-bottom: 16px;
  scroll-margin-top: 96px;
  min-width: 0;
}

.sec:last-child {
  margin-bottom: 0;
}

.sec h3 {
  font-family: var(--serif);
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.sec h3 .n {
  font-size: 10px;
  color: #b0b8cc;
  font-weight: 400;
  letter-spacing: .1em;
}

/* 案例 */
.case {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 18px 20px;
  margin-bottom: 14px;
  position: relative;
  overflow: hidden;
  background: #fff;
}

.case:last-of-type {
  margin-bottom: 0;
}

.case::before {
  content: "";
  position: absolute;
  left: 0;
  top: 16px;
  bottom: 16px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(#4d84ff, rgba(77, 132, 255, .12));
}

.case .cind {
  font-size: 11px;
  color: var(--accent-deep);
  letter-spacing: .05em;
}

.case h4 {
  font-family: var(--serif);
  font-size: 15.5px;
  font-weight: 500;
  margin: 6px 0 8px;
  line-height: 1.5;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: baseline;
}

.case h4 .clink {
  font-family: var(--sans);
  font-size: 11px;
  color: var(--accent-deep);
  white-space: nowrap;
  font-weight: 400;
}

.case .portrait {
  font-size: 12.5px;
  color: #4a5578;
  line-height: 1.75;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--line);
}

.case ul {
  margin: 10px 0 0 18px;
  font-size: 12.8px;
  color: #3c4560;
  line-height: 1.85;
}

.case ul li::marker {
  color: #8fabee;
}

.case .clabel {
  font-size: 9.5px;
  color: var(--accent);
  letter-spacing: .2em;
  margin-top: 12px;
}

.case .delivery {
  font-size: 12.5px;
  color: var(--muted);
  line-height: 1.75;
  background: var(--blue-soft);
  border-radius: 10px;
  padding: 10px 14px;
  margin-top: 6px;
}

.case .delivery b {
  color: var(--ink);
  font-weight: 500;
}

.case .cred {
  margin-top: 10px;
  font-size: 10.5px;
  color: #98a1b8;
  letter-spacing: .04em;
}

.foot-note {
  font-size: 11.5px;
  color: #98a1b8;
  line-height: 1.65;
  margin-top: 12px;
}

/* 服务 */
.svc {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 13px 0;
  border-bottom: 1px dashed var(--line);
}

.svc:last-child {
  border: 0;
  padding-bottom: 0;
}

.svc-b {
  flex: 1;
  min-width: 0;
}

.svc-t {
  font-size: 13.5px;
  font-weight: 500;
}

.svc-d {
  font-size: 12px;
  color: var(--muted);
  margin-top: 2px;
  line-height: 1.55;
}

.svc-a {
  font-size: 11.5px;
  color: var(--accent-deep);
  white-space: nowrap;
  padding: 5px 12px;
  border-radius: 7px;
  border: 1px solid #dbe5fb;
  background: #f7faff;
  transition: .15s ease;
}

.svc-a:hover {
  background: var(--blue-soft);
}

.svc-a[disabled] {
  color: #a8b0c4;
  border-color: var(--line);
  background: #f7f8fa;
  cursor: not-allowed;
}

/* 合作 / 身份 */
.collab,
.roles {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.role {
  display: flex;
  gap: 12px;
  align-items: baseline;
  padding: 12px 16px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fbfcfe;
  flex-wrap: wrap;
}

.role b {
  font-size: 13.5px;
  font-weight: 500;
}

.role .rt {
  font-size: 12px;
  color: var(--accent-deep);
}

.role p {
  flex-basis: 100%;
  font-size: 12.5px;
  color: var(--muted);
  line-height: 1.6;
}

.collab-i {
  display: flex;
  gap: 10px;
  padding: 11px 14px;
  border-radius: 10px;
  background: var(--paper);
  border: 1px solid #eef1f7;
}

.collab-i b {
  font-size: 12.5px;
  font-weight: 500;
  display: block;
}

.collab-i span {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.6;
}

.collab-i .dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--accent);
  margin-top: 7px;
  flex: none;
}

/* 项目表 */
.table-scroll {
  overflow-x: auto;
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
  letter-spacing: .1em;
  padding: 0 12px 10px 0;
  border-bottom: 1px solid var(--line);
}

table.rt td {
  font-size: 12.8px;
  line-height: 1.7;
  padding: 12px 12px 12px 0;
  border-bottom: 1px solid var(--line);
  vertical-align: top;
  color: var(--ink-soft);
  word-break: break-word;
}

table.rt td b {
  font-weight: 500;
  color: var(--ink);
}

table.rt tr:last-child td {
  border-bottom: 0;
}

/* 履历折叠 */
.fold {
  border: 1px solid var(--line);
  border-radius: 10px;
  overflow: hidden;
}

.fold-h {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 14px;
  font-size: 12.5px;
  color: var(--ink-soft);
  background: #fbfcfe;
  text-align: left;
}

.fold-h:hover {
  background: var(--paper);
}

.fold-h em {
  font-style: normal;
  font-size: 11px;
  color: #98a1b8;
  display: flex;
  align-items: center;
  gap: 5px;
}

.fold-h em svg {
  transition: .2s ease;
}

.fold.open .fold-h em svg {
  transform: rotate(180deg);
}

.fold-b {
  padding: 4px 14px 12px;
}

.exp-t {
  font-size: 12.5px;
  font-weight: 500;
  margin-top: 8px;
}

.exp-d {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.6;
}

/* ===== 开发中提示 ===== */
.mask {
  position: fixed;
  inset: 0;
  background: rgba(23, 28, 46, .42);
  backdrop-filter: blur(2px);
  z-index: 100;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 52px 20px;
  overflow: auto;
}

.modal {
  background: #fff;
  border-radius: 14px;
  width: 100%;
  max-width: 520px;
  box-shadow: 0 30px 70px -30px rgba(20, 30, 60, .5);
  overflow: hidden;
}

.m-head {
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.m-head h2 {
  font-size: 16px;
  font-weight: 500;
}

.m-head p {
  font-size: 12px;
  color: var(--muted);
  margin-top: 6px;
  line-height: 1.7;
}

.m-x {
  margin-left: auto;
  font-size: 18px;
  color: #b0b8cc;
  line-height: 1;
  padding: 2px 6px;
}

.m-x:hover {
  color: var(--ink);
}

.m-foot {
  padding: 14px 24px 20px;
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-end;
  border-top: 1px solid var(--line);
  background: #fbfcfe;
}

.btn-ghost {
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink-soft);
  padding: 9px 18px;
  border-radius: 10px;
  font-size: 13px;
}

:deep(.site-foot) {
  margin-top: 0;
}

@media (max-width: 1080px) {
  .cols {
    grid-template-columns: minmax(0, 1fr);
  }

  .aside {
    position: static;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .aside .panel:first-child {
    grid-column: span 2;
  }
}

@media (max-width: 760px) {
  .hero {
    flex-direction: column;
    padding: 22px;
  }

  .hero-side {
    align-items: flex-start;
  }

  .aside {
    grid-template-columns: 1fr;
  }

  .aside .panel:first-child {
    grid-column: span 1;
  }

  .sec {
    padding: 18px;
  }

  .dock-in {
    padding: 10px var(--gut) 6px;
  }

  .dock-name .badge {
    display: none;
  }

  table.rt {
    table-layout: auto;
    min-width: 520px;
  }
}
</style>
