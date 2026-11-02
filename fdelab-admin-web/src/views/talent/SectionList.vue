<template>
  <section class="block">
    <div class="head">
      <h3>{{ title }}</h3>
      <el-button link type="primary" @click="$emit('add')">添加</el-button>
    </div>
    <div v-for="(item, index) in items" :key="index" class="item">
      <slot :item="item" />
      <el-button link type="danger" @click="items.splice(index, 1)">移除</el-button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  items: { type: Array, required: true },
})
defineEmits(['add'])
</script>

<style scoped>
.block {
  margin-top: 8px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.head h3 {
  margin: 0;
}

.item {
  display: grid;
  gap: 8px;
  margin-bottom: 12px;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
}
</style>
