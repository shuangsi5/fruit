import axios from 'axios'
import { getErrorMessage, notifyError } from '../utils/request'

// Axios 实例：baseURL 统一指向 /api，由 Vite 代理转发到后端 8080
const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：注入 Authorization: Bearer <token>
service.interceptors.request.use(
  (config) => {
    let token = ''
    try {
      token = localStorage.getItem('ft_token') || ''
    } catch (e) {
      token = ''
    }
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：先解包 {code,message,data,timestamp}，code≠0 按错误码提示
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 兼容文件流等非标准包装
    if (res === null || typeof res !== 'object' || !('code' in res)) {
      return res
    }
    const code = res.code
    if (code === 0) {
      return res.data
    }
    // 40101/40102：清 Token 并跳登录
    if (code === 40101 || code === 40102) {
      try {
        localStorage.removeItem('ft_token')
        localStorage.removeItem('ft_user')
      } catch (e) {
        // ignore
      }
      const msg = getErrorMessage(code, res.message)
      notifyError(msg)
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(msg))
    }
    const msg = getErrorMessage(code, res.message)
    notifyError(msg)
    return Promise.reject(new Error(msg))
  },
  (error) => {
    let msg = '网络异常，请检查网络后重试'
    if (error.response) {
      const status = error.response.status
      if (status === 404) msg = '接口不存在（404）'
      else if (status === 500) msg = '服务器内部错误（500）'
      else if (status === 403) msg = '权限不足（403）'
      else if (status === 401) msg = '请先登录（401）'
    } else if (error.message && error.message.includes('timeout')) {
      msg = '请求超时，请稍后重试'
    }
    notifyError(msg)
    return Promise.reject(error)
  }
)

export default service
