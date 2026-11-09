<template>
  <TalentPage v-if="activeSlug" :slug="activeSlug" />
  <router-view v-else-if="bareRoute" />
  <div v-else class="site">
    <SiteNav />
    <main>
      <router-view />
    </main>
    <SiteFooter />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TalentPage from './views/TalentPage.vue'
import SiteNav from './components/SiteNav.vue'
import SiteFooter from './components/SiteFooter.vue'
import { talentSlugFromHost } from './utils/host'

const route = useRoute()
const activeSlug = computed(() => talentSlugFromHost(window.location.hostname) || (route.name === 'talent' ? String(route.params.slug || '') : ''))
const bareRoute = computed(() => route.name === 'talents' || route.name === 'talentsLegacy' || route.name === 'talentDetail')
</script>
