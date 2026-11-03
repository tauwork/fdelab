<template>
  <TalentPage v-if="activeSlug" :slug="activeSlug" />
  <router-view v-else-if="bareRoute" />
  <div v-else class="site">
    <header class="top">
      <router-link class="brand" to="/">FDELab</router-link>
      <nav>
        <router-link to="/talents">人才</router-link>
        <router-link to="/#about">关于</router-link>
        <router-link to="/#latest">动态</router-link>
      </nav>
    </header>
    <main>
      <router-view />
    </main>
    <footer>
      <span>FDELab</span>
      <span>展示站</span>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TalentPage from './views/TalentPage.vue'
import { talentSlugFromHost } from './utils/host'

const route = useRoute()
const activeSlug = computed(() => talentSlugFromHost(window.location.hostname) || (route.name === 'talent' ? String(route.params.slug || '') : ''))
const bareRoute = computed(() => route.name === 'talents')
</script>
