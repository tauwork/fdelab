<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索编号 / 名称"
        class="search"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="published" placeholder="全部发布状态" class="status" @change="search">
        <el-option label="全部发布状态" value="all" />
        <el-option label="已发布" :value="1" />
        <el-option label="待复核" :value="0" />
      </el-select>
      <el-button type="primary" class="new" @click="openCreate">新建服务运作</el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="code" label="编号" width="90" />
      <el-table-column prop="name" label="名称" min-width="220" show-overflow-tooltip />
      <el-table-column label="运作标签" width="210" show-overflow-tooltip>
        <template #default="{ row }">{{ rowLabel(row) }}</template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="顺序" width="80" />
      <el-table-column label="挂载产品" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.itemCount" type="info" size="small">{{ row.itemCount }} 条</el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="发布" width="100">
        <template #default="{ row }">
          <el-tag :type="row.published === 1 ? 'success' : 'warning'" size="small">
            {{ row.published === 1 ? '已发布' : '待复核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-tooltip :disabled="isAdmin" content="仅管理员可发布" placement="top">
            <span class="publish-wrap">
              <el-button
                link
                :type="row.published === 1 ? 'warning' : 'success'"
                :disabled="!isAdmin"
                @click="togglePublish(row)"
              >
                {{ row.published === 1 ? '下架' : '发布' }}
              </el-button>
            </span>
          </el-tooltip>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="changePage"
      />
    </div>

    <el-drawer
      v-model="drawer"
      :title="form.id ? `编辑服务运作 · ${form.code}` : '新建服务运作'"
      size="760px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="业务码" prop="code">
          <el-input v-model="form.code" :disabled="!!form.id" placeholder="如 F5，唯一业务码" />
          <div class="hint">系统必填：站点运作详情按它访问，创建后不可改。</div>
        </el-form-item>

        <el-divider content-position="left">卡片字段</el-divider>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="运作标签" prop="form">
          <div class="label-row">
            <el-input v-model="form.form" placeholder="如 产品运作 ①" />
            <span class="dot">·</span>
            <el-input v-model="form.weight" placeholder="如 最轻 · 不改变交付模式" />
          </div>
          <div class="hint">两段拼成卡片顶部整行标签；第二段可留空。</div>
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
          <div class="hint">卡片副文截 72 字，运作详情页首段显示全文。</div>
        </el-form-item>
        <el-form-item label="周期" prop="cycle">
          <el-input v-model="form.cycle" placeholder="如 2–4 周" />
        </el-form-item>
        <el-form-item label="计价" prop="pricing">
          <el-input v-model="form.pricing" />
        </el-form-item>
        <el-form-item label="展示顺序">
          <el-input-number v-model="form.sortOrder" :min="1" :max="999" controls-position="right" />
          <div class="hint">留空时新建自动排最后；站点按它升序，卡片序号也由它生成。</div>
        </el-form-item>

        <el-divider content-position="left">详情内容（可后补）</el-divider>
        <el-form-item label="适合谁">
          <el-input v-model="form.fit" />
        </el-form-item>
        <el-form-item label="交付物清单">
          <el-input v-model="deliverablesText" type="textarea" :rows="5" placeholder="每行一条" />
          <div class="hint">每行一条，保存时转成有序数组，空行忽略。</div>
        </el-form-item>
        <el-form-item label="成功判据">
          <el-input v-model="form.success" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="参照案例">
          <el-select
            v-model="form.caseIds"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            class="cases"
            placeholder="可多选，留空则不展示参照案例"
          >
            <el-option
              v-for="item in meta.cases"
              :key="item.id"
              :label="`${item.code} · ${item.title}${item.published === 1 ? '' : '（待复核）'}`"
              :value="item.id"
            />
          </el-select>
          <div class="hint">选项含草稿；站点只展示已发布案例，草稿关联不会显示。</div>
        </el-form-item>

        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="保存后回到「待复核」，需管理员发布才在站点可见"
          class="save-tip"
        />
      </el-form>

      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-drawer>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createForm,
  deleteForm,
  fetchForm,
  fetchFormMeta,
  fetchForms,
  publishForm,
  updateForm,
} from '../../api/product'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.profile?.roleCode === 'ADMIN')

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const published = ref('all')
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const meta = reactive({ cases: [] })
const drawer = ref(false)
const formRef = ref()
const deliverablesText = ref('')

const form = reactive({
  id: '',
  code: '',
  name: '',
  form: '',
  weight: '',
  sortOrder: undefined,
  fit: '',
  description: '',
  cycle: '',
  pricing: '',
  success: '',
  caseIds: [],
})

const rules = {
  code: [{ required: true, message: '业务码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  form: [{ required: true, message: '运作标签不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '说明不能为空', trigger: 'blur' }],
  cycle: [{ required: true, message: '周期不能为空', trigger: 'blur' }],
  pricing: [{ required: true, message: '计价不能为空', trigger: 'blur' }],
}

function rowLabel(row) {
  return [row.form, row.weight].filter(Boolean).join(' · ') || '—'
}

function formatTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

async function loadMeta() {
  try {
    const data = await fetchFormMeta()
    meta.cases = data?.cases || []
  } catch (error) {
    ElMessage.error(error.message || '案例词表加载失败')
  }
}

async function load() {
  loading.value = true
  try {
    const data = await fetchForms({
      page: page.value,
      size: size.value,
      q: keyword.value || undefined,
      published: published.value === 'all' ? undefined : published.value,
    })
    records.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '服务运作加载失败')
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function changePage(next) {
  page.value = next
  load()
}

function resetForm() {
  Object.assign(form, {
    id: '',
    code: '',
    name: '',
    form: '',
    weight: '',
    sortOrder: undefined,
    fit: '',
    description: '',
    cycle: '',
    pricing: '',
    success: '',
    caseIds: [],
  })
  deliverablesText.value = ''
}

function openCreate() {
  resetForm()
  drawer.value = true
}

async function openEdit(row) {
  try {
    const data = await fetchForm(row.id)
    Object.assign(form, {
      id: data.id,
      code: data.code,
      name: data.name || '',
      form: data.form || '',
      weight: data.weight || '',
      sortOrder: data.sortOrder ?? undefined,
      fit: data.fit || '',
      description: data.description || '',
      cycle: data.cycle || '',
      pricing: data.pricing || '',
      success: data.success || '',
      caseIds: data.caseIds || [],
    })
    deliverablesText.value = (data.deliverables || []).join('\n')
    drawer.value = true
  } catch (error) {
    ElMessage.error(error.message || '服务运作加载失败')
  }
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  const payload = {
    code: form.code,
    name: form.name,
    form: form.form,
    weight: form.weight,
    sortOrder: form.sortOrder ?? null,
    fit: form.fit,
    description: form.description,
    deliverables: deliverablesText.value
      .split('\n')
      .map((line) => line.trim())
      .filter(Boolean),
    cycle: form.cycle,
    pricing: form.pricing,
    success: form.success,
    caseIds: form.caseIds,
  }

  saving.value = true
  try {
    if (form.id) {
      await updateForm(form.id, payload)
    } else {
      await createForm(payload)
    }
    ElMessage.success('已保存，待复核')
    drawer.value = false
    load()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function togglePublish(row) {
  try {
    if (row.published === 1) {
      await ElMessageBox.confirm(`下架 ${row.code} ${row.name || ''}？`, '下架服务运作', { type: 'warning' })
      await publishForm(row.id, 0)
      ElMessage.success('已下架')
    } else {
      await publishForm(row.id, 1)
      ElMessage.success('已发布')
    }
    load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`删除 ${row.code} ${row.name || ''}？`, '删除服务运作', { type: 'warning' })
    await deleteForm(row.id)
    ElMessage.success('已删除')
    if (records.value.length === 1 && page.value > 1) page.value -= 1
    load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(async () => {
  await loadMeta()
  load()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.search {
  width: 240px;
}

.status {
  width: 150px;
}

.new {
  margin-left: auto;
}

.publish-wrap {
  display: inline-block;
  margin: 0 10px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.hint {
  width: 100%;
  font-size: 12px;
  color: var(--muted, #8a8f99);
  line-height: 1.6;
  margin-top: 4px;
}

.label-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.dot {
  color: var(--muted, #8a8f99);
}

.cases {
  width: 100%;
}

.save-tip {
  margin-top: 6px;
}
</style>
