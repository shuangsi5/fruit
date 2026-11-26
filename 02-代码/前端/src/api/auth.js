import request from './index'

// 认证接口

// 1. 注册：{ username, password, phone, realName, role, merchantName? }
export function register(data) {
  return request.post('/auth/register', data)
}

// 2. 登录：{ username, password } -> { token, userId, role, merchantId }
export function login(data) {
  return request.post('/auth/login', data)
}

// 3. 当前登录用户
export function getMe() {
  return request.get('/auth/me')
}

// 4. 退出登录
export function logout() {
  return request.post('/auth/logout')
}
