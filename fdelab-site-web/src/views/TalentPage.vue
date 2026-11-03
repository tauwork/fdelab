<template>
  <article class="profile" v-loading="loading">
    <div class="topbar">
      <span class="brandmark">FDELab</span>
      <router-link class="back" to="/talents">← 返回人才清单</router-link>
    </div>

    <header ref="heroRef" class="hero" @mousemove="moveShine">
      <div class="mark">FDE TALENT</div>
      <h1 class="hero-name">{{ talent?.name || 'FDE' }}</h1>
      <p v-if="talent?.headline" class="headline">{{ talent.headline }}</p>
      <p v-if="talent?.summary" class="summary">{{ talent.summary }}</p>
      <div v-if="badges.length" class="badges">
        <span v-for="item in badges" :key="item" class="badge">{{ item }}</span>
      </div>
      <div class="hero-foot">
        <span v-if="talent?.location" class="loc">{{ talent.location }}</span>
        <div class="hero-actions">
          <button v-if="contacts.length" class="btn contact" type="button" @click="scrollToContact">联系 TA</button>
          <button class="btn solid like" type="button" @click="like">
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
  } catch (err) {
    error.value = err.message || '点赞失败'
  }
}

watch(() => props.slug, load, { immediate: true })
</script>

<style scoped>
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
  margin-bottom: 30px;
}

.brandmark {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 20px;
  letter-spacing: 0.05em;
}

.back {
  font-size: 12.5px;
  color: var(--muted);
  text-decoration: none;
  letter-spacing: 0.04em;
  transition: color 0.2s ease;
}

.back:hover {
  color: var(--accent);
}

.hero {
  position: relative;
  background-color: #fffdf8;
  background-image:
    radial-gradient(ellipse 64% 50% at 14% 6%, rgba(31, 107, 74, 0.08), transparent 62%),
    radial-gradient(ellipse 58% 46% at 92% 96%, rgba(196, 165, 116, 0.16), transparent 62%),
    linear-gradient(168deg, #fffdf8, #fcf8f0);
  border: 0;
  border-radius: 12px;
  padding: 40px 42px 32px;
  box-shadow:
    0 1px 2px rgba(23, 36, 30, 0.05),
    0 20px 44px -28px rgba(23, 36, 30, 0.32),
    0 1px 0 rgba(255, 255, 255, 0.9) inset;
}

.hero::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(31, 107, 74, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(31, 107, 74, 0.045) 1px, transparent 1px);
  background-size: 24px 24px;
  background-position: center;
  -webkit-mask-image: radial-gradient(ellipse 88% 74% at 50% 30%, #000 10%, transparent 78%);
  mask-image: radial-gradient(ellipse 88% 74% at 50% 30%, #000 10%, transparent 78%);
}

.hero .shine {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background: radial-gradient(circle 180px at var(--mx, 50%) var(--my, 40%),
    rgba(255, 255, 255, 0.85) 0%, rgba(196, 165, 116, 0.6) 22%, transparent 62%);
  mix-blend-mode: soft-light;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.hero:hover .shine {
  opacity: 0.9;
}

.hero > * {
  position: relative;
}

.mark {
  font-size: 9.5px;
  letter-spacing: 0.24em;
  color: var(--gold);
}

.hero-name {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 46px;
  font-weight: 500;
  margin: 16px 0 4px;
  letter-spacing: 0.02em;
  line-height: 1.1;
}

.headline {
  font-size: 14.5px;
  color: var(--accent);
  margin-top: 6px;
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
  letter-spacing: 0.04em;
  color: var(--paper);
  background: var(--accent);
  border-radius: 999px;
  padding: 6px 14px;
  box-shadow: 0 8px 18px -10px rgba(31, 107, 74, 0.55);
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
  letter-spacing: 0.04em;
}

.hero-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 14px;
}

.btn {
  border: 0;
  background: none;
  padding: 0;
  color: var(--muted);
  font-size: 12.5px;
  cursor: pointer;
  font-family: inherit;
  letter-spacing: 0.02em;
  transition: color 0.18s ease;
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

.btn.contact {
  border: 1px solid var(--line);
  padding: 9px 18px;
  border-radius: 999px;
  color: var(--ink);
  background: rgba(255, 255, 255, 0.5);
}

.btn.contact:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: rgba(255, 255, 255, 0.8);
}

.like {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.like .num {
  font-variant-numeric: tabular-nums;
}

.sec {
  margin-top: 44px;
}

.sec > h2 {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 22px;
  font-weight: 500;
  margin: 0 0 6px;
  letter-spacing: 0.01em;
}

.sec > .slab {
  font-size: 9.5px;
  letter-spacing: 0.22em;
  color: var(--gold);
}

.sec-body {
  border-top: 1px solid var(--line);
  margin-top: 14px;
  padding-top: 18px;
}

.dotlist {
  font-size: 13px;
  color: #6d786f;
  line-height: 2.1;
}

.dotlist.industry {
  margin-top: 2px;
  color: #8a7a5e;
}

.dotsep {
  color: var(--gold);
  margin: 0 10px;
  opacity: 0.75;
}

.kv {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px 32px;
}

.kv .item h3 {
  margin: 0 0 4px;
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

@media (max-width: 640px) {
  .kv {
    grid-template-columns: 1fr;
  }
}

.case {
  position: relative;
  background-color: #fffdf8;
  background-image:
    radial-gradient(ellipse 60% 55% at 16% 10%, rgba(31, 107, 74, 0.06), transparent 62%),
    linear-gradient(168deg, #fffdf8, #fcf8f0);
  border: 0;
  border-radius: 12px;
  padding: 24px 26px 20px;
  margin-bottom: 18px;
  box-shadow:
    0 1px 2px rgba(23, 36, 30, 0.045),
    0 14px 30px -26px rgba(23, 36, 30, 0.28),
    0 1px 0 rgba(255, 255, 255, 0.9) inset;
}

.case::before {
  content: "";
  position: absolute;
  left: 0;
  top: 18px;
  bottom: 18px;
  width: 2px;
  background: linear-gradient(rgba(31, 107, 74, 0.35), rgba(31, 107, 74, 0.05));
  border-radius: 2px;
}

.case .ind {
  font-size: 11px;
  color: var(--accent);
  letter-spacing: 0.06em;
}

.case h3 {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 19px;
  font-weight: 500;
  margin: 8px 0 10px;
  line-height: 1.45;
}

.case .portrait {
  font-size: 12.8px;
  color: #6d786f;
  line-height: 1.75;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--line);
}

.case ul {
  margin: 12px 0;
  padding-left: 18px;
  font-size: 13px;
  color: #46524c;
  line-height: 1.85;
}

.case .delivery {
  font-size: 12.8px;
  color: var(--muted);
  line-height: 1.75;
}

.case .delivery b {
  color: var(--ink);
  font-weight: 500;
}

.case .clabel {
  font-size: 9.5px;
  color: var(--gold);
  letter-spacing: 0.2em;
}

.contact-bar {
  margin-top: 48px;
  border-radius: 12px;
  padding: 22px 26px;
  background: var(--ink);
  color: var(--paper);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px 28px;
  box-shadow: 0 20px 44px -28px rgba(23, 36, 30, 0.5);
}

.contact-bar .t {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 17px;
  letter-spacing: 0.02em;
}

.contact-bar .c {
  font-size: 12.5px;
  color: rgba(244, 241, 234, 0.72);
  letter-spacing: 0.04em;
}

.contact-bar .c b {
  color: var(--paper);
  font-weight: 500;
}

.contact-bar .gold {
  color: var(--gold);
}

.error {
  color: #8a7a5e;
}
</style>
