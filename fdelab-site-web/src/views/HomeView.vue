<template>
  <div>
    <section class="hero">
      <p class="eyebrow">{{ profile?.name || 'FDELab' }}</p>
      <h1>{{ profile?.slogan || '实验、内容与业务的统一入口' }}</h1>
      <p class="intro">{{ profile?.intro || '正在连接内容服务。' }}</p>
    </section>

    <section id="about" class="channels">
      <article>
        <h2>小程序</h2>
        <p>面向微信里的日常使用，读取同一组公开接口。</p>
      </article>
      <article>
        <h2>展示站</h2>
        <p>对外介绍实验室和动态，不处理登录。</p>
      </article>
      <article>
        <h2>管理后台</h2>
        <p>登录后查看概览和用户，业务菜单后续往这里加。</p>
      </article>
    </section>

    <section id="latest" class="latest">
      <div class="section-title">
        <h2>动态</h2>
        <span v-if="loading">加载中</span>
      </div>
      <p v-if="error" class="error">{{ error }}。先启动 fdelab-server，并确认数据库已初始化。</p>
      <div v-else class="list">
        <router-link v-for="item in articles" :key="item.id" class="card" :to="`/articles/${item.id}`">
          <time>{{ item.createTime }}</time>
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary }}</p>
        </router-link>
        <p v-if="!loading && articles.length === 0" class="error">还没有已发布的动态。</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchArticles, fetchProfile } from '../api/site'

const profile = ref(null)
const articles = ref([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const [site, list] = await Promise.all([fetchProfile(), fetchArticles()])
    profile.value = site
    articles.value = list || []
  } catch (err) {
    error.value = err.message || '暂时连不上内容服务'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero {
  padding: 72px 7vw 36px;
}

.eyebrow {
  margin: 0;
  color: var(--gold);
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

h1 {
  max-width: 760px;
  margin: 14px 0 0;
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: clamp(36px, 5vw, 64px);
  font-weight: 500;
  line-height: 1.15;
}

.intro {
  max-width: 620px;
  margin-top: 18px;
  color: var(--muted);
  line-height: 1.8;
}

.channels {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 12px 7vw 48px;
}

.channels article,
.card {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 22px;
}

.channels h2,
.card h3 {
  margin: 0 0 8px;
  font-size: 20px;
}

.channels p,
.card p {
  margin: 0;
  color: var(--muted);
  line-height: 1.7;
}

.latest {
  padding: 0 7vw 64px;
}

.section-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.section-title h2 {
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 32px;
  font-weight: 500;
}

.list {
  display: grid;
  gap: 14px;
}

.card time {
  color: var(--accent);
  font-size: 13px;
}

.error {
  color: #8a5a2a;
}

@media (max-width: 800px) {
  .channels {
    grid-template-columns: 1fr;
  }
}
</style>
