import http from './http'

export function fetchTalents(params) {
  return http.get('/api/public/talents', { params })
}

export function fetchRoster(params) {
  return http.get('/api/public/talents/roster', { params })
}

export function fetchTalentBySlug(slug, visitorKey) {
  return http.get(`/api/public/talents/slug/${slug}`, { params: { visitorKey } })
}

export function toggleTalentLike(id, visitorKey) {
  return http.post(`/api/public/talents/${id}/like`, { visitorKey })
}
