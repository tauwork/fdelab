import http from './http'

export function searchCustomers(keyword) {
  return http.get('/api/admin/customers', {
    params: keyword ? { keyword } : undefined,
  })
}

export function createCustomer(data) {
  return http.post('/api/admin/customers', data)
}
