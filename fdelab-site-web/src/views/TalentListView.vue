<template>
  <section class="directory">
    <h1>人才</h1>
    <p class="lead">按点赞从高到低。每位 FDE 有自己的子域名主页。</p>
    <p v-if="error" class="error">{{ error }}</p>
    <article v-for="item in records" :key="item.id" class="row">
      <router-link :to="`/t/${item.slug}`">
        <strong>{{ item.name }}</strong>
        <span>{{ item.headline }}</span>
        <em>{{ item.location }}</em>
      </router-link>
      <button type="button" @click="like(item)">{{ item.liked ? '已赞' : '点赞' }} {{ item.likeCount }}</button>
    </article>
    <p v-if="!loading && records.length === 0" class="error">还没有发布的人才。</p>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchTalents, toggleTalentLike } from '../api/talent'
import { visitorKey } from '../utils/host'

const records = ref([])
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  try {
    const data = await fetchTalents({ page: 1, size: 50, visitorKey: visitorKey() })
    records.value = data.records || []
  } catch (err) {
    error.value = err.message || '人才加载失败'
  } finally {
    loading.value = false
  }
}

async function like(item) {
  try {
    const result = await toggleTalentLike(item.id, visitorKey())
    item.liked = result.liked
    item.likeCount = result.likeCount
    records.value = [...records.value].sort((a, b) => b.likeCount - a.likeCount || a.id - b.id)
  } catch (err) {
    error.value = err.message || '点赞失败'
  }
}

onMounted(load)
</script>

<style scoped>
.directory {
  padding: 48px 7vw 80px;
}

h1 {
  margin: 0;
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 48px;
  font-weight: 500;
}

.lead,
.error {
  color: var(--muted);
}

.row {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  margin-top: 14px;
  padding: 18px 20px;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--card);
}

.row a {
  display: grid;
  gap: 4px;
}

.row span,
.row em {
  color: var(--muted);
  font-style: normal;
}

button {
  border: 0;
  border-radius: 999px;
  padding: 8px 12px;
  background: #1f6b4a;
  color: #f4f1ea;
  cursor: pointer;
}
</style>
