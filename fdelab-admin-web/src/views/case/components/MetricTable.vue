<template>
  <div class="metric-table">
    <div class="head">
      <span class="count">硬指标 {{ metrics.length }} 条</span>
      <span class="ops">
        <el-button size="small" @click="pasteVisible = true">从 Excel 粘贴</el-button>
        <el-button size="small" type="primary" plain @click="$emit('add')">添加指标</el-button>
      </span>
    </div>
    <div class="table-wrap">
      <el-table :data="metrics" size="small" border>
        <el-table-column label="#" width="46" align="center">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column label="指标名" min-width="150">
          <template #default="{ row }"><el-input v-model="row.name" size="small" /></template>
        </el-table-column>
        <el-table-column label="口径" min-width="180">
          <template #default="{ row }"><el-input v-model="row.definition" size="small" /></template>
        </el-table-column>
        <el-table-column label="基线" min-width="120">
          <template #default="{ row }"><el-input v-model="row.baseline" size="small" /></template>
        </el-table-column>
        <el-table-column label="结果" min-width="130">
          <template #default="{ row }"><el-input v-model="row.result" size="small" /></template>
        </el-table-column>
        <el-table-column label="时间窗" min-width="130">
          <template #default="{ row }"><el-input v-model="row.window" size="small" /></template>
        </el-table-column>
        <el-table-column label="可信度" width="92">
          <template #default="{ row }">
            <el-select v-model="row.cred" size="small" placeholder="—">
              <el-option v-for="code in ['A', 'B', 'C', 'D']" :key="code" :label="code" :value="code" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="来源序号" width="150">
          <template #default="{ row }">
            <el-select v-model="row.sourceRef" size="small" clearable placeholder="—">
              <el-option
                v-for="option in sourceOptions"
                :key="option.value"
                :label="`#${option.no} ${option.title}`"
                :value="option.value"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="168" fixed="right">
          <template #default="{ $index }">
            <el-button link :disabled="$index === 0" @click="$emit('move', $index, $index - 1)">上移</el-button>
            <el-button link :disabled="$index === metrics.length - 1" @click="$emit('move', $index, $index + 1)">下移</el-button>
            <el-button link type="danger" @click="$emit('remove', $index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <paste-dialog v-model="pasteVisible" @confirm="onPaste" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import PasteDialog from './PasteDialog.vue'
import { isSourceBlank } from '../validation'

const props = defineProps({
  metrics: { type: Array, required: true },
  sources: { type: Array, default: () => [] },
})
const emit = defineEmits(['add', 'move', 'remove', 'append'])
const pasteVisible = ref(false)

// 来源序号按「非空来源」编号（与落库后的 sources 下标一致），value 仍是 UI 原始行下标 + 1。
const sourceOptions = computed(() => {
  const options = []
  let no = 0
  ;(props.sources || []).forEach((source, index) => {
    if (!source || isSourceBlank(source)) return
    no += 1
    options.push({ value: index + 1, no, title: source.title || '未命名来源' })
  })
  return options
})

function onPaste(rows) {
  emit('append', rows)
}
</script>

<style scoped>
.metric-table {
  border: 1px dashed var(--line);
  border-radius: 10px;
  padding: 10px 12px 12px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.count {
  font-size: 13px;
  color: #6d7872;
}

.table-wrap {
  overflow-x: auto;
}
</style>
