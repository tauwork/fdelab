<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索标题 / 交付方 / 编号"
        class="search"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="industry" clearable placeholder="全部行业" class="industry" @change="search">
        <el-option v-for="item in industries" :key="item" :label="item" :value="item" />
      </el-select>
      <div class="creds">
        <el-button
          v-for="item in credOptions"
          :key="item.value"
          size="small"
          round
          :type="cred === item.value ? 'primary' : ''"
          @click="selectCred(item.value)"
        >
          {{ item.label }}
        </el-button>
      </div>
      <el-select v-model="status" placeholder="全部状态" class="status" @change="search">
        <el-option label="全部状态" value="all" />
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-button type="primary" class="new" @click="router.push('/cases/new')">新建案例</el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="code" label="编号" width="110" />
      <el-table-column label="标题" min-width="240" show-overflow-tooltip>
        <template #default="{ row }">{{ row.title || '（未命名案例）' }}</template>
      </el-table-column>
      <el-table-column prop="customerName" label="客户" min-width="180" show-overflow-tooltip />
      <el-table-column prop="industry" label="行业" min-width="150" show-overflow-tooltip />
      <el-table-column label="可信度" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.cred" :type="credTagType(row.cred)" size="small">{{ row.cred }}</el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="metricCount" label="指标数" width="82" align="center" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.published === 1 ? 'success' : 'info'">{{ row.published === 1 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/cases/${row.id}/edit`)">编辑</el-button>
          <el-button link :disabled="row.published !== 1" @click="preview(row)">预览</el-button>
          <el-button link @click="clone(row)">复制</el-button>
          <el-button v-if="isAdmin" link :type="row.published === 1 ? 'warning' : 'success'" @click="togglePublish(row)">
            {{ row.published === 1 ? '下架' : '发布' }}
          </el-button>
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
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cloneCase,
  deleteCase,
  fetchCaseMeta,
  fetchCases,
  publishCase,
  SITE_BASE,
  unpublishCase,
} from '../../api/case'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.profile?.roleCode === 'ADMIN')

const loading = ref(false)
const keyword = ref('')
const industry = ref('')
const cred = ref('')
const status = ref('all')
const industries = ref([])
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const credOptions = [
  { value: '', label: '全部可信度' },
  { value: 'A', label: 'A 客户侧证实' },
  { value: 'B', label: 'B 厂商具名' },
  { value: 'C', label: 'C 厂商匿名' },
  { value: 'D', label: 'D 二手转述' },
]

function credTagType(value) {
  return { A: 'success', B: 'primary', C: 'warning', D: 'info' }[value] || 'info'
}

function formatTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

async function load() {
  loading.value = true
  try {
    const data = await fetchCases({
      page: page.value,
      size: size.value,
      q: keyword.value || undefined,
      industry: industry.value || undefined,
      cred: cred.value || undefined,
      published: status.value === 'all' ? undefined : status.value,
    })
    records.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '案例加载失败')
  } finally {
    loading.value = false
  }
}

async function loadMeta() {
  try {
    const data = await fetchCaseMeta()
    industries.value = data?.industries || []
  } catch {
    industries.value = []
  }
}

function search() {
  page.value = 1
  load()
}

function selectCred(value) {
  if (cred.value === value) return
  cred.value = value
  search()
}

function changePage(next) {
  page.value = next
  load()
}

function preview(row) {
  if (row.published !== 1) return
  window.open(`${SITE_BASE}/articles/${encodeURIComponent(row.code)}`, '_blank')
}

async function clone(row) {
  try {
    const data = await cloneCase(row.id)
    ElMessage.success('已复制为新草稿')
    router.push(`/cases/${data.id}/edit`)
  } catch (error) {
    ElMessage.error(error.message || '复制失败')
  }
}

async function togglePublish(row) {
  try {
    if (row.published === 1) {
      await ElMessageBox.confirm(`下架 ${row.code}？`, '下架案例')
      await unpublishCase(row.id)
      ElMessage.success('已下架')
    } else {
      await publishCase(row.id)
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
    await ElMessageBox.confirm(`删除 ${row.code} ${row.title || ''}？`, '删除案例', { type: 'warning' })
    await deleteCase(row.id)
    ElMessage.success('已删除')
    if (records.value.length === 1 && page.value > 1) page.value -= 1
    load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadMeta()
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
  width: 260px;
}

.industry {
  width: 190px;
}

.status {
  width: 130px;
}

.creds {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.new {
  margin-left: auto;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
