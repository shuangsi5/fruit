import request from './index'

// 平台管理端接口（ADMIN）

// 40. 商家列表（含审核状态）
export function getMerchants(params) {
  return request.get('/admin/merchants', { params })
}

// 41. 审核商家
export function auditMerchant(id, data) {
  return request.post('/admin/merchants/' + id + '/audit', data)
}

// 42. 用户管理列表
export function getUsers(params) {
  return request.get('/admin/users', { params })
}

// 43. 启用/禁用用户
export function updateUserStatus(id, data) {
  return request.put('/admin/users/' + id + '/status', data)
}

// 44. 投诉列表
export function getComplaints(params) {
  return request.get('/admin/complaints', { params })
}

// 45. 处理投诉
export function handleComplaint(id, data) {
  return request.post('/admin/complaints/' + id + '/handle', data)
}

// 46. 数据总览统计
export function getOverview(params) {
  return request.get('/admin/overview', { params })
}
