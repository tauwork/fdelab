<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="搜索用户名或昵称" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column label="角色" min-width="100">
        <template #default="{ row }">{{ roleLabel(row.roleCode) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
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
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUsers } from '../../api/system'

const loading = ref(false)
const keyword = ref('')
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

function roleLabel(code) {
  if (code === 'ADMIN') return '管理员'
  if (code === 'EDITOR') return '编辑'
  return code || '-'
}

async function load() {
  loading.value = true
  try {
    const data = await fetchUsers({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
    })
    records.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '用户加载失败')
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

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.toolbar .el-input {
  max-width: 280px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
