<template>
  <div class="product-page">
    <div class="wrap hero">
      <h1 class="hl">产品即拿即用，服务陪跑到底</h1>
      <div class="hl-sub">
        各行标准品，拿走就能部署。下半是服务；四种服务运作按投入由轻到重，把产品接进你的生产、陪跑到出结果。
      </div>
    </div>

    <div v-if="error" class="wrap errbar">
      <span>{{ error }}</span>
      <button type="button" @click="load">重试</button>
    </div>

    <ProductTabBar :active="tab" :items="tabs" @change="switchTab" />

    <div v-show="tab === 'products'" id="products" class="panel">
      <div class="wrap sec">
        <ProductFilterBar
          :active="chip"
          :total="items.length"
          :counts="kindCounts"
          @change="selectChip"
        />

        <div v-if="brief" class="formbar">
          <div class="frow">
            <span class="fname">{{ brief.name }}</span>
            <span class="fbrief">{{ brief.text }}</span>
          </div>
        </div>

        <div v-if="loading" class="items" aria-busy="true">
          <div v-for="n in 6" :key="n" class="icard skel">
            <div class="bar w40"></div>
            <div class="bar w80"></div>
            <div class="bar w90"></div>
            <div class="bar w60"></div>
          </div>
        </div>
        <div v-else-if="visibleSkus.length" class="items">
          <ProductCard v-for="item in visibleSkus" :key="`${chip}-${item.code}`" :item="item" />
        </div>
        <div v-else class="state empty">
          <!-- TODO(文案)：空态文案待定，不在代码里自拟 -->
        </div>

        <div class="leadout">
          <div class="lo-k">产品上线只是开始</div>
          <div class="lo-t">
            谁把它接进生产流程？上线后的指标谁在盯？模型升级、规则变更，谁在它失效前动手？——这三个问题，没有标准品能回答。
          </div>
          <button class="lo-go" type="button" @click="switchTab('services', true)">看四种服务运作 →</button>
        </div>
      </div>
    </div>

    <div v-show="tab === 'services'" id="services" class="panel panel-services">
      <div class="wrap sec">
        <StatusCell v-if="forms.length" :forms="forms" @jump="jumpToForm" />

        <div v-if="loading" class="svcgrid" aria-busy="true">
          <div v-for="n in 4" :key="n" class="scard skel">
            <div class="bar w40"></div>
            <div class="bar w70"></div>
            <div class="bar w90"></div>
            <div class="bar w50"></div>
          </div>
        </div>
        <div v-else-if="forms.length" class="svcgrid">
          <ServiceFormCard
            v-for="form in forms"
            :key="form.code"
            :form="form"
            :flash="flashCode === form.code"
          />
        </div>
        <div v-else class="state empty">
          <!-- TODO(文案)：空态文案待定，不在代码里自拟 -->
        </div>

        <div class="sec divided">
          <h2 class="sec-h small">收费模式红线</h2>
          <div class="warn">
            <b>人天模式在 AI 时代有个致命悖论：AI 让交付变快 → 人天变少 → 收入下降，越高效越亏钱。</b><br>
            四种产品运作一律按「结果 + 资产」计价：交付物计价、固定范围 + 结果奖金、里程碑 + 能力订阅。参考路径：人工费（共创客户）→ 混合计价 → token / 订阅（规模化客户）。
          </div>
        </div>

        <div class="sec divided">
          <h2 class="sec-h small">下一步</h2>
          <div class="next">
            <a class="ncard">
              <div class="st">STEP 1</div>
              <b>看参照案例</b>
              <p>同场景的真实交付与硬指标。</p>
            </a>
            <a class="ncard">
              <div class="st">STEP 2</div>
              <b>配 FDE 团队</b>
              <p>按行业选派驻人选（通才 + 领域专家混编）。</p>
            </a>
            <a class="ncard">
              <div class="st">STEP 3</div>
              <b>AI 生成售前方案</b>
              <p>输入客户信息，当场出结构化打法。</p>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useProducts } from '../composables/useProducts'
import ProductTabBar from '../components/product/ProductTabBar.vue'
import ProductFilterBar from '../components/product/ProductFilterBar.vue'
import ProductCard from '../components/product/ProductCard.vue'
import ServiceFormCard from '../components/product/ServiceFormCard.vue'
import StatusCell from '../components/product/StatusCell.vue'

const TAB_KEYS = ['products', 'services']

// 选中 chip 时的一句话说明（demo PK_BRIEF 原文），属页面文案。
const KIND_BRIEF = {
  de: { name: '数字员工', text: '按你的场景部署、按你的验证器验收，交付的是能上线干活的数字员工。' },
  ag: { name: 'Agent', text: '把一类重复作业整段接管，人只在判定点介入。' },
  sa: { name: 'SaaS', text: '开账号即用，按订阅计费，数据留在你的环境。' },
  m: { name: '方法资产', text: '把我们自己的交付物标准化成可授权的模板与手册，你的团队照着就能交付。' },
}

const route = useRoute()
const { forms, items, loading, error, load } = useProducts()

const tab = ref(initialTab())
const chip = ref('all')
const flashCode = ref('')
let flashTimer = 0

const tabs = computed(() => [
  { key: 'products', label: '产品', count: items.value.length },
  { key: 'services', label: '服务', count: forms.value.length },
])

const kindCounts = computed(() => {
  const counts = {}
  for (const item of items.value) {
    counts[item.kind] = (counts[item.kind] || 0) + 1
  }
  return counts
})

const visibleSkus = computed(() => (
  chip.value === 'all' ? items.value : items.value.filter((item) => item.kind === chip.value)
))

const brief = computed(() => (chip.value === 'all' ? null : KIND_BRIEF[chip.value] || null))

function initialTab() {
  const hash = String(window.location.hash || '').replace('#', '')
  return TAB_KEYS.includes(hash) ? hash : 'products'
}

function switchTab(name, scroll = false) {
  if (!TAB_KEYS.includes(name)) return
  tab.value = name
  if (window.location.hash !== `#${name}`) {
    window.history.replaceState(null, '', `${route.path}#${name}`)
  }
  if (scroll) {
    document.getElementById('tabs')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function selectChip(kind) {
  chip.value = kind
}

function jumpToForm(code) {
  const card = document.getElementById(`svc-${code}`)
  if (!card) return
  card.scrollIntoView({ behavior: 'smooth', block: 'center' })
  flashCode.value = code
  clearTimeout(flashTimer)
  flashTimer = setTimeout(() => {
    flashCode.value = ''
  }, 1600)
}

watch(
  () => route.hash,
  (hash) => {
    const name = String(hash || '').replace('#', '')
    if (TAB_KEYS.includes(name)) tab.value = name
  },
)

onMounted(() => {
  document.title = '产品与服务 · FDE 智研社'
  load()
})

onBeforeUnmount(() => {
  clearTimeout(flashTimer)
})
</script>
