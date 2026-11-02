<template>
  <article class="profile" v-loading="loading">
    <header class="hero">
      <p class="domain">{{ talent?.slug || slug }}.fdelab.io</p>
      <h1>{{ talent?.name || 'FDE' }}</h1>
      <p class="headline">{{ talent?.headline }}</p>
      <div v-if="badges.length" class="badges">
        <span v-for="item in badges" :key="item">{{ item }}</span>
      </div>
      <button class="like" type="button" @click="like">{{ talent?.liked ? '已赞' : '点赞' }} {{ talent?.likeCount || 0 }}</button>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <section v-if="contacts.length" class="panel contacts">
      <p v-for="item in contacts" :key="item.label">
        <span>{{ item.label }}</span>
        <a v-if="item.href" :href="item.href">{{ item.value }}</a>
        <em v-else>{{ item.value }}</em>
      </p>
    </section>

    <section v-if="identityTags.length || industryTags.length" class="panel tags">
      <div v-if="identityTags.length">
        <span v-for="item in identityTags" :key="item">{{ item }}</span>
      </div>
      <div v-if="industryTags.length" class="muted">
        <span v-for="item in industryTags" :key="item">{{ item }}</span>
      </div>
    </section>

    <section v-if="profile.roles.length" class="block">
      <h2>主要身份</h2>
      <div class="stack">
        <article v-for="(item, index) in profile.roles" :key="index">
          <p class="org">{{ item.org }}</p>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>

    <section v-if="profile.experiences.length" class="block">
      <h2>过往经历</h2>
      <div class="stack">
        <article v-for="(item, index) in profile.experiences" :key="index">
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>

    <section v-if="profile.services.length" class="block">
      <h2>提供的服务</h2>
      <ol class="services">
        <li v-for="(item, index) in profile.services" :key="index">
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </li>
      </ol>
    </section>

    <section v-if="profile.collaborations.length" class="block">
      <h2>正在寻找的合作</h2>
      <div class="stack">
        <article v-for="(item, index) in profile.collaborations" :key="index">
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>

    <section v-if="profile.cases.length" class="block">
      <h2>FDE 实战案例</h2>
      <article v-for="(item, index) in profile.cases" :key="index" class="case">
        <p class="industry">{{ item.industry }}</p>
        <h3>{{ item.title }}</h3>
        <p class="portrait">{{ item.portrait }}</p>
        <ul>
          <li v-for="point in item.points || []" :key="point">{{ point }}</li>
        </ul>
        <p class="delivery">交付与进展：{{ item.delivery }}</p>
      </article>
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
  padding: 48px 24px 80px;
}

.domain {
  margin: 0;
  color: var(--gold, #c4a574);
  letter-spacing: 0.08em;
}

h1 {
  margin: 8px 0 0;
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: clamp(40px, 6vw, 68px);
  font-weight: 500;
}

.headline {
  margin-top: 10px;
  color: var(--muted, #5e6a63);
  font-size: 18px;
}

.badges,
.tags span,
.like {
  display: inline-flex;
  margin: 8px 8px 0 0;
  padding: 6px 10px;
  border-radius: 999px;
  background: #17241e;
  color: #f4f1ea;
  font-size: 13px;
}

.like {
  border: 0;
  cursor: pointer;
  background: #1f6b4a;
}

.panel,
.stack article,
.case {
  margin-top: 18px;
  padding: 20px;
  border: 1px solid var(--line, #e3dccf);
  border-radius: 16px;
  background: var(--card, #fffdf8);
}

.contacts p,
.stack p,
.services p,
.portrait,
.delivery {
  margin: 6px 0 0;
  line-height: 1.7;
}

.contacts span {
  display: inline-block;
  width: 48px;
  color: var(--muted, #5e6a63);
}

.tags span {
  background: #efe8db;
  color: #17241e;
}

.tags .muted span {
  background: transparent;
  border: 1px solid var(--line, #e3dccf);
}

.block h2,
.case h3,
.stack h3,
.services h3 {
  margin: 0;
}

.block {
  margin-top: 36px;
}

.block h2 {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 32px;
  font-weight: 500;
}

.org,
.industry {
  margin: 0;
  color: #1f6b4a;
  font-size: 13px;
}

.services {
  list-style: none;
  padding: 0;
  counter-reset: service;
}

.services li {
  margin-top: 14px;
  padding-left: 48px;
  position: relative;
}

.services li::before {
  counter-increment: service;
  content: counter(service, decimal-leading-zero);
  position: absolute;
  left: 0;
  top: 0;
  color: #c4a574;
  font-weight: 600;
}

.case + .case {
  margin-top: 14px;
}

.error {
  color: #8a5a2a;
}
</style>
