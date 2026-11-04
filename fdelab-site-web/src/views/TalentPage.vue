<template>
  <div class="talent-detail">
    <article class="profile" :aria-busy="loading ? 'true' : 'false'">
      <div class="topbar">
        <span class="brandmark">FDELab</span>
        <router-link class="back" to="/talents">← 返回人才清单</router-link>
      </div>

      <header ref="heroRef" class="hero" @mousemove="moveShine">
        <div class="head-row">
          <div class="chip" aria-hidden="true"><span>{{ initial }}</span></div>
          <div class="head-main">
            <div class="mark">FDE TALENT</div>
            <h1 class="hero-name">{{ talent?.name || 'FDE' }}</h1>
            <p v-if="talent?.headline" class="headline">{{ talent.headline }}</p>
          </div>
        </div>
        <p v-if="talent?.summary" class="summary">{{ talent.summary }}</p>
        <div v-if="badges.length" class="badges">
          <span v-for="item in badges" :key="item" class="badge">{{ item }}</span>
        </div>
        <div class="hero-foot">
          <span v-if="talent?.location" class="loc">{{ talent.location }}</span>
          <div class="hero-actions">
            <button v-if="contacts.length" class="btn contact" type="button" @click="scrollToContact">联系 TA</button>
            <button class="btn solid like" :class="{ liked: talent?.liked }" type="button" @click="like">
              <span>▲ {{ talent?.liked ? '已赞' : '点赞' }}</span>
              <span class="num">{{ talent?.likeCount || 0 }}</span>
            </button>
          </div>
        </div>
        <div class="shine"></div>
      </header>

      <p v-if="error" class="error">{{ error }}</p>

      <section v-if="identityTags.length || industryTags.length" class="sec">
        <div class="slab">IDENTITY · INDUSTRY</div>
        <h2>身份与行业</h2>
        <div class="sec-body">
          <div v-if="identityTags.length" class="dotlist">
            <template v-for="(item, index) in identityTags" :key="item">
              <span v-if="index" class="dotsep">·</span>{{ item }}
            </template>
          </div>
          <div v-if="industryTags.length" class="dotlist industry">
            <template v-for="(item, index) in industryTags" :key="item">
              <span v-if="index" class="dotsep">·</span>{{ item }}
            </template>
          </div>
        </div>
      </section>

      <section v-if="profile.roles.length" class="sec">
        <div class="slab">ROLES</div>
        <h2>当前角色</h2>
        <div class="sec-body kv">
          <article v-for="(item, index) in profile.roles" :key="index" class="item">
            <h3>
              <span v-if="item.org" class="org">{{ item.org }}</span>
              <span v-if="item.org && item.title">&nbsp;</span>
              <span v-if="item.title" class="tt">{{ item.title }}</span>
            </h3>
            <p v-if="item.description">{{ item.description }}</p>
          </article>
        </div>
      </section>

      <section v-if="profile.experiences.length" class="sec">
        <div class="slab">EXPERIENCE</div>
        <h2>履历</h2>
        <div class="sec-body kv">
          <article v-for="(item, index) in profile.experiences" :key="index" class="item">
            <h3><span v-if="item.title" class="org">{{ item.title }}</span></h3>
            <p v-if="item.description">{{ item.description }}</p>
          </article>
        </div>
      </section>

      <section v-if="profile.services.length" class="sec">
        <div class="slab">SERVICES</div>
        <h2>服务内容</h2>
        <div class="sec-body kv">
          <article v-for="(item, index) in profile.services" :key="index" class="item">
            <h3><span v-if="item.title" class="org">{{ item.title }}</span></h3>
            <p v-if="item.description">{{ item.description }}</p>
          </article>
        </div>
      </section>

      <section v-if="profile.collaborations.length" class="sec">
        <div class="slab">COLLABORATION</div>
        <h2>期望合作</h2>
        <div class="sec-body kv">
          <article v-for="(item, index) in profile.collaborations" :key="index" class="item">
            <h3><span v-if="item.title" class="org">{{ item.title }}</span></h3>
            <p v-if="item.description">{{ item.description }}</p>
          </article>
        </div>
      </section>

      <section v-if="profile.cases.length" class="sec">
        <div class="slab">CASES · 行业 / 画像 / 方案 / 交付</div>
        <h2>代表案例</h2>
        <div class="sec-body">
          <article v-for="(item, index) in profile.cases" :key="index" class="case">
            <div v-if="item.industry" class="ind">{{ item.industry }}</div>
            <h3 v-if="item.title">{{ item.title }}</h3>
            <p v-if="item.portrait" class="portrait">{{ item.portrait }}</p>
            <ul v-if="item.points && item.points.length">
              <li v-for="point in item.points" :key="point">{{ point }}</li>
            </ul>
            <template v-if="item.delivery">
              <div class="clabel">DELIVERY</div>
              <p class="delivery"><b>交付结果：</b>{{ item.delivery }}</p>
            </template>
          </article>
        </div>
      </section>

      <section v-if="contacts.length" ref="contactRef" class="contact-bar">
        <span class="t">合作洽谈</span>
        <span v-for="item in barContacts" :key="item.label" class="c">
          {{ item.label }}
          <a v-if="item.href" :href="item.href">
            <span v-if="item.label === '网站'" class="gold">↗</span>
            <b>{{ item.value }}</b>
          </a>
          <b v-else>{{ item.value }}</b>
        </span>
      </section>
    </article>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { fetchTalentBySlug, toggleTalentLike } from '../api/talent'
import { visitorKey } from '../utils/host'

const props = defineProps({
  slug: { type: String, required: true },
})

const loading = ref(true)
const error = ref('')
const talent = ref(null)
const heroRef = ref(null)
const contactRef = ref(null)

const profile = computed(() => talent.value?.profile || {
  badges: [],
  identityTags: [],
  industryTags: [],
  roles: [],
  experiences: [],
  services: [],
  collaborations: [],
  cases: [],
})
const badges = computed(() => profile.value.badges || [])
const identityTags = computed(() => profile.value.identityTags || [])
const industryTags = computed(() => profile.value.industryTags || [])
const initial = computed(() => (talent.value?.name || 'F').slice(0, 1))
const contacts = computed(() => {
  const person = talent.value
  if (!person) return []
  return [
    person.phone ? { label: '手机', value: person.phone, href: `tel:${person.phone}` } : null,
    person.wechat ? { label: '微信', value: person.wechat } : null,
    person.website ? { label: '网站', value: person.websiteLabel || person.website, href: person.website } : null,
    person.location ? { label: '所在', value: person.location } : null,
  ].filter(Boolean)
})
const barContacts = computed(() => {
  const channels = contacts.value.filter((item) => item.label !== '所在')
  return channels.length ? channels : contacts.value
})

function moveShine(event) {
  const hero = heroRef.value
  if (!hero) return
  const rect = hero.getBoundingClientRect()
  hero.style.setProperty('--mx', `${event.clientX - rect.left}px`)
  hero.style.setProperty('--my', `${event.clientY - rect.top}px`)
}

function scrollToContact() {
  contactRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function load(slug) {
  loading.value = true
  error.value = ''
  talent.value = null
  try {
    talent.value = await fetchTalentBySlug(slug, visitorKey())
    document.title = `${talent.value.name} · FDELab`
  } catch (err) {
    error.value = err.message || '主页加载失败'
  } finally {
    loading.value = false
  }
}

async function like() {
  if (!talent.value) return
  try {
    const result = await toggleTalentLike(talent.value.id, visitorKey())
    talent.value.liked = result.liked
    talent.value.likeCount = result.likeCount
  } catch {
    // 无接口时保留演示交互态
    talent.value.liked = !talent.value.liked
    talent.value.likeCount = Math.max(0, (Number(talent.value.likeCount) || 0) + (talent.value.liked ? 1 : -1))
  }
}

watch(() => props.slug, load, { immediate: true })
</script>

<style scoped>
.talent-detail {
  --ink: #171c2e;
  --ink-soft: #2a3045;
  --muted: #66708a;
  --paper: #f5f7fb;
  --card: #ffffff;
  --line: #e8ecf4;
  --accent: #2e6bff;
  --accent-deep: #1f56d6;
  --blue-soft: #eef3ff;
  --blue-line: #dbe3f2;
  --shadow: 0 1px 2px rgba(23, 28, 46, .04), 0 26px 52px -26px rgba(80, 110, 200, .28);
  --shadow-hi: 0 1px 2px rgba(23, 28, 46, .05), 0 36px 68px -30px rgba(80, 110, 200, .42);
  --serif: "Iowan Old Style", Palatino, "Songti SC", serif;
  min-height: 100vh;
  background:
    radial-gradient(ellipse 46% 34% at 18% -4%, #e3ecfc 0%, transparent 62%),
    radial-gradient(ellipse 42% 30% at 86% 4%, #ece7fc 0%, transparent 60%),
    var(--paper);
  color: var(--ink);
  font-family: "Avenir Next", "PingFang SC", "Hiragino Sans GB", "Noto Sans SC", sans-serif;
  -webkit-font-smoothing: antialiased;
}

.profile {
  max-width: 880px;
  margin: 0 auto;
  padding: 30px 5vw 90px;
  position: relative;
  z-index: 1;
}

.topbar {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 26px;
}

.brandmark {
  font-family: var(--serif);
  font-size: 18px;
  letter-spacing: .05em;
  color: var(--muted);
}

.back {
  font-size: 12.5px;
  color: var(--muted);
  text-decoration: none;
  letter-spacing: .04em;
  transition: color .2s ease;
}

.back:hover {
  color: var(--accent);
}

.hero {
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, .92);
  border: 1px solid rgba(232, 236, 244, .9);
  border-radius: 18px;
  padding: 38px 40px 30px;
  box-shadow: var(--shadow);
  transition: box-shadow .35s ease;
}

.hero:hover {
  box-shadow: var(--shadow-hi);
}

.hero::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(46, 107, 255, .045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(46, 107, 255, .045) 1px, transparent 1px);
  background-size: 26px 26px;
  background-position: center;
  -webkit-mask-image: radial-gradient(ellipse 86% 72% at 24% 18%, #000 8%, transparent 76%);
  mask-image: radial-gradient(ellipse 86% 72% at 24% 18%, #000 8%, transparent 76%);
}

.hero::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background:
    radial-gradient(ellipse 44% 40% at 92% 100%, rgba(147, 169, 255, .28), transparent 66%),
    radial-gradient(ellipse 38% 32% at 98% 16%, rgba(193, 207, 255, .3), transparent 62%),
    linear-gradient(168deg, rgba(255, 255, 255, 0) 46%, rgba(224, 232, 252, .6) 100%);
}

.hero .shine {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background: radial-gradient(circle 220px at var(--mx, 50%) var(--my, 30%),
    rgba(255, 255, 255, .95) 0%, rgba(130, 168, 255, .6) 26%, transparent 64%);
  mix-blend-mode: soft-light;
  opacity: 0;
  transition: opacity .4s ease;
}

.hero:hover .shine {
  opacity: .85;
}

.hero > * {
  position: relative;
  z-index: 1;
}

.head-row {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.chip {
  flex: 0 0 auto;
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(140deg, #4d84ff, #2e6bff);
  box-shadow: 0 12px 26px -10px rgba(46, 107, 255, .6), inset 0 1px 0 rgba(255, 255, 255, .35);
}

.chip span {
  font-family: var(--serif);
  font-size: 28px;
  color: #fff;
  line-height: 1;
}

.head-main {
  min-width: 0;
}

.mark {
  font-size: 9.5px;
  letter-spacing: .26em;
  color: var(--accent);
}

.hero-name {
  font-family: var(--serif);
  font-size: 46px;
  font-weight: 500;
  margin: 10px 0 4px;
  letter-spacing: .02em;
  line-height: 1.08;
}

.headline {
  font-size: 14.5px;
  color: var(--accent);
  margin: 6px 0 0;
  line-height: 1.6;
}

.summary {
  margin: 12px 0 0;
  color: var(--muted);
  font-size: 13.5px;
  line-height: 1.8;
  max-width: 620px;
}

.badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 20px;
}

.badge {
  font-size: 11.5px;
  letter-spacing: .04em;
  color: #fff;
  background: linear-gradient(140deg, #4d84ff, #2e6bff);
  border-radius: 999px;
  padding: 6px 14px;
  box-shadow: 0 10px 20px -12px rgba(46, 107, 255, .8);
}

.hero-foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 18px;
  margin-top: 26px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
}

.loc {
  font-size: 12.5px;
  color: var(--muted);
  letter-spacing: .04em;
}

.hero-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn {
  border: 0;
  background: none;
  padding: 0;
  color: var(--muted);
  font-size: 12.5px;
  cursor: pointer;
  font-family: inherit;
  letter-spacing: .02em;
  transition: color .18s ease;
}

.btn.contact {
  border: 1px solid var(--blue-line);
  padding: 9px 18px;
  border-radius: 999px;
  color: var(--ink);
  background: rgba(255, 255, 255, .7);
  transition: all .2s ease;
}

.btn.contact:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: #fff;
}

.btn.solid {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  background: var(--accent);
  color: #fff;
  font-weight: 500;
  padding: 9px 18px;
  border-radius: 999px;
  box-shadow: 0 10px 22px -10px rgba(46, 107, 255, .75);
  transition: all .2s ease;
}

.btn.solid:hover {
  background: var(--accent-deep);
  color: #fff;
  transform: translateY(-1px);
}

.btn.solid .num {
  font-variant-numeric: tabular-nums;
  opacity: .85;
}

.btn.solid.liked {
  background: var(--blue-soft);
  color: var(--accent);
  box-shadow: none;
}

.sec {
  margin-top: 42px;
}

.sec > .slab {
  font-size: 9.5px;
  letter-spacing: .24em;
  color: var(--accent);
}

.sec > h2 {
  font-family: var(--serif);
  font-size: 22px;
  font-weight: 500;
  margin: 8px 0 6px;
  letter-spacing: .01em;
}

.sec-body {
  border-top: 1px solid var(--line);
  margin-top: 14px;
  padding-top: 20px;
}

.dotlist {
  font-size: 13px;
  color: #4a5578;
  line-height: 2.1;
}

.dotlist.industry {
  margin-top: 2px;
  color: #3d5f9e;
}

.dotsep {
  color: #a9bcea;
  margin: 0 10px;
}

.kv {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 16px;
}

.kv .item {
  background: #f7f9fd;
  border: 1px solid rgba(232, 236, 244, .9);
  border-radius: 12px;
  padding: 16px 18px;
}

.kv .item h3 {
  margin: 0 0 5px;
  font-size: 14.5px;
  font-weight: 500;
}

.kv .item h3 .org {
  color: var(--accent);
}

.kv .item h3 .tt {
  color: var(--ink);
}

.kv .item p {
  margin: 0;
  font-size: 12.8px;
  color: var(--muted);
  line-height: 1.7;
}

.case {
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, .92);
  border: 1px solid rgba(232, 236, 244, .9);
  border-radius: 16px;
  padding: 24px 26px 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(23, 28, 46, .035), 0 18px 40px -30px rgba(80, 110, 200, .34);
}

.case::before {
  content: "";
  position: absolute;
  left: 0;
  top: 20px;
  bottom: 20px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(#4d84ff, rgba(77, 132, 255, .12));
}

.case .ind {
  font-size: 11px;
  color: var(--accent);
  letter-spacing: .06em;
}

.case h3 {
  font-family: var(--serif);
  font-size: 19px;
  font-weight: 500;
  margin: 8px 0 11px;
  line-height: 1.45;
}

.case .portrait {
  font-size: 12.8px;
  color: #4a5578;
  line-height: 1.75;
  padding-bottom: 11px;
  border-bottom: 1px dashed var(--line);
}

.case ul {
  margin: 13px 0;
  padding-left: 18px;
  font-size: 13px;
  color: #3c4560;
  line-height: 1.85;
}

.case ul li::marker {
  color: #8fabee;
}

.case .clabel {
  font-size: 9.5px;
  color: var(--accent);
  letter-spacing: .22em;
  margin-top: 14px;
}

.case .delivery {
  font-size: 12.8px;
  color: var(--muted);
  line-height: 1.75;
  background: var(--blue-soft);
  border-radius: 10px;
  padding: 11px 14px;
  margin: 7px 0 0;
}

.case .delivery b {
  color: var(--ink);
  font-weight: 500;
}

.contact-bar {
  margin-top: 48px;
  border-radius: 16px;
  padding: 22px 26px;
  background: linear-gradient(120deg, #1b2440, #171c2e 62%);
  color: #fff;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 28px;
  box-shadow: 0 26px 50px -30px rgba(23, 28, 46, .6);
}

.contact-bar .t {
  font-family: var(--serif);
  font-size: 17px;
  letter-spacing: .02em;
}

.contact-bar .c {
  font-size: 12.5px;
  color: rgba(255, 255, 255, .6);
  letter-spacing: .04em;
}

.contact-bar .c b {
  color: #fff;
  font-weight: 500;
  margin-left: 6px;
}

.contact-bar .c a {
  text-decoration: none;
}

.contact-bar .gold {
  color: #7ea6ff;
}

.error {
  color: #c0392b;
}

@media (max-width: 900px) {
  .hero {
    padding: 30px 26px 24px;
  }

  .hero-name {
    font-size: 36px;
  }
}

@media (max-width: 640px) {
  .kv {
    grid-template-columns: 1fr;
  }

  .chip {
    width: 50px;
    height: 50px;
    border-radius: 13px;
  }

  .chip span {
    font-size: 23px;
  }

  .hero-name {
    font-size: 30px;
  }

  .case {
    padding: 20px 20px 16px;
  }
}
</style>
