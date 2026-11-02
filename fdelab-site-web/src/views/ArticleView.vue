<template>
  <article class="article">
    <router-link class="back" to="/">返回首页</router-link>
    <p v-if="error" class="error">{{ error }}</p>
    <template v-else-if="article">
      <time>{{ article.createTime }}</time>
      <h1>{{ article.title }}</h1>
      <p class="summary">{{ article.summary }}</p>
      <p class="content">{{ article.content }}</p>
    </template>
    <p v-else>加载中</p>
  </article>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchArticle } from '../api/site'

const route = useRoute()
const article = ref(null)
const error = ref('')

async function load(id) {
  article.value = null
  error.value = ''
  try {
    article.value = await fetchArticle(id)
  } catch (err) {
    error.value = err.message || '内容加载失败'
  }
}

onMounted(() => load(route.params.id))
watch(() => route.params.id, (id) => {
  if (id) load(id)
})
</script>

<style scoped>
.article {
  max-width: 760px;
  margin: 0 auto;
  padding: 48px 24px 80px;
}

.back {
  color: var(--accent);
}

time {
  display: block;
  margin-top: 28px;
  color: var(--muted);
}

h1 {
  margin: 10px 0 0;
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: clamp(32px, 4vw, 48px);
  font-weight: 500;
}

.summary,
.content {
  line-height: 1.8;
}

.summary {
  color: var(--muted);
}

.error {
  color: #8a5a2a;
}
</style>
