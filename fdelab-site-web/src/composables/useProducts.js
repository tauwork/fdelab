import { ref } from 'vue'
import { getForms, getItems } from '../api/product'

/**
 * 产品与服务页的数据源：运作（4 条）+ 产品条目。
 * chip 计数与筛选都在这一份条目上做，不再重复请求。
 */
export function useProducts() {
  const forms = ref([])
  const items = ref([])
  const loading = ref(false)
  const error = ref('')

  async function load() {
    loading.value = true
    error.value = ''
    try {
      const [formList, itemList] = await Promise.all([
        getForms(),
        getItems(),
      ])
      forms.value = Array.isArray(formList) ? formList : []
      items.value = Array.isArray(itemList) ? itemList : []
    } catch (err) {
      error.value = err?.message || '加载失败'
    } finally {
      loading.value = false
    }
  }

  return { forms, items, loading, error, load }
}
