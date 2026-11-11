<template>
  <el-tabs v-model="tab" class="product-admin-tabs">
    <el-tab-pane label="产品" name="items">
      <ProductListView />
    </el-tab-pane>
    <el-tab-pane label="服务" name="forms">
      <ServiceFormListView />
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import { ref } from 'vue'
import ProductListView from './ProductListView.vue'
import ServiceFormListView from './ServiceFormListView.vue'

// 与站点 /products 的一页两 TAB 对齐：产品 / 服务。
const tab = ref('items')
</script>

<style scoped>
.product-admin-tabs :deep(.el-tabs__header) {
  margin: 0 0 14px;
}
</style>
