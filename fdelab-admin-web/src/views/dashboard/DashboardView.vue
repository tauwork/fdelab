<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="never">
          <el-statistic title="用户" :value="stats.userCount" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="never">
          <el-statistic title="已发布动态" :value="stats.articleCount" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="never">
          <el-statistic title="已发布人才" :value="stats.talentCount" />
        </el-card>
      </el-col>
    </el-row>
    <el-alert v-if="error" class="alert" :title="error" type="warning" show-icon :closable="false" />
    <el-card class="note" shadow="never">
      <p>人才档案可以在这里维护，也可以由 FDE 技能库上传后自动生成子域名主页。</p>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { fetchDashboard } from '../../api/system'

const loading = ref(true)
const error = ref('')
const stats = reactive({
  userCount: 0,
  articleCount: 0,
  talentCount: 0,
})

onMounted(async () => {
  try {
    Object.assign(stats, await fetchDashboard())
  } catch (err) {
    error.value = err.message || '概览加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.alert,
.note {
  margin-top: 16px;
}

.note p {
  margin: 0;
  line-height: 1.7;
}
</style>
