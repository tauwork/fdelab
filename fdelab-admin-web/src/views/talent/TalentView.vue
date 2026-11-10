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
      <el-table-column label="CFDE 等级" width="110">
        <template #default="{ row }">{{ row.levelName || '未设置' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.published === 1 ? 'success' : 'info'">{{ row.published === 1 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="openCopy(row)">复制</el-button>
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

  <el-dialog v-model="visible" :title="form.id ? '编辑人才' : '新建人才'" width="760px" top="6vh">
    <el-form label-position="top">
      <div class="grid">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="子域名，留空则按姓名生成"><el-input v-model="form.slug" placeholder="jiangliwen" /></el-form-item>
        <el-form-item label="头像地址" class="wide"><el-input v-model="form.avatarUrl" placeholder="https://..." /></el-form-item>
        <el-form-item label="一句话介绍" class="wide"><el-input v-model="form.headline" /></el-form-item>
        <el-form-item label="列表摘要" class="wide"><el-input v-model="form.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="CFDE 等级">
          <el-select v-model="form.levelCode" clearable placeholder="未设置">
            <el-option label="M 大师级" value="M" />
            <el-option label="P 专业级" value="P" />
            <el-option label="A 助理级" value="A" />
          </el-select>
        </el-form-item>
        <el-form-item label="年资（年）"><el-input-number v-model="form.years" :min="0" :max="60" /></el-form-item>
        <el-form-item label="最近复核时间">
          <el-date-picker
            v-model="form.reviewTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            :disabled-date="notAfterToday"
          />
        </el-form-item>
        <el-form-item label="接单状态">
          <el-switch v-model="form.accepting" :active-value="1" :inactive-value="0" active-text="可接单" inactive-text="暂停接单" />
        </el-form-item>
        <el-form-item label="可公开案例数（自动统计）">
          <el-input :model-value="form.caseCount" disabled />
        </el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="微信"><el-input v-model="form.wechat" /></el-form-item>
        <el-form-item label="网站"><el-input v-model="form.website" /></el-form-item>
        <el-form-item label="网站名称"><el-input v-model="form.websiteLabel" /></el-form-item>
        <el-form-item label="常驻" class="wide"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="点赞数"><el-input-number v-model="form.likeCount" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-tag :type="form.published === 1 ? 'success' : 'info'">{{ form.published === 1 ? '已发布' : '草稿' }}</el-tag>
          <span class="state-hint">保存只更新内容，{{ isAdmin ? '发布/下架请用列表行的按钮' : '发布由管理员操作' }}</span>
        </el-form-item>
      </div>
      <el-form-item label="徽章，一行一个"><el-input v-model="form.badgesText" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="身份标签，一行一个"><el-input v-model="form.identityText" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="行业标签，一行一个"><el-input v-model="form.industryText" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="技能标签，一行一个"><el-input v-model="form.skillsText" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="能力雷达，0-10 分，五项全填才在详情页展示">
        <div class="radar">
          <div v-for="(dim, index) in RADAR_DIMS" :key="dim" class="radar-item">
            <span class="dim">{{ dim }}</span>
            <el-input-number
              v-model="form.radar[index]"
              :min="0"
              :max="10"
              size="small"
              controls-position="right"
              placeholder="未填"
            />
          </div>
          <el-button link type="primary" :disabled="form.radar.every((item) => item == null)" @click="clearRadar">
            清空
          </el-button>
        </div>
      </el-form-item>

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

      <el-form-item label="关联标杆案例（顺序即详情页展示顺序）">
        <div class="case-picker">
          <el-select
            v-model="casePick"
            filterable
            remote
            clearable
            :remote-method="searchCaseOptions"
            :loading="caseLoading"
            placeholder="搜索案例编号 / 标题 / 交付方"
            @change="addCase"
          >
            <el-option
              v-for="item in caseOptions"
              :key="item.id"
              :label="`${item.code} · ${item.title}`"
              :value="item.id"
              :disabled="isLinked(item.id)"
            >
              <span>{{ item.code }} · {{ item.title }}</span>
              <el-tag v-if="item.published !== 1" size="small" type="info">草稿</el-tag>
            </el-option>
          </el-select>
          <span class="hint">仅已发布案例会展示在详情页</span>
        </div>
        <div v-if="form.linkedCases.length" class="linked-list">
          <div v-for="(item, index) in form.linkedCases" :key="item.id" class="linked-row">
            <span class="code">{{ item.code }}</span>
            <span class="title">{{ item.title }}</span>
            <el-tag v-if="item.published !== 1" size="small" type="info">草稿</el-tag>
            <span class="actions">
              <el-button link :disabled="index === 0" @click="moveCase(index, -1)">上移</el-button>
              <el-button link :disabled="index === form.linkedCases.length - 1" @click="moveCase(index, 1)">下移</el-button>
              <el-button link type="danger" @click="form.linkedCases.splice(index, 1)">移除</el-button>
            </span>
          </div>
        </div>
        <p v-else class="hint">尚未关联案例</p>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createTalent,
  deleteTalent,
  fetchTalent,
  fetchTalentCases,
  fetchTalents,
  publishTalent,
  saveTalentCases,
  unpublishTalent,
  updateTalent,
} from '../../api/talent'
import { fetchCaseOptions } from '../../api/case'
import { useUserStore } from '../../stores/user'
import SectionList from './SectionList.vue'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.profile?.roleCode === 'ADMIN')

// 下标顺序即 profile_json.radar 的顺序，与后端 TalentProfileMeta.RADAR_DIMS 保持一致，不可调整。
const RADAR_DIMS = ['工程实现', '业务翻译', '评测设计', '抽象复用', '现场推进']

// 复核是对既往记录的确认，未来日期不可选。
const notAfterToday = (date) => date.getTime() > Date.now()

const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const keyword = ref('')
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const caseOptions = ref([])
const casePick = ref(null)
const caseLoading = ref(false)
const form = reactive(blankForm())

function blankForm() {
  return {
    id: null,
    name: '',
    slug: '',
    avatarUrl: '',
    headline: '',
    summary: '',
    phone: '',
    wechat: '',
    website: '',
    websiteLabel: '',
    location: '',
    published: 0,
    likeCount: 0,
    levelCode: '',
    years: null,
    accepting: 1,
    reviewTime: null,
    caseCount: 0,
    badgesText: '',
    identityText: '',
    industryText: '',
    skillsText: '',
    radar: RADAR_DIMS.map(() => null),
    roles: [],
    experiences: [],
    services: [],
    collaborations: [],
    linkedCases: [],
    legacyCases: [],
  }
}

// 旧后台的 profile_json.cases 不再编辑，但保存时原样带回，避免擦掉详情页的项目段落。

function lines(text) {
  return String(text || '').split('\n').map((item) => item.trim()).filter(Boolean)
}

function radarAt(list, index) {
  const value = Number((list || [])[index])
  return Number.isFinite(value) ? value : null
}

function clearRadar() {
  form.radar = RADAR_DIMS.map(() => null)
}

function fillForm(data) {
  const profile = data?.profile || {}
  Object.assign(form, blankForm(), {
    id: data?.id || null,
    name: data?.name || '',
    slug: data?.slug || '',
    avatarUrl: data?.avatarUrl || '',
    headline: data?.headline || '',
    summary: data?.summary || '',
    phone: data?.phone || '',
    wechat: data?.wechat || '',
    website: data?.website || '',
    websiteLabel: data?.websiteLabel || '',
    location: data?.location || '',
    published: data?.published ?? 1,
    likeCount: data?.likeCount || 0,
    levelCode: data?.levelCode || '',
    years: data?.years ?? null,
    accepting: data?.accepting ?? 1,
    reviewTime: data?.reviewTime || null,
    caseCount: data?.caseCount ?? 0,
    badgesText: (profile.badges || []).join('\n'),
    identityText: (profile.identityTags || []).join('\n'),
    industryText: (profile.industryTags || []).join('\n'),
    skillsText: (profile.skills || []).join('\n'),
    radar: RADAR_DIMS.map((_, index) => radarAt(profile.radar, index)),
    roles: profile.roles || [],
    experiences: profile.experiences || [],
    services: profile.services || [],
    collaborations: profile.collaborations || [],
    linkedCases: [],
    legacyCases: profile.cases || [],
  })
}

function payload() {
  return {
    name: form.name,
    slug: form.slug,
    avatarUrl: form.avatarUrl,
    headline: form.headline,
    summary: form.summary,
    phone: form.phone,
    wechat: form.wechat,
    website: form.website,
    websiteLabel: form.websiteLabel,
    location: form.location,
    likeCount: form.likeCount,
    levelCode: form.levelCode || null,
    years: form.years,
    accepting: form.accepting,
    reviewTime: form.reviewTime || null,
    profile: {
      badges: lines(form.badgesText),
      identityTags: lines(form.identityText),
      industryTags: lines(form.industryText),
      skills: lines(form.skillsText),
      // 后端只在 5 项齐全时渲染雷达卡，全空即不下发。
      radar: form.radar.every((item) => item == null) ? [] : form.radar.map(Number),
      roles: form.roles,
      experiences: form.experiences,
      services: form.services,
      collaborations: form.collaborations,
      cases: form.legacyCases,
    },
  }
}

async function searchCaseOptions(keywordText) {
  caseLoading.value = true
  try {
    caseOptions.value = await fetchCaseOptions(keywordText)
  } catch (error) {
    ElMessage.error(error.message || '案例搜索失败')
  } finally {
    caseLoading.value = false
  }
}

function isLinked(id) {
  return form.linkedCases.some((item) => item.id === id)
}

function addCase(id) {
  casePick.value = null
  if (!id || isLinked(id)) return
  const option = caseOptions.value.find((item) => item.id === id)
  if (option) form.linkedCases.push({ ...option })
}

function moveCase(index, offset) {
  const target = index + offset
  if (target < 0 || target >= form.linkedCases.length) return
  const [item] = form.linkedCases.splice(index, 1)
  form.linkedCases.splice(target, 0, item)
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
  caseOptions.value = []
  visible.value = true
}

async function openEdit(row) {
  const [detail, links] = await Promise.all([fetchTalent(row.id), fetchTalentCases(row.id)])
  fillForm(detail)
  form.linkedCases = links || []
  caseOptions.value = []
  visible.value = true
}

// 复制只带档案内容（介绍、标签、雷达、经历、服务、项目经历），身份、联系方式和认证结论留空由新档案自己填。
async function openCopy(row) {
  fillForm(await fetchTalent(row.id))
  Object.assign(form, {
    id: null,
    name: '',
    slug: '',
    avatarUrl: '',
    phone: '',
    wechat: '',
    website: '',
    websiteLabel: '',
    location: '',
    levelCode: '',
    years: null,
    reviewTime: null,
    likeCount: 0,
    caseCount: 0,
    published: 0,
    linkedCases: [],
  })
  caseOptions.value = []
  visible.value = true
}

async function save() {
  const filled = form.radar.filter((item) => item != null).length
  if (filled > 0 && filled < RADAR_DIMS.length) {
    ElMessage.warning('能力雷达需要 5 项全部填写，或用「清空」取消配置')
    return
  }
  saving.value = true
  try {
    const saved = form.id ? await updateTalent(form.id, payload()) : await createTalent(payload())
    const talentId = saved?.id || form.id
    await saveTalentCases(talentId, form.linkedCases.map((item) => item.id))
    const unpublished = form.linkedCases.filter((item) => item.published !== 1).length
    visible.value = false
    ElMessage.success(form.id ? '已保存' : '已保存为草稿，需在列表点「发布」才会上名录')
    if (unpublished > 0) {
      ElMessage.warning(`已关联 ${unpublished} 个未发布案例，详情页不会展示`)
    }
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
      await ElMessageBox.confirm(`下架 ${row.name}？下架后名录和主页都不再展示。`, '下架人才')
      await unpublishTalent(row.id)
      ElMessage.success('已下架')
    } else {
      await publishTalent(row.id)
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

.case-picker {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.case-picker .el-select {
  width: 360px;
}

.linked-list {
  width: 100%;
  margin-top: 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
}

.linked-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.linked-row:first-child {
  border-top: none;
}

.linked-row .code {
  color: var(--el-text-color-secondary);
  font-family: monospace;
}

.linked-row .title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.state-hint {
  margin-left: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.radar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 16px;
}

.radar-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.radar-item .dim {
  font-size: 13px;
}

.radar-item .el-input-number {
  width: 92px;
}
</style>
