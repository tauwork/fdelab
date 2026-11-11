<template>
  <div class="product-page">
    <div class="wrap crumb">
      <RouterLink to="/products">← 返回产品与服务</RouterLink>
      <template v-if="form"> / <span>{{ form.no }} · {{ form.name }}</span></template>
    </div>

    <div v-if="form" class="wrap fhead">
      <div class="i-top">
        <span class="who">{{ form.label }}</span>
      </div>
      <h1>{{ form.name }}</h1>
      <p class="desc">{{ form.desc }}</p>
    </div>

    <div v-if="form" class="wrap segs">
      <div class="seg wide">
        <div class="k">适合谁</div>
        <div class="b">{{ form.fit }}</div>
      </div>
      <div class="seg wide">
        <div class="k">交付物清单</div>
        <div class="b">
          <ul class="dl">
            <li v-for="item in form.deliverables" :key="item">{{ item }}</li>
          </ul>
        </div>
      </div>
      <div class="seg">
        <div class="k">周期</div>
        <div class="b">{{ form.cycle }}</div>
      </div>
      <div class="seg">
        <div class="k">计价</div>
        <div class="b">{{ form.pricing }}</div>
      </div>
      <div class="seg wide">
        <div class="k">成功判据</div>
        <div class="b">{{ form.success }}</div>
      </div>
      <div class="seg">
        <div class="k">参照案例</div>
        <div class="b">
          <CaseRefCard v-for="ref in form.caseRefs" :key="ref.id" :item="ref" />
          <!-- 占位文案取自 v15：F4 无关联案例时展示 -->
          <template v-if="!form.caseRefs.length">能力线产品，方法论基础见 FDE 三条识别标准。</template>
        </div>
      </div>
      <div class="seg">
        <div class="k">必须避开的坑</div>
        <div class="b" v-html="PIT_TEXT"></div>
      </div>
    </div>

    <div v-if="!form" class="wrap fhead">
      <p v-if="error" class="lead">{{ error }}</p>
      <!-- TODO(文案)：加载中 / 空态文案待定 -->
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getForm } from '../api/product'
import CaseRefCard from '../components/product/CaseRefCard.vue'

// 与 v15 同文（原型 product.html）
const PIT_TEXT = '① <b>把 FDE 当新业务部门卖人头</b>——按人月报价派顾问驻场，天花板低、断单即断粮；② <b>过早抽象</b>——第 2 个案例就抽通用组件，大概率抽象错（OpenAI 先跑 10+ 用例再收敛）；③ <b>一号位不参与</b>——客户一号位不愿花时间是最高频隐性死因。'

const route = useRoute()
const form = ref(null)
const error = ref('')

async function load(code) {
  error.value = ''
  form.value = null
  try {
    form.value = await getForm(code)
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
