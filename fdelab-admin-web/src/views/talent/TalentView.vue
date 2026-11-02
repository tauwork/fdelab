<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="搜索姓名、子域名或介绍" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="openCreate">新建</el-button>
    </div>
    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column label="子域名" min-width="180">
        <template #default="{ row }">{{ row.slug }}.fdelab.io</template>
      </el-table-column>
      <el-table-column prop="headline" label="介绍" min-width="220" show-overflow-tooltip />
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.published === 1 ? 'success' : 'info'">{{ row.published === 1 ? '发布' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

  <el-dialog v-model="visible" :title="form.id ? '编辑人才' : '新建人才'" width="760px" top="6vh">
    <el-form label-position="top">
      <div class="grid">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="子域名，留空则按姓名生成"><el-input v-model="form.slug" placeholder="jiangliwen" /></el-form-item>
        <el-form-item label="一句话介绍" class="wide"><el-input v-model="form.headline" /></el-form-item>
        <el-form-item label="列表摘要" class="wide"><el-input v-model="form.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="微信"><el-input v-model="form.wechat" /></el-form-item>
        <el-form-item label="网站"><el-input v-model="form.website" /></el-form-item>
        <el-form-item label="网站名称"><el-input v-model="form.websiteLabel" /></el-form-item>
        <el-form-item label="常驻" class="wide"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="点赞数"><el-input-number v-model="form.likeCount" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.published" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="下架" />
        </el-form-item>
      </div>
      <el-form-item label="徽章，一行一个"><el-input v-model="form.badgesText" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="身份标签，一行一个"><el-input v-model="form.identityText" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="行业标签，一行一个"><el-input v-model="form.industryText" type="textarea" :rows="2" /></el-form-item>

      <section-list title="主要身份" :items="form.roles" @add="form.roles.push({ org: '', title: '', description: '' })">
        <template #default="{ item }">
          <el-input v-model="item.org" placeholder="组织" />
          <el-input v-model="item.title" placeholder="身份" />
          <el-input v-model="item.description" placeholder="说明" />
        </template>
      </section-list>
      <section-list title="过往经历" :items="form.experiences" @add="form.experiences.push({ title: '', description: '' })">
        <template #default="{ item }">
          <el-input v-model="item.title" placeholder="标题" />
          <el-input v-model="item.description" type="textarea" :rows="2" placeholder="内容" />
        </template>
      </section-list>
      <section-list title="提供的服务" :items="form.services" @add="form.services.push({ title: '', description: '' })">
        <template #default="{ item }">
          <el-input v-model="item.title" placeholder="服务" />
          <el-input v-model="item.description" type="textarea" :rows="2" placeholder="说明" />
        </template>
      </section-list>
      <section-list title="正在寻找的合作" :items="form.collaborations" @add="form.collaborations.push({ title: '', description: '' })">
        <template #default="{ item }">
          <el-input v-model="item.title" placeholder="主题" />
          <el-input v-model="item.description" type="textarea" :rows="2" placeholder="说明" />
        </template>
      </section-list>
      <section-list title="FDE 实战案例" :items="form.cases" @add="form.cases.push({ industry: '', title: '', portrait: '', pointsText: '', delivery: '' })">
        <template #default="{ item }">
          <el-input v-model="item.industry" placeholder="行业 · 主题" />
          <el-input v-model="item.title" placeholder="案例标题" />
          <el-input v-model="item.portrait" type="textarea" :rows="2" placeholder="客户画像" />
          <el-input v-model="item.pointsText" type="textarea" :rows="3" placeholder="要点，一行一条" />
          <el-input v-model="item.delivery" type="textarea" :rows="2" placeholder="交付与进展" />
        </template>
      </section-list>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createTalent, deleteTalent, fetchTalent, fetchTalents, updateTalent } from '../../api/talent'
import SectionList from './SectionList.vue'

const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const keyword = ref('')
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const form = reactive(blankForm())

function blankForm() {
  return {
    id: null,
    name: '',
    slug: '',
    headline: '',
    summary: '',
    phone: '',
    wechat: '',
    website: '',
    websiteLabel: '',
    location: '',
    published: 1,
    likeCount: 0,
    badgesText: '',
    identityText: '',
    industryText: '',
    roles: [],
    experiences: [],
    services: [],
    collaborations: [],
    cases: [],
  }
}

function lines(text) {
  return String(text || '').split('\n').map((item) => item.trim()).filter(Boolean)
}

function fillForm(data) {
  const profile = data?.profile || {}
  Object.assign(form, blankForm(), {
    id: data?.id || null,
    name: data?.name || '',
    slug: data?.slug || '',
    headline: data?.headline || '',
    summary: data?.summary || '',
    phone: data?.phone || '',
    wechat: data?.wechat || '',
    website: data?.website || '',
    websiteLabel: data?.websiteLabel || '',
    location: data?.location || '',
    published: data?.published ?? 1,
    likeCount: data?.likeCount || 0,
    badgesText: (profile.badges || []).join('\n'),
    identityText: (profile.identityTags || []).join('\n'),
    industryText: (profile.industryTags || []).join('\n'),
    roles: profile.roles || [],
    experiences: profile.experiences || [],
    services: profile.services || [],
    collaborations: profile.collaborations || [],
    cases: (profile.cases || []).map((item) => ({ ...item, pointsText: (item.points || []).join('\n') })),
  })
}

function payload() {
  return {
    name: form.name,
    slug: form.slug,
    headline: form.headline,
    summary: form.summary,
    phone: form.phone,
    wechat: form.wechat,
    website: form.website,
    websiteLabel: form.websiteLabel,
    location: form.location,
    published: form.published,
    likeCount: form.likeCount,
    profile: {
      badges: lines(form.badgesText),
      identityTags: lines(form.identityText),
      industryTags: lines(form.industryText),
      roles: form.roles,
      experiences: form.experiences,
      services: form.services,
      collaborations: form.collaborations,
      cases: form.cases.map((item) => ({
        industry: item.industry,
        title: item.title,
        portrait: item.portrait,
        points: lines(item.pointsText),
        delivery: item.delivery,
      })),
    },
  }
}

async function load() {
  loading.value = true
  try {
    const data = await fetchTalents({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    records.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '人才加载失败')
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

function openCreate() {
  fillForm(null)
  visible.value = true
}

async function openEdit(row) {
  const detail = await fetchTalent(row.id)
  fillForm(detail)
  visible.value = true
}

async function save() {
  saving.value = true
  try {
    if (form.id) await updateTalent(form.id, payload())
    else await createTalent(payload())
    visible.value = false
    ElMessage.success('已保存')
    load()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`删除 ${row.name} 的人才档案？`, '删除人才')
  await deleteTalent(row.id)
  ElMessage.success('已删除')
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
  max-width: 320px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.wide {
  grid-column: 1 / -1;
}
</style>
