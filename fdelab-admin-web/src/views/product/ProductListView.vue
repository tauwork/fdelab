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
      <el-select v-model="kind" clearable placeholder="全部形态" class="narrow" @change="search">
        <el-option v-for="item in meta.kinds" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="status" clearable placeholder="全部能力状态" class="status" @change="search">
        <el-option v-for="item in meta.statuses" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="published" placeholder="全部发布状态" class="status" @change="search">
        <el-option label="全部发布状态" value="all" />
        <el-option label="已发布" :value="1" />
        <el-option label="待复核" :value="0" />
      </el-select>
      <el-button type="primary" class="new" @click="openCreate">新建产品</el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="code" label="编号" width="100" />
      <el-table-column prop="name" label="名称" min-width="220" show-overflow-tooltip />
      <el-table-column label="形态" width="110">
        <template #default="{ row }">{{ kindLabel(row.kind) }}</template>
      </el-table-column>
      <el-table-column label="能力状态" width="110">
        <template #default="{ row }">{{ statusLabel(row.status) }}</template>
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

    <el-drawer v-model="drawer" :title="form.id ? `编辑产品 · ${form.code}` : '新建产品'" size="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="所属运作" prop="productId">
          <el-select v-model="form.productId" placeholder="选择 F1–F4">
            <el-option
              v-for="item in meta.products"
              :key="item.id"
              :label="`${item.code} · ${item.name}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="形态" prop="kind">
          <el-select v-model="form.kind">
            <el-option v-for="item in meta.kinds" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="能力状态" prop="status">
          <el-select v-model="form.status">
            <el-option v-for="item in meta.statuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品介绍" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="周期" prop="cycle">
          <el-input v-model="form.cycle" placeholder="如 2–4 周" />
        </el-form-item>
        <el-form-item label="计价" prop="pricing">
          <el-input v-model="form.pricing" />
        </el-form-item>

        <el-divider content-position="left">详情内容（过渡期）</el-divider>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="详情页设计定稿后此处改为结构化表单"
          class="detail-tip"
        />
        <el-form-item label="detail JSON">
          <el-input v-model="detailText" type="textarea" :rows="12" class="mono" />
          <div class="detail-actions">
            <el-button size="small" @click="formatDetail">格式化</el-button>
          </div>
        </el-form-item>
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
  createItem,
  deleteItem,
  fetchItem,
  fetchItemMeta,
  fetchItems,
  publishItem,
  updateItem,
} from '../../api/product'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.profile?.roleCode === 'ADMIN')

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const kind = ref('')
const status = ref('')
const published = ref('all')
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const meta = reactive({ kinds: [], statuses: [], products: [] })
const drawer = ref(false)
const formRef = ref()
const detailText = ref('')

const form = reactive({
  id: '',
  code: '',
  productId: '',
  kind: 'de',
  name: '',
  status: 'on',
  summary: '',
  cycle: '',
  pricing: '',
})

const rules = {
  productId: [{ required: true, message: '所属运作不能为空', trigger: 'change' }],
  kind: [{ required: true, message: '形态不能为空', trigger: 'change' }],
  name: [{ required: true, message: '产品名不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '能力状态不能为空', trigger: 'change' }],
  summary: [{ required: true, message: '产品介绍不能为空', trigger: 'blur' }],
  cycle: [{ required: true, message: '周期不能为空', trigger: 'blur' }],
  pricing: [{ required: true, message: '计价不能为空', trigger: 'blur' }],
}

function kindLabel(value) {
  return meta.kinds.find((item) => item.value === value)?.label || value || '—'
}

function statusLabel(value) {
  return meta.statuses.find((item) => item.value === value)?.label || value || '—'
}

function formatTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

async function loadMeta() {
  try {
    const data = await fetchItemMeta()
    meta.kinds = data?.kinds || []
    meta.statuses = data?.statuses || []
    meta.products = data?.products || []
  } catch (error) {
    ElMessage.error(error.message || '词表加载失败')
  }
}

async function load() {
  loading.value = true
  try {
    const data = await fetchItems({
      page: page.value,
      size: size.value,
      q: keyword.value || undefined,
      kind: kind.value || undefined,
      status: status.value || undefined,
      published: published.value === 'all' ? undefined : published.value,
    })
    records.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '产品加载失败')
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
    productId: meta.products[0]?.id || '',
    kind: 'de',
    name: '',
    status: 'on',
    summary: '',
    cycle: '',
    pricing: '',
  })
  detailText.value = ''
}

function openCreate() {
  resetForm()
  drawer.value = true
}

async function openEdit(row) {
  try {
    const data = await fetchItem(row.id)
    Object.assign(form, {
      id: data.id,
      code: data.code,
      productId: data.productId,
      kind: data.kind,
      name: data.name,
      status: data.status,
      summary: data.summary || '',
      cycle: data.cycle || '',
      pricing: data.pricing || '',
    })
    detailText.value = data.detail ? JSON.stringify(data.detail, null, 2) : ''
    drawer.value = true
  } catch (error) {
    ElMessage.error(error.message || '产品加载失败')
  }
}

function formatDetail() {
  if (!detailText.value.trim()) return
  try {
    detailText.value = JSON.stringify(JSON.parse(detailText.value), null, 2)
  } catch {
    ElMessage.error('详情内容不是合法 JSON')
  }
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  if (detailText.value.trim()) {
    try {
      JSON.parse(detailText.value)
    } catch {
      ElMessage.error('详情内容不是合法 JSON')
      return
    }
  }

  const payload = {
    productId: form.productId,
    kind: form.kind,
    name: form.name,
    status: form.status,
    summary: form.summary,
    cycle: form.cycle,
    pricing: form.pricing,
    detail: detailText.value.trim() || null,
  }

  saving.value = true
  try {
    const saved = form.id ? await updateItem(form.id, payload) : await createItem(payload)
    ElMessage.success(!form.id && saved?.code ? `已保存，业务码 ${saved.code}` : '已保存，待复核')
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
      await ElMessageBox.confirm(`下架 ${row.code} ${row.name || ''}？`, '下架产品', { type: 'warning' })
      await publishItem(row.id, 0)
      ElMessage.success('已下架')
    } else {
      await publishItem(row.id, 1)
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
    await ElMessageBox.confirm(`删除 ${row.code} ${row.name || ''}？`, '删除产品', { type: 'warning' })
    await deleteItem(row.id)
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

.narrow {
  width: 130px;
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
  font-size: 12px;
  color: var(--muted, #8a8f99);
  line-height: 1.6;
  margin-top: 4px;
}

.detail-tip {
  margin-bottom: 14px;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  margin-top: 8px;
}

.mono :deep(textarea) {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 12.5px;
}
</style>
