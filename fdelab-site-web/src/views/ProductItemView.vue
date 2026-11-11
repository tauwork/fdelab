<template>
  <div class="product-page">
    <div class="wrap crumb">
      <RouterLink to="/products">← 返回产品与服务</RouterLink>
    </div>

    <div v-if="item" class="wrap fhead">
      <div class="i-top">
        <span class="kind" :class="item.kind">{{ item.kindLabel }}</span>
        <span class="pill" :class="STATUS_TONE[item.status] || 'grey'">{{ item.statusLabel }}</span>
      </div>
      <h1>{{ item.name }}</h1>
      <p class="lead">{{ item.summary }}</p>
      <!-- TODO(文案)：条目详情页版式未定，这里只放一行中性提示 -->
      <p class="fineprint">详细内容整理中</p>
    </div>

    <div v-else class="wrap fhead">
      <p v-if="error" class="lead">{{ error }}</p>
      <!-- TODO(文案)：加载中 / 空态文案待定 -->
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getItem } from '../api/product'

const STATUS_TONE = { on: 'ok', beta: 'warn', dev: 'grey' }

const route = useRoute()
const item = ref(null)
const error = ref('')

async function load(code) {
  error.value = ''
  item.value = null
  try {
    item.value = await getItem(code)
  } catch (err) {
    error.value = err?.message || '加载失败'
  }
}

watch(
  () => route.params.code,
  (code) => {
    if (code) load(code)
  },
)

onMounted(() => {
  document.title = '产品与服务 · FDE 智研社'
  if (route.params.code) load(route.params.code)
})
</script>
