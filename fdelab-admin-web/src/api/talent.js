import http from './http'

export function fetchTalents(params) {
  return http.get('/api/system/talents', { params })
}

export function fetchTalent(id) {
  return http.get(`/api/system/talents/${id}`)
}

export function createTalent(data) {
  return http.post('/api/system/talents', data)
}

export function updateTalent(id, data) {
  return http.put(`/api/system/talents/${id}`, data)
}

export function deleteTalent(id) {
  return http.delete(`/api/system/talents/${id}`)
}

export function publishTalent(id) {
  return http.post(`/api/system/talents/${id}/publish`)
}

export function unpublishTalent(id) {
  return http.post(`/api/system/talents/${id}/unpublish`)
}

export function fetchTalentCases(id) {
  return http.get(`/api/system/talents/${id}/cases`)
}

export function saveTalentCases(id, caseIds) {
  return http.put(`/api/system/talents/${id}/cases`, { caseIds })
}
