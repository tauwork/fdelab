import { API_BASE } from './config'

export function request({ url, method = 'GET', data, quiet = false }) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE}${url}`,
      method,
      data,
      header: method === 'GET' ? {} : { 'Content-Type': 'application/json' },
      success: (res) => {
        const body = res.data || {}
        if (res.statusCode >= 200 && res.statusCode < 300 && body.code === 0) {
          resolve(body.data)
          return
        }
        const message = body.message || '请求失败'
        if (!quiet) {
          uni.showToast({ title: message, icon: 'none' })
        }
        reject(new Error(message))
      },
      fail: (error) => {
        if (!quiet) {
          uni.showToast({ title: '网络异常', icon: 'none' })
        }
        reject(error)
      },
    })
  })
}
