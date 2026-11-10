import http from './http'

/** 站点前台地址，仅用于「预览」新窗口。 */
export const SITE_BASE = import.meta.env.VITE_SITE_BASE || 'http://localhost:5174'

/**
 * 后端 CaseAdminVO = { id, form, customerName, customerMasked, createTime, updateTime }。
 * 页面只认扁平字段，所以这里统一 {...form, id}。
 */
function flatten(data) {
  if (!data) return data
  return {
    ...(data.form || {}),
    id: data.id,
    customerName: data.customerName,
    customerMasked: data.customerMasked,
    createTime: data.createTime,
    updateTime: data.updateTime,
  }
}

export function fetchCases(params) {
  return http.get('/api/admin/cases', { params })
}

export function fetchCase(id) {
  return http.get(`/api/admin/cases/${id}`).then(flatten)
}

export function fetchCaseMeta() {
  return http.get('/api/admin/cases/meta')
}

export function createCase(data) {
  return http.post('/api/admin/cases', data).then(flatten)
}

export function updateCase(id, data) {
  return http.put(`/api/admin/cases/${id}`, data).then(flatten)
}

export function deleteCase(id) {
  return http.delete(`/api/admin/cases/${id}`)
}

export function cloneCase(id) {
  return http.post(`/api/admin/cases/${id}/clone`).then(flatten)
}

export function publishCase(id) {
  return http.post(`/api/admin/cases/${id}/publish`).then(flatten)
}

export function unpublishCase(id) {
  return http.post(`/api/admin/cases/${id}/unpublish`).then(flatten)
}

export function validateCase(data, id) {
  return http.post('/api/admin/cases/validate', data, {
    params: id ? { id } : undefined,
  })
}
