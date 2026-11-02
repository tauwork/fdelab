import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('fdelab_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code === 'number') {
      if (body.code !== 0) {
        return Promise.reject(new Error(body.message || '请求失败'))
      }
      return body.data
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || '网络异常'
    if (status === 401) {
      localStorage.removeItem('fdelab_token')
      if (location.pathname !== '/login') {
        location.assign('/login')
      }
    }
    return Promise.reject(new Error(message))
  },
)

export default http
