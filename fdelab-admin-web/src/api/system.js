import http from './http'

export function fetchDashboard() {
  return http.get('/api/system/dashboard')
}

export function fetchUsers(params) {
  return http.get('/api/system/users', { params })
}
