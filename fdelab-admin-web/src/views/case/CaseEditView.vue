<template>
  <div class="edit-page" v-loading="loading">
    <div class="topbar">
      <div class="topbar-left">
        <el-button class="back-btn" @click="goBack">← 返回列表</el-button>
        <span class="code">{{ form.code || meta.nextCode || '新案例' }}</span>
        <el-tag v-if="form.published === 1" type="success" size="small">已发布</el-tag>
        <el-tag v-else type="info" size="small">草稿</el-tag>
        <span class="saved">{{ saveStatus }}</span>
      </div>
      <div class="topbar-right">
        <el-tooltip content="发布后可在前台查看" :disabled="form.published === 1">
          <span>
            <el-button :disabled="form.published !== 1" @click="openSite">预览</el-button>
          </span>
        </el-tooltip>
        <el-button :loading="saving" @click="saveDraft()">保存草稿</el-button>
        <el-tooltip :content="isAdmin ? '发布前会先跑校验' : '仅管理员可发布'">
          <span>
            <el-button type="primary" :disabled="!isAdmin" :loading="publishing" @click="publish">
              发布
            </el-button>
          </span>
        </el-tooltip>
      </div>
    </div>

    <div class="body">
      <aside class="nav">
        <a
          v-for="item in sections"
          :key="item.id"
          class="nav-item"
          :class="dotClass(item.id)"
          href="javascript:void(0)"
          @click.prevent="scrollToSection(item.id)"
        >
          <span class="dot" />
          <span>{{ item.label }}</span>
        </a>
      </aside>

      <main class="form-col">
        <section id="basic" class="panel">
          <h2>基本信息</h2>
          <div class="grid two">
            <el-form-item label="编号（只读）">
              <el-input :model-value="form.code || meta.nextCode" disabled />
            </el-form-item>
            <el-form-item label="标题">
              <el-input v-model="form.title" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item label="副标题" class="wide">
              <el-input v-model="form.subtitle" maxlength="300" show-word-limit />
            </el-form-item>
            <el-form-item label="客户">
              <div class="customer-picker">
                <el-select
                  v-model="form.customerId"
                  filterable
                  remote
                  clearable
                  :remote-method="remoteCustomers"
                  :loading="customerLoading"
                  placeholder="搜索并选择客户"
                  @change="onCustomerChange"
                >
                  <el-option
                    v-for="item in customerOptions"
                    :key="item.id"
                    :label="item.displayName"
                    :value="item.id"
                  />
                  <el-option label="＋ 新建客户" value="__new__" />
                </el-select>
                <el-button @click="openCustomerDialog">新建客户</el-button>
              </div>
            </el-form-item>
            <el-form-item label="交付方">
              <el-input v-model="form.partner" maxlength="200" />
            </el-form-item>
            <el-form-item label="行业">
              <el-select v-model="form.industry" filterable allow-create clearable default-first-option placeholder="选择或输入后回车新建">
                <el-option v-for="item in meta.industries" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="职能">
              <el-select v-model="form.func" filterable allow-create clearable default-first-option placeholder="选择或输入后回车新建">
                <el-option v-for="item in meta.funcs" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="发生年份">
              <el-input v-model="form.occurYear" maxlength="32" placeholder="如 2025–2026" />
            </el-form-item>
            <el-form-item label="规模">
              <el-input v-model="form.scale" maxlength="64" placeholder="如 L:50–200 人月" />
            </el-form-item>
            <el-form-item label="FDE 模式">
              <el-input v-model="form.fdeMode" maxlength="64" placeholder="如 驻场嵌入式" />
            </el-form-item>
            <el-form-item label="模型含量">
              <el-input-number v-model="form.modelPct" :min="0" :max="100" :step="0.1" :precision="1" />
            </el-form-item>
          </div>
        </section>

        <section id="credibility" class="panel">
          <h2>可信度与标签</h2>
          <el-form-item label="可信度">
            <el-radio-group v-model="form.cred">
              <el-radio-button v-for="item in meta.creds" :key="item.code" :value="item.code">
                {{ item.code }} {{ item.label }}
              </el-radio-button>
            </el-radio-group>
            <p class="hint">{{ credHint }}</p>
          </el-form-item>
          <div class="grid two">
            <el-form-item label="可信度说明 · 主标题">
              <el-input v-model="credMain" placeholder="如 A 级" />
            </el-form-item>
            <el-form-item label="可信度说明 · 副标题">
              <el-input v-model="credSub" placeholder="如 客户具名" />
            </el-form-item>
          </div>
          <div class="grid three">
            <el-form-item label="技术标签">
              <el-select v-model="form.tech" multiple filterable allow-create default-first-option placeholder="选择或输入后回车新建">
                <el-option v-for="item in meta.tags.tech" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="AI 标签">
              <el-select v-model="form.ai" multiple filterable allow-create default-first-option placeholder="选择或输入后回车新建">
                <el-option v-for="item in meta.tags.ai" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="管理标签">
              <el-select v-model="form.mgmt" multiple filterable allow-create default-first-option placeholder="选择或输入后回车新建">
                <el-option v-for="item in meta.tags.mgmt" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </div>
        </section>

        <section id="narrative" class="panel">
          <h2>痛点与目标</h2>
          <el-form-item label="痛点">
            <el-input v-model="form.pain" type="textarea" :rows="5" />
            <span class="count">{{ form.pain.length }} 字</span>
          </el-form-item>
          <el-form-item label="目标">
            <el-input v-model="form.goal" type="textarea" :rows="4" />
            <span class="count">{{ form.goal.length }} 字</span>
          </el-form-item>
        </section>

        <section id="timeline" class="panel">
          <h2>推进路径</h2>
          <row-card
            v-for="(row, index) in form.timeline"
            :key="index"
            :index="index"
            :total="form.timeline.length"
            :label="`阶段 ${index + 1}`"
            @up="moveIn('timeline', index, index - 1)"
            @down="moveIn('timeline', index, index + 1)"
            @remove="form.timeline.splice(index, 1)"
            @dragstart="startDrag('timeline', index)"
            @drop="dropAt('timeline', index)"
          >
            <el-input v-model="row.stage" placeholder="阶段" />
            <el-input v-model="row.note" type="textarea" :rows="2" placeholder="说明" />
          </row-card>
          <el-button size="small" @click="form.timeline.push({ stage: '', note: '' })">添加阶段</el-button>
        </section>

        <section id="org" class="panel">
          <h2>双方分工</h2>
          <row-card
            v-for="(row, index) in form.org"
            :key="index"
            :index="index"
            :total="form.org.length"
            :label="`分工 ${index + 1}`"
            @up="moveIn('org', index, index - 1)"
            @down="moveIn('org', index, index + 1)"
            @remove="form.org.splice(index, 1)"
            @dragstart="startDrag('org', index)"
            @drop="dropAt('org', index)"
          >
            <el-input v-model="row.role" placeholder="角色" />
            <el-input v-model="row.headcount" placeholder="配置 / 人数" />
            <el-input v-model="row.duty" placeholder="职责" />
          </row-card>
          <el-button size="small" @click="form.org.push({ role: '', headcount: '', duty: '' })">添加分工</el-button>
        </section>

        <section id="effects" class="panel">
          <h2>业务效果</h2>
          <metric-table
            :metrics="form.metrics"
            :sources="form.sources"
            @add="form.metrics.push(blankMetric())"
            @append="appendMetrics"
            @move="moveMetricRow"
            @remove="removeMetricRow"
          />
          <kpi-editor
            class="sub-block"
            :kpis="form.kpis"
            :metrics="form.metrics"
            @add="addKpi"
            @remove="form.kpis.splice($event, 1)"
          />

          <h3 class="sub-title">一线证言</h3>
          <row-card
            v-for="(row, index) in form.testimony"
            :key="index"
            :index="index"
            :total="form.testimony.length"
            :label="`证言 ${index + 1}`"
            @up="moveIn('testimony', index, index - 1)"
            @down="moveIn('testimony', index, index + 1)"
            @remove="form.testimony.splice(index, 1)"
            @dragstart="startDrag('testimony', index)"
            @drop="dropAt('testimony', index)"
          >
            <el-input v-model="row.quote" type="textarea" :rows="2" placeholder="证言原文" />
            <el-input v-model="row.attribution" placeholder="说话人 / 出处" />
          </row-card>
          <el-button size="small" @click="form.testimony.push({ quote: '', attribution: '' })">添加证言</el-button>
        </section>

        <section id="csf" class="panel">
          <h2>关键成功要素</h2>
          <row-card
            v-for="(row, index) in form.csf"
            :key="index"
            :index="index"
            :total="form.csf.length"
            :label="`要素 ${index + 1}`"
            @up="moveIn('csf', index, index - 1)"
            @down="moveIn('csf', index, index + 1)"
            @remove="form.csf.splice(index, 1)"
            @dragstart="startDrag('csf', index)"
            @drop="dropAt('csf', index)"
          >
            <el-input v-model="row.dimension" placeholder="维度" />
            <el-input v-model="row.practice" placeholder="做法" />
            <el-input v-model="row.reason" placeholder="为什么重要" />
            <el-input v-model="row.consequence" placeholder="缺失的后果" />
          </row-card>
          <el-button size="small" @click="form.csf.push({ dimension: '', practice: '', reason: '', consequence: '' })">
            添加要素
          </el-button>
        </section>

        <section id="verdict" class="panel">
          <h2>能力归属检验</h2>
          <div class="grid two">
            <el-form-item label="能力沉淀在哪一侧">
              <el-input v-model="form.verdict.deposit" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="模型含量估算（叙述文本）">
              <el-input v-model="form.verdict.modelPct" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="可复用资产与产品化率">
              <el-input v-model="form.verdict.reusable" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="单位经济学走向">
              <el-input v-model="form.verdict.economics" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="会被下一代模型冲掉吗" class="wide">
              <el-input v-model="form.verdict.survive" type="textarea" :rows="2" />
            </el-form-item>
          </div>
          <el-form-item label="一句话结论">
            <el-input v-model="form.verdict.answer" type="textarea" :rows="4" />
          </el-form-item>
        </section>

        <section id="sources" class="panel">
          <h2>引用与来源</h2>
          <h3 class="sub-title">引用</h3>
          <row-card
            v-for="(row, index) in form.quotes"
            :key="index"
            :index="index"
            :total="form.quotes.length"
            :label="`引用 ${index + 1}`"
            @up="moveIn('quotes', index, index - 1)"
            @down="moveIn('quotes', index, index + 1)"
            @remove="form.quotes.splice(index, 1)"
            @dragstart="startDrag('quotes', index)"
            @drop="dropAt('quotes', index)"
          >
            <el-input v-model="row.text" type="textarea" :rows="2" placeholder="原文" />
            <el-input v-model="row.source" placeholder="出处" />
          </row-card>
          <el-button size="small" @click="form.quotes.push({ text: '', source: '' })">添加引用</el-button>

          <h3 class="sub-title">来源</h3>
          <row-card
            v-for="(row, index) in form.sources"
            :key="index"
            :index="index"
            :total="form.sources.length"
            :label="`来源 ${index + 1}`"
            @up="moveIn('sources', index, index - 1)"
            @down="moveIn('sources', index, index + 1)"
            @remove="form.sources.splice(index, 1)"
            @dragstart="startDrag('sources', index)"
            @drop="dropAt('sources', index)"
          >
            <el-input v-model="row.title" placeholder="标题" />
            <el-input v-model="row.url" placeholder="https://..." />
            <el-input v-model="row.type" placeholder="来源类型，如 厂商官方（一手）" />
          </row-card>
          <el-button size="small" @click="form.sources.push({ title: '', url: '', type: '' })">添加来源</el-button>
        </section>
      </main>

      <aside class="preview">
        <h3>列表卡片预览</h3>
        <article class="pcard">
          <div class="pcard-top">
            <span class="ava" :class="credClass">{{ initial }}</span>
            <div class="pcard-title">
              <b>{{ form.title || '未命名案例' }}</b>
              <span class="pcred" :class="credClass">{{ form.cred || '—' }}</span>
            </div>
          </div>
          <p class="prole">{{ form.subtitle || '副标题' }}</p>
          <p class="pmeta">
            <template v-if="previewCustomerName">客户：<b>{{ previewCustomerName }}</b>　·　</template>
            {{ form.occurYear || '发生年份' }}
            <template v-if="form.func">　·　{{ form.func }}</template>
          </p>
          <p class="pbio">{{ form.pain || '痛点原文会显示在这里，最多两行。' }}</p>
          <span class="pchip">{{ form.industry || '行业' }}</span>
          <div class="pkpis">
            <div v-for="(kpi, index) in previewKpis" :key="index" class="pkpi">
              <b :class="{ red: kpi.r }">{{ kpi.v || '—' }}</b>
              <span>{{ kpi.k || '指标名' }}</span>
            </div>
            <p v-if="!previewKpis.length" class="pempty">还没有 KPI 卡</p>
          </div>
          <div class="pfoot">
            <span>硬指标 {{ previewMetricCount }}</span>
            <span>查看 →</span>
          </div>
        </article>
      </aside>
    </div>

    <el-dialog v-model="customerDialogVisible" title="新建客户" width="420px">
      <el-form label-position="top">
        <el-form-item label="客户显示名">
          <el-input v-model="customerForm.displayName" placeholder="如 Airbus（具名）" />
        </el-form-item>
        <el-form-item label="是否脱敏">
          <el-switch v-model="customerForm.isMasked" :active-value="1" :inactive-value="0" active-text="脱敏" inactive-text="具名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="customerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="customerSaving" @click="saveNewCustomer">保存并选中</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCase, fetchCase, fetchCaseMeta, publishCase, SITE_BASE, updateCase, validateCase } from '../../api/case'
import { createCustomer, searchCustomers } from '../../api/customer'
import { useUserStore } from '../../stores/user'
import MetricTable from './components/MetricTable.vue'
import KpiEditor from './components/KpiEditor.vue'
import RowCard from './components/RowCard.vue'
import { moveMetric, removeMetricAt } from './metricRemap'
import { isMetricBlank, isSourceBlank, joinCredNote, sectionStatus, splitCredNote, validateForm } from './validation'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.profile?.roleCode === 'ADMIN')

const sections = [
  { id: 'basic', label: '基本信息' },
  { id: 'credibility', label: '可信度与标签' },
  { id: 'narrative', label: '痛点与目标' },
  { id: 'timeline', label: '推进路径' },
  { id: 'org', label: '双方分工' },
  { id: 'effects', label: '业务效果' },
  { id: 'csf', label: '关键成功要素' },
  { id: 'verdict', label: '能力归属检验' },
  { id: 'sources', label: '引用与来源' },
]

const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const customerLoading = ref(false)
const customerSaving = ref(false)
const customerDialogVisible = ref(false)
const customerOptions = ref([])
const customerForm = reactive({ displayName: '', isMasked: 1 })
const savedAt = ref(null)
const saveStatus = ref('尚未保存')
const apiIssues = ref({ errors: [], warnings: [] })
const credMain = ref('')
const credSub = ref('')
const dragging = ref({ section: '', index: -1 })
const loaded = ref(false)
const dirty = ref(false)
const issuesVisible = ref(false)
let filling = false
let autoSaveTimer = null
let localTimer = null

const meta = reactive({
  nextCode: '',
  industries: [],
  funcs: [],
  tags: { tech: [], ai: [], mgmt: [] },
  creds: [
    { code: 'A', label: '客户侧证实', hint: '' },
    { code: 'B', label: '厂商具名', hint: '' },
    { code: 'C', label: '厂商匿名', hint: '' },
    { code: 'D', label: '二手转述', hint: '' },
  ],
})

function blankMetric() {
  return { name: '', definition: '', baseline: '', result: '', window: '', cred: '', sourceRef: null }
}

function blankKpi() {
  return { v: '', k: '', r: false, m: null }
}

function blankVerdict() {
  return { deposit: '', modelPct: '', reusable: '', economics: '', survive: '', answer: '' }
}

function blankForm() {
  return {
    id: null,
    code: '',
    title: '',
    subtitle: '',
    industry: '',
    func: '',
    occurYear: '',
    partner: '',
    cred: 'C',
    credNote: '',
    fdeMode: '',
    scale: '',
    modelPct: null,
    customerId: null,
    customerName: '',
    newCustomer: null,
    pain: '',
    goal: '',
    tech: [],
    ai: [],
    mgmt: [],
    kpis: [],
    timeline: [],
    org: [],
    metrics: [],
    csf: [],
    testimony: [],
    quotes: [],
    sources: [],
    verdict: blankVerdict(),
    primaryFdeId: null,
    contributorIds: [],
    published: 0,
  }
}

const form = reactive(blankForm())

const credHint = computed(() => meta.creds.find((item) => item.code === form.cred)?.hint || '')

const selectedCustomer = computed(() => customerOptions.value.find((item) => item.id === form.customerId))
const previewCustomerName = computed(() => selectedCustomer.value?.displayName || form.customerName || '')
const previewKpis = computed(() => form.kpis.filter((item) => item.v || item.k))
const previewMetricCount = computed(
  () => form.metrics.filter((row) => row.name || row.definition || row.baseline || row.result || row.window || row.cred).length,
)
const initial = computed(() => {
  const partner = String(form.partner || '').trim()
  if (partner) {
    const first = partner.split(/\s+/)[0] || partner
    return first.charAt(0).toUpperCase()
  }
  return String(form.title || '案').trim().charAt(0) || '案'
})
const credClass = computed(() => String(form.cred || 'd').toLowerCase())

const clientValidation = computed(() => validateForm(form))
const effectiveValidation = computed(() => {
  // 空白新建表单不提前飘红；用户改动过、或已有 id 后才展示校验结果。
  if (!issuesVisible.value) return { errors: [], warnings: [] }
  return {
    errors: [...clientValidation.value.errors, ...apiIssues.value.errors],
    warnings: [...clientValidation.value.warnings, ...apiIssues.value.warnings],
  }
})

const filledSections = computed(() => ({
  basic: Boolean(form.title || form.industry || form.customerId),
  credibility: true,
  narrative: Boolean(form.pain || form.goal),
  timeline: form.timeline.length > 0,
  org: form.org.length > 0,
  effects: form.metrics.length > 0 || form.kpis.length > 0 || form.testimony.length > 0,
  csf: form.csf.length > 0,
  verdict: Object.values(form.verdict || {}).some((value) => String(value || '').trim() !== ''),
  sources: form.sources.length > 0 || form.quotes.length > 0,
}))

function dotClass(section) {
  return sectionStatus(effectiveValidation.value, section, filledSections.value[section])
}

function scrollToSection(section) {
  const element = document.getElementById(section)
  if (!element) return
  const top = element.getBoundingClientRect().top + window.scrollY - 76
  window.scrollTo({ top, behavior: 'smooth' })
}

function startDrag(section, index) {
  dragging.value = { section, index }
}

function dropAt(section, index) {
  if (dragging.value.section !== section) return
  moveIn(section, dragging.value.index, index)
  dragging.value = { section: '', index: -1 }
}

function moveIn(section, from, to) {
  const list = form[section]
  if (!list || from < 0 || to < 0 || from >= list.length || to >= list.length || from === to) return
  const [row] = list.splice(from, 1)
  list.splice(to, 0, row)
}

function moveMetricRow(from, to) {
  moveMetric(form.metrics, form.kpis, from, to)
  dirty.value = true
}

function removeMetricRow(index) {
  // 最容易漏的 bug：删除指标行后同步重算 kpis[].m。
  removeMetricAt(form.metrics, form.kpis, index)
  dirty.value = true
}

function appendMetrics(rows) {
  form.metrics.push(...rows)
  dirty.value = true
}

function addKpi() {
  if (form.kpis.length < 4) {
    form.kpis.push(blankKpi())
    dirty.value = true
  }
}

function splitCred(note) {
  const [main, sub] = splitCredNote(note)
  credMain.value = main
  credSub.value = sub
}

watch([credMain, credSub], () => {
  form.credNote = joinCredNote(credMain.value, credSub.value)
})

watch(
  form,
  () => {
    if (filling) return
    dirty.value = true
    issuesVisible.value = true
    apiIssues.value = { errors: [], warnings: [] }
  },
  { deep: true },
)

async function fillForm(data) {
  filling = true
  Object.assign(form, blankForm(), data || {})
  const textFields = [
    'code', 'title', 'subtitle', 'industry', 'func', 'occurYear', 'partner',
    'cred', 'credNote', 'fdeMode', 'scale', 'pain', 'goal', 'customerName',
  ]
  for (const field of textFields) {
    form[field] = data?.[field] == null ? '' : String(data[field])
  }
  form.modelPct = data?.modelPct == null || data.modelPct === '' ? null : Number(data.modelPct)
  form.customerId = data?.customerId ?? null
  form.primaryFdeId = data?.primaryFdeId ?? null
  form.published = data?.published ?? 0
  form.tech = Array.isArray(data?.tech) ? [...data.tech] : []
  form.ai = Array.isArray(data?.ai) ? [...data.ai] : []
  form.mgmt = Array.isArray(data?.mgmt) ? [...data.mgmt] : []
  form.kpis = (data?.kpis || []).map((item) => ({ ...blankKpi(), ...item }))
  form.timeline = (data?.timeline || []).map((item) => ({ stage: '', note: '', ...item }))
  form.org = (data?.org || []).map((item) => ({ role: '', headcount: '', duty: '', ...item }))
  form.metrics = (data?.metrics || []).map((item) => ({ ...blankMetric(), ...item }))
  form.csf = (data?.csf || []).map((item) => ({ dimension: '', practice: '', reason: '', consequence: '', ...item }))
  form.testimony = (data?.testimony || []).map((item) => ({ quote: '', attribution: '', ...item }))
  form.quotes = (data?.quotes || []).map((item) => ({ text: '', source: '', ...item }))
  form.sources = (data?.sources || []).map((item) => ({ title: '', url: '', type: '', ...item }))
  form.verdict = { ...blankVerdict(), ...(data?.verdict || {}) }
  form.contributorIds = Array.isArray(data?.contributorIds) ? [...data.contributorIds] : []
  splitCred(form.credNote)
  await nextTick()
  filling = false
  dirty.value = false
  issuesVisible.value = Boolean(form.id)
}

async function loadMeta() {
  try {
    const data = await fetchCaseMeta()
    Object.assign(meta, data || {})
    meta.tags = data?.tags || { tech: [], ai: [], mgmt: [] }
    if (data?.creds?.length) meta.creds = data.creds
  } catch (error) {
    ElMessage.error(error.message || '词表加载失败')
  }
}

async function remoteCustomers(keyword) {
  customerLoading.value = true
  try {
    const list = await searchCustomers(keyword)
    const selected = customerOptions.value.find((item) => item.id === form.customerId)
    const merged = [...list]
    if (selected && !merged.some((item) => item.id === selected.id)) merged.unshift(selected)
    customerOptions.value = merged
  } catch (error) {
    ElMessage.error(error.message || '客户搜索失败')
  } finally {
    customerLoading.value = false
  }
}

function ensureCustomerOption(id, name) {
  if (id == null) return
  if (!customerOptions.value.some((item) => item.id === id)) {
    customerOptions.value.unshift({ id, displayName: name || '未命名客户', isMasked: 0 })
  }
}

function onCustomerChange(value) {
  if (value === '__new__') {
    form.customerId = null
    openCustomerDialog()
    return
  }
  const item = customerOptions.value.find((customer) => customer.id === value)
  form.customerName = item?.displayName || ''
}

function openCustomerDialog() {
  customerForm.displayName = ''
  customerForm.isMasked = 1
  customerDialogVisible.value = true
}

async function saveNewCustomer() {
  if (!customerForm.displayName.trim()) {
    ElMessage.warning('请填写客户显示名')
    return
  }
  customerSaving.value = true
  try {
    const customer = await createCustomer({
      displayName: customerForm.displayName.trim(),
      isMasked: customerForm.isMasked,
    })
    ensureCustomerOption(customer.id, customer.displayName)
    form.customerId = customer.id
    form.customerName = customer.displayName
    customerDialogVisible.value = false
    ElMessage.success('客户已创建并选中')
  } catch (error) {
    ElMessage.error(error.message || '客户创建失败')
  } finally {
    customerSaving.value = false
  }
}

async function loadCase(id) {
  // 雪花 ID 超出 JS 安全整数范围，必须保持字符串，不能用 Number() 转换。
  id = String(id)
  loading.value = true
  try {
    const data = await fetchCase(id)
    await fillForm({ ...data, id })
    ensureCustomerOption(data.customerId, data.customerName)
  } catch (error) {
    ElMessage.error(error.message || '案例加载失败')
    router.replace('/cases')
  } finally {
    loading.value = false
  }
}

function localKey() {
  return `fdelab:case-draft:${form.id || 'new'}`
}

function storeLocalDraft() {
  if (!loaded.value || !dirty.value) return
  try {
    localStorage.setItem(localKey(), JSON.stringify({ savedAt: Date.now(), form: JSON.parse(JSON.stringify(form)) }))
  } catch {
    // 忽略配额或序列化失败。
  }
}

function clearLocalDraft() {
  localStorage.removeItem('fdelab:case-draft:new')
  if (form.id) localStorage.removeItem(`fdelab:case-draft:${form.id}`)
}

async function maybeRestoreDraft() {
  let raw = null
  try {
    raw = localStorage.getItem(localKey())
  } catch {
    return
  }
  if (!raw) return
  let draft = null
  try {
    draft = JSON.parse(raw)
  } catch {
    localStorage.removeItem(localKey())
    return
  }
  if (!draft?.form) return
  const when = new Date(draft.savedAt || Date.now()).toLocaleString()
  const restore = await ElMessageBox.confirm(
    `检测到 ${when} 的本地未保存草稿，是否恢复？`,
    '恢复草稿',
    { confirmButtonText: '恢复', cancelButtonText: '忽略', type: 'warning' },
  )
    .then(() => true)
    .catch(() => false)
  if (restore) {
    const id = form.id
    await fillForm({ ...draft.form, id })
    dirty.value = true
    issuesVisible.value = true
  } else {
    localStorage.removeItem(localKey())
  }
}

function payload() {
  // 空行不落库：先把 UI 的原始下标映射成落库后的压缩下标，再写 m / sourceRef。
  const metricIndexMap = new Map()
  let metricCursor = 0
  form.metrics.forEach((row, index) => {
    if (!isMetricBlank(row)) metricIndexMap.set(index, metricCursor++)
  })
  const sourceIndexMap = new Map()
  let sourceCursor = 0
  form.sources.forEach((row, index) => {
    if (row && !isSourceBlank(row)) sourceIndexMap.set(index, sourceCursor++)
  })

  return {
    code: form.code,
    title: form.title,
    subtitle: form.subtitle,
    industry: form.industry,
    func: form.func,
    occurYear: form.occurYear,
    partner: form.partner,
    cred: form.cred,
    credNote: form.credNote,
    fdeMode: form.fdeMode,
    scale: form.scale,
    modelPct: form.modelPct,
    customerId: form.customerId,
    newCustomer: null,
    pain: form.pain,
    goal: form.goal,
    tech: form.tech,
    ai: form.ai,
    mgmt: form.mgmt,
    kpis: form.kpis.map((item) => ({
      v: item.v,
      k: item.k,
      r: Boolean(item.r),
      m: item.m == null || item.m === '' || !metricIndexMap.has(Number(item.m))
        ? null
        : metricIndexMap.get(Number(item.m)),
    })),
    timeline: form.timeline.map((item) => ({ stage: item.stage, note: item.note })),
    org: form.org.map((item) => ({ role: item.role, headcount: item.headcount, duty: item.duty })),
    metrics: form.metrics.map((item) => ({
      name: item.name,
      definition: item.definition,
      baseline: item.baseline,
      result: item.result,
      window: item.window,
      cred: item.cred,
      sourceRef: item.sourceRef == null || item.sourceRef === ''
        ? null
        : (sourceIndexMap.has(Number(item.sourceRef) - 1)
          ? sourceIndexMap.get(Number(item.sourceRef) - 1) + 1
          : null),
    })),
    csf: form.csf.map((item) => ({
      dimension: item.dimension,
      practice: item.practice,
      reason: item.reason,
      consequence: item.consequence,
    })),
    testimony: form.testimony.map((item) => ({ quote: item.quote, attribution: item.attribution })),
    quotes: form.quotes.map((item) => ({ text: item.text, source: item.source })),
    sources: form.sources.map((item) => ({ title: item.title, url: item.url, type: item.type })),
    verdict: { ...form.verdict },
    primaryFdeId: form.primaryFdeId,
    contributorIds: form.contributorIds,
    published: form.published,
  }
}

/** 返回列表：有未保存更改时先询问（保存并返回 / 不保存直接返回 / 留在页面） */
async function goBack() {
  if (dirty.value) {
    try {
      await ElMessageBox.confirm('有未保存的更改，返回前要保存吗？', '离开编辑页', {
        distinguishCancelAndClose: true,
        confirmButtonText: '保存并返回',
        cancelButtonText: '不保存直接返回',
        type: 'warning',
      })
    } catch (action) {
      // 'cancel' = 不保存直接返回；'close' = 关闭弹窗，留在页面
      if (action !== 'cancel') return
      router.push('/cases')
      return
    }
    try {
      await saveDraft({ silent: true })
    } catch {
      return // 保存失败，留在页面让用户处理
    }
  }
  router.push('/cases')
}

async function saveDraft(options = {}) {
  if (saving.value) return null
  saving.value = true
  try {
    const data = form.id ? await updateCase(form.id, payload()) : await createCase(payload())
    const wasNew = !form.id
    form.id = data.id
    form.code = data.code || form.code
    form.published = data.published ?? form.published
    if (data.customerId != null) {
      form.customerId = data.customerId
      ensureCustomerOption(data.customerId, data.customerName)
    }
    if (wasNew) {
      await router.replace(`/cases/${data.id}/edit`)
    }
    savedAt.value = new Date()
    dirty.value = false
    clearLocalDraft()
    saveStatus.value = `保存于 ${savedAt.value.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
    if (!options.silent) ElMessage.success('已保存')
    return data
  } catch (error) {
    if (!options.silent) ElMessage.error(error.message || '保存失败')
    throw error
  } finally {
    saving.value = false
  }
}

async function publish() {
  if (!isAdmin.value) return
  issuesVisible.value = true
  publishing.value = true
  try {
    const check = await validateCase(payload(), form.id)
    apiIssues.value = { errors: check.errors || [], warnings: check.warnings || [] }
    if (!check.ok) {
      const first = check.errors?.[0]
      ElMessage.error(first?.message || '校验未通过')
      if (first) await nextTick().then(() => scrollToSection(first.section))
      return
    }
    const saved = await saveDraft({ silent: true })
    if (!saved?.id) return
    await publishCase(saved.id)
    form.published = 1
    dirty.value = false
    clearLocalDraft()
    ElMessage.success('已发布')
    saveStatus.value = `保存于 ${new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

function openSite() {
  if (!form.code) return
  window.open(`${SITE_BASE}/articles/${encodeURIComponent(form.code)}`, '_blank')
}

onMounted(async () => {
  await loadMeta()
  const id = route.params.id || null
  if (id) {
    await loadCase(id)
  } else {
    await fillForm(blankForm())
    form.code = meta.nextCode || ''
  }
  await remoteCustomers('')
  loaded.value = true
  await maybeRestoreDraft()

  autoSaveTimer = setInterval(() => {
    if (form.id && dirty.value && !saving.value) {
      saveDraft({ silent: true }).catch(() => {})
    }
  }, 60000)
  localTimer = setInterval(storeLocalDraft, 10000)
})

onBeforeUnmount(() => {
  if (autoSaveTimer) clearInterval(autoSaveTimer)
  if (localTimer) clearInterval(localTimer)
})
</script>

<style scoped>
.edit-page {
  max-width: 1480px;
  margin: 0 auto;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: rgba(255, 253, 248, 0.96);
  border: 1px solid var(--line);
  border-radius: 10px;
  backdrop-filter: blur(6px);
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.code {
  font-family: "Iowan Old Style", Palatino, serif;
  font-size: 17px;
}

.saved {
  font-size: 12px;
  color: #8a938d;
}

.body {
  display: grid;
  grid-template-columns: 200px minmax(0, 1fr) 280px;
  gap: 18px;
  align-items: start;
}

.nav {
  position: sticky;
  top: 76px;
  display: grid;
  gap: 2px;
  padding: 8px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fffdf8;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 8px 10px;
  border-radius: 7px;
  color: #4c5851;
  text-decoration: none;
  font-size: 13px;
}

.nav-item:hover {
  background: #f4f1ea;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d8d2c4;
  flex: none;
}

.nav-item.filled .dot {
  background: #1f6b4a;
}

.nav-item.warning .dot {
  background: #d19a2a;
}

.nav-item.error .dot {
  background: #c0392b;
}

.form-col {
  min-width: 0;
}

.panel {
  padding: 18px 20px 20px;
  margin-bottom: 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fffdf8;
}

.panel h2 {
  margin: 0 0 16px;
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 20px;
  font-weight: 500;
}

.grid {
  display: grid;
  gap: 0 16px;
}

.grid.two {
  grid-template-columns: 1fr 1fr;
}

.grid.three {
  grid-template-columns: repeat(3, 1fr);
}

.wide {
  grid-column: 1 / -1;
}

.customer-picker {
  display: flex;
  gap: 8px;
  width: 100%;
}

.customer-picker .el-select {
  flex: 1;
}

.hint {
  width: 100%;
  margin: 6px 0 0;
  font-size: 12px;
  color: #8a938d;
}

.count {
  display: block;
  margin-top: 4px;
  text-align: right;
  font-size: 12px;
  color: #9aa39d;
}

.sub-block {
  margin-top: 16px;
}

.sub-title {
  margin: 18px 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: #4c5851;
}

.preview {
  position: sticky;
  top: 76px;
}

.preview h3 {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 500;
  color: #6d7872;
}

.pcard {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fffdf8;
}

.pcard-top {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.ava {
  width: 38px;
  height: 38px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  background: #7b8c83;
  flex: none;
}

.ava.a {
  background: #2f7d58;
}

.ava.b {
  background: #2f5f96;
}

.ava.c {
  background: #b07a1c;
}

.ava.d {
  background: #7b8c83;
}

.pcard-title {
  min-width: 0;
}

.pcard-title b {
  display: block;
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 16px;
  font-weight: 400;
  line-height: 1.35;
}

.pcred {
  display: inline-block;
  margin-top: 3px;
  padding: 1px 7px;
  border-radius: 999px;
  border: 1px solid #d8d2c4;
  font-size: 10px;
  font-weight: 600;
  color: #6d7872;
}

.pcred.a {
  color: #2f7d58;
  border-color: #b7d8c6;
  background: #edf7f1;
}

.pcred.b {
  color: #2f5f96;
  border-color: #bdd0e7;
  background: #eef4fb;
}

.pcred.c {
  color: #a3690b;
  border-color: #e6d1a6;
  background: #fbf5e8;
}

.prole {
  margin: 8px 0 0;
  font-size: 12px;
  color: #6d7872;
}

.pmeta {
  margin: 6px 0 0;
  font-size: 11px;
  color: #8a938d;
}

.pbio {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: #4c5851;
}

.pchip {
  display: inline-block;
  margin-top: 9px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #f4f1ea;
  font-size: 10px;
  color: #6d7872;
}

.pkpis {
  display: flex;
  gap: 6px;
  margin-top: 12px;
  min-height: 40px;
}

.pkpi {
  flex: 1;
  min-width: 0;
  padding-right: 6px;
  border-right: 1px solid #efe9dd;
}

.pkpi:last-child {
  border-right: 0;
}

.pkpi b {
  display: block;
  font-size: 14px;
  color: #17241e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pkpi b.red {
  color: #c0392b;
}

.pkpi span {
  display: block;
  margin-top: 2px;
  font-size: 10px;
  color: #8a938d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pempty {
  margin: 0;
  font-size: 11px;
  color: #b0b7b2;
}

.pfoot {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 9px;
  border-top: 1px solid #efe9dd;
  font-size: 11px;
  color: #8a938d;
}

@media (max-width: 1280px) {
  .body {
    grid-template-columns: 200px minmax(0, 1fr);
  }

  .preview {
    display: none;
  }
}

@media (max-width: 960px) {
  .body {
    grid-template-columns: 1fr;
  }

  .nav {
    position: static;
    grid-template-columns: repeat(3, 1fr);
  }

  .grid.two,
  .grid.three {
    grid-template-columns: 1fr;
  }
}
</style>
