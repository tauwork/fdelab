import http from './http'

export function getForms() {
  return http.get('/api/public/products')
}

export function getForm(code) {
  return http.get(`/api/public/products/${code}`)
}

export function getItems(params) {
  return http.get('/api/public/product-items', { params })
}

export function getItem(code) {
  return http.get(`/api/public/product-items/${code}`)
}
