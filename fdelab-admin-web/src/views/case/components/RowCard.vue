<template>
  <div class="row-card" @dragover.prevent @drop.prevent="$emit('drop', index)">
    <div class="row-head">
      <span v-if="draggable" class="handle" draggable="true" title="拖拽排序" @dragstart.stop="$emit('dragstart', index)">⋮⋮</span>
      <span class="label">{{ label || `#${index + 1}` }}</span>
      <span class="actions">
        <el-button link :disabled="index === 0" @click="$emit('up')">上移</el-button>
        <el-button link :disabled="index === total - 1" @click="$emit('down')">下移</el-button>
        <el-button link type="danger" @click="$emit('remove')">删除</el-button>
      </span>
    </div>
    <div class="row-body">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  index: { type: Number, required: true },
  total: { type: Number, required: true },
  label: { type: String, default: '' },
  draggable: { type: Boolean, default: true },
})
defineEmits(['up', 'down', 'remove', 'dragstart', 'drop'])
</script>

<style scoped>
.row-card {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fffdf8;
  padding: 10px 12px 12px;
  margin-bottom: 10px;
}

.row-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.handle {
  cursor: grab;
  color: #9aa39d;
  letter-spacing: -2px;
  user-select: none;
}

.label {
  font-size: 12px;
  color: #6d7872;
}

.actions {
  margin-left: auto;
}

.row-body {
  display: grid;
  gap: 8px;
}
</style>
