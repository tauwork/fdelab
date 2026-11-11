import http from './http'

export function fetchItems(params) {
  return http.get('/api/admin/product-items', { params })
}

export function fetchItem(id) {
  return http.get(`/api/admin/product-items/${id}`)
}

export function fetchItemMeta() {
  return http.get('/api/admin/product-items/meta')
}

export function createItem(data) {
  return http.post('/api/admin/product-items', data)
}

export function updateItem(id, data) {
  return http.put(`/api/admin/product-items/${id}`, data)
}

export function publishItem(id, published) {
  return http.put(`/api/admin/product-items/${id}/publish`, { published })
}

export function deleteItem(id) {
  return http.delete(`/api/admin/product-items/${id}`)
}

// 服务运作（fde_product_form）：与产品条目同口径的后台 CRUD
export function fetchForms(params) {
  return http.get('/api/admin/product-forms', { params })
}

export function fetchForm(id) {
  return http.get(`/api/admin/product-forms/${id}`)
}

export function fetchFormMeta() {
  return http.get('/api/admin/product-forms/meta')
}

export function createForm(data) {
  return http.post('/api/admin/product-forms', data)
}

export function updateForm(id, data) {
  return http.put(`/api/admin/product-forms/${id}`, data)
}

export function publishForm(id, published) {
  return http.put(`/api/admin/product-forms/${id}/publish`, { published })
}

export function deleteForm(id) {
  return http.delete(`/api/admin/product-forms/${id}`)
}
