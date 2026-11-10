<template>
  <div class="kpi-editor">
    <div class="head">
      <div>
        <b>卡片 KPI</b>
        <span class="hint">最多 4 条；v 是短值，m 关联指标行下标</span>
      </div>
      <el-button size="small" type="primary" plain :disabled="kpis.length >= 4" @click="$emit('add')">
        添加 KPI 卡
      </el-button>
    </div>

    <p v-if="!kpis.length" class="empty">还没有 KPI 卡。可以先添加指标行，再「从指标行生成」。</p>

    <div v-for="(kpi, index) in kpis" :key="index" class="card">
      <div class="card-head">
        <span>KPI {{ index + 1 }}</span>
        <el-button link type="danger" @click="$emit('remove', index)">删除</el-button>
      </div>
      <div class="grid">
        <el-input v-model="kpi.v" placeholder="短值，如 -48%" />
        <el-input v-model="kpi.k" placeholder="指标名" />
        <el-select v-model="kpi.m" clearable placeholder="关联指标行">
          <el-option
            v-for="option in metricOptions"
            :key="option.value"
            :label="`#${option.no} ${option.title}`"
            :value="option.value"
          />
        </el-select>
        <el-select
          :model-value="undefined"
          placeholder="从指标行生成"
          :disabled="!metricOptions.length"
          @change="applyMetric(kpi, $event)"
        >
          <el-option
            v-for="option in metricOptions"
            :key="option.value"
            :label="`#${option.no} ${option.title}`"
            :value="option.value"
          />
        </el-select>
        <label class="red">
          <el-switch v-model="kpi.r" />
          <span>红标</span>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { isMetricBlank } from '../validation'

const props = defineProps({
  kpis: { type: Array, required: true },
  metrics: { type: Array, required: true },
})
defineEmits(['add', 'remove'])

// 只列出非空指标；value 保留 UI 原始下标，落库时由页面统一映射成压缩下标。
const metricOptions = computed(() => {
  const options = []
  let no = 0
  ;(props.metrics || []).forEach((metric, index) => {
    if (!metric || isMetricBlank(metric)) return
    no += 1
    options.push({ value: index, no, title: metric.name || '未命名指标' })
  })
  return options
})

function applyMetric(kpi, metricIndex) {
  const metric = props.metrics[metricIndex]
  if (!metric) return
  // 只带出 v / k / m，红标仍由人工决定。
  kpi.v = metric.result || ''
  kpi.k = metric.name || ''
  kpi.m = metricIndex
}
</script>

<style scoped>
.kpi-editor {
  border: 1px dashed var(--line);
  border-radius: 10px;
  padding: 10px 12px 12px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.hint {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: #8a938d;
}

.empty {
  margin: 8px 0;
  color: #8a938d;
  font-size: 13px;
}

.card {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 8px 10px 10px;
  margin-bottom: 8px;
  background: #fffdf8;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #6d7872;
  margin-bottom: 6px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
}

.red {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6d7872;
}

@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
