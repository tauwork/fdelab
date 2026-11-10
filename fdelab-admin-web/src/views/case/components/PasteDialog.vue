<template>
  <el-dialog
    :model-value="modelValue"
    title="从 Excel 粘贴指标"
    width="680px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <p class="tip">粘贴 6 列：指标名、口径、基线、结果、时间窗、可信度 A–D。列数不足补空，多余列忽略；首行表头会自动跳过。</p>
    <el-input v-model="text" type="textarea" :rows="10" placeholder="从 Excel 复制后直接粘贴到这里" />
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="confirm">导入</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

defineProps({
  modelValue: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'confirm'])
const text = ref('')

const HEADER = /^(指标名|指标名称|指标|metric|metrics)$/i

function parse(textValue) {
  const lines = String(textValue || '')
    .split(/\r?\n/)
    .map((line) => line.trimEnd())
    .filter((line) => line.trim() !== '')
  const rows = []
  lines.forEach((line, index) => {
    const cells = line.split('\t')
    if (index === 0 && HEADER.test(String(cells[0] || '').trim())) return
    const value = (position) => String(cells[position] == null ? '' : cells[position]).trim()
    rows.push({
      name: value(0),
      definition: value(1),
      baseline: value(2),
      result: value(3),
      window: value(4),
      cred: value(5).toUpperCase(),
      sourceRef: null,
    })
  })
  return rows
}

function confirm() {
  const rows = parse(text.value)
  if (rows.length === 0) {
    ElMessage.warning('没有解析到可导入的行')
    return
  }
  emit('confirm', rows)
  emit('update:modelValue', false)
  text.value = ''
}
</script>

<style scoped>
.tip {
  margin: 0 0 12px;
  color: #6d7872;
  font-size: 13px;
  line-height: 1.6;
}
</style>
