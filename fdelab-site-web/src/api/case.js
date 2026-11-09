import http from './http'

export function fetchCases(params) {
  return http.get('/api/public/cases', { params })
}

export function getCaseDetail(code) {
  return http.get(`/api/public/cases/${code}`)
}

export function fetchCaseFacets() {
  return http.get('/api/public/cases/facets')
}
