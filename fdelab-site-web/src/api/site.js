import http from './http'

export function fetchProfile() {
  return http.get('/api/public/profile')
}

export function fetchArticles() {
  return http.get('/api/public/articles')
}

export function fetchArticle(id) {
  return http.get(`/api/public/articles/${id}`)
}
